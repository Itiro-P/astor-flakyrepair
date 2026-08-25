package fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.shufflemutators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.Mutator;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffleGuards;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledJSON;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledList;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledMap;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledSet;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import spoon.reflect.cu.position.NoSourcePosition;
/**
 * @brief Mutator base de coleções.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class ShuffleMutator extends Mutator<CtElement> {
    private final ShuffleGuards guards;

    public ShuffleMutator(Factory factory) {
        super(factory);

        this.guards = new ShuffleGuards(new HashMap<CtTypeReference<?>, CtTypeReference<?>>() {{
            TypeFactory typeFactory = factory.Type();
            put(typeFactory.createReference(java.util.List.class), typeFactory.createReference(ShuffledList.class));
            put(typeFactory.createReference(java.util.Set.class), typeFactory.createReference(ShuffledSet.class));
            put(typeFactory.createReference(java.util.Map.class), typeFactory.createReference(ShuffledMap.class));
            put(typeFactory.createReference(org.json.JSONObject.class), typeFactory.createReference(ShuffledJSON.class));
        }});
    }

    /**
     * @brief Computa a mutação para um certo tipo alvo.
     * @param toMutate O elemento a ser mutado.
     * @param replacementType O tipo a ser trocado.
     * @param targetType O tipo original alvo.
     * @return Uma lista de mutantes.
     */
    public List<MutantCtElement> compute(CtElement toMutate,
        CtTypeReference replacementType,
        CtTypeReference targetType
    ) {
        List<MutantCtElement> result = new ArrayList<>();
            
        if (toMutate == null) return result;

        else if (toMutate instanceof CtConstructorCall) {
            CtConstructorCall<?> ctc = (CtConstructorCall<?>) toMutate;
            CtTypeReference<?> type = null;
            if (ctc.getTypeCasts().isEmpty()) {
                type = ctc.getType();
            } else ctc.getTypeCasts().get(0);

            if(type == null) type = ctc.getType();

            if(!this.guards.isCandidate(type, targetType)) return result;

            CtConstructorCall<?> wrapped = this.wrapTarget(replacementType, ctc);
            result.add(new MutantCtElement(wrapped, 1));
        }

        else if (toMutate instanceof CtLocalVariable) {
            // É uma variável local (`Map a = new HashMap(b)`)
            CtLocalVariable localVar = (CtLocalVariable) toMutate;
            CtExpression<?> assignment = localVar.getAssignment();

            CtTypeReference<?> type = null;
            if (assignment.getTypeCasts().isEmpty()) {
                type = assignment.getType();
            } else assignment.getTypeCasts().get(0);

            if(type == null || assignment == null || !this.guards.isCandidate(type, targetType)) return result;
            
            // Pegamos o tipo e seus argumentos (que aqui é a própria variável)
            // Criamos o novo construtor com os argumentos do alvo
            CtConstructorCall<?> wrapped = this.wrapTarget(replacementType, localVar.getDefaultExpression().clone());

            // Como é uma variável, precisamos mudar a ATRIBUIÇÃO dela
            CtLocalVariable mutant = localVar.clone();
            mutant.setAssignment(wrapped);
            result.add(new MutantCtElement(mutant, 1));
        }

        else if (toMutate instanceof CtInvocation) {
            CtInvocation<?> inv = (CtInvocation<?>) toMutate;
            CtTypeReference<?> type = null;
            if (inv.getTypeCasts().isEmpty()) {
                type = inv.getType();
            } else inv.getTypeCasts().get(0);
            
            if (type == null) return result;
            // Aqui pode ocorrer 2 casos:
            
            // A invocação retorna umm tipo que queremos mutacionar
            if (this.guards.isInvocationCandidate(inv, targetType)) {
                CtConstructorCall wrapped = this.wrapTarget(replacementType, inv);
                result.add(new MutantCtElement(wrapped, 1));
            }
            
            // O alvo da invocação é um tipo que queremos mutacionar
            else if (this.guards.isTargetInvocationCandidate(inv, targetType)) {
                CtConstructorCall wrapped = this.wrapTarget(replacementType, inv.getTarget());
                CtInvocation newParent = inv.clone();
                newParent.setTarget(wrapped);
                result.add(new MutantCtElement(newParent, 1));
            }
        }
        return result;
    }
    
    /**
     * @brief Cria um novo objeto com o tipo desejado envolto dele.
     * @param replacementType O tipo desejado.
     * @param target O objeto alvo.
     * @return Um novo objeto com o tipo desejado envolto dele.
     */
    private CtConstructorCall wrapTarget(CtTypeReference<?> replacementType, CtExpression<?> target) {
        CtConstructorCall wrapped = factory.createConstructorCall();
        CtExpression<?> clonedTarget = target.clone();
        clonedTarget.setPosition(new NoSourcePosition());
        clonedTarget.getElements(element -> true)
            .forEach(element -> element.setPosition(new NoSourcePosition()));
        wrapped.setType(replacementType);
        wrapped.setArguments(Arrays.asList(clonedTarget));
        return wrapped;
    }
}
