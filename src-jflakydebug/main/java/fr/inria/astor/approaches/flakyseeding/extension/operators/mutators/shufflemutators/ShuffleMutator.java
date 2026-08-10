package fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.shufflemutators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.Mutator;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * @brief Mutator base de coleções.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class ShuffleMutator extends Mutator<CtElement> {
    public ShuffleMutator(Factory factory) {
        super(factory);
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
            if(!this.isValid(ctc.getType(), targetType)) return result;

            CtConstructorCall<?> wrapped = this.wrapTarget(replacementType, ctc);
            result.add(new MutantCtElement(wrapped, 1));
        }

        else if (toMutate instanceof CtLocalVariable) {
            // É uma variável local (`Map a = new HashMap(b)`)
            CtLocalVariable localVar = (CtLocalVariable) toMutate;
            if(!this.isValid(localVar.getType(), targetType)) return result;
            
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
            // Aqui pode ocorrer 2 casos:
            
            // A invocação retorna umm tipo que queremos mutacionar
            if (this.isValid(inv.getType(), targetType)) {
                CtConstructorCall wrapped = this.wrapTarget(replacementType, inv);
                result.add(new MutantCtElement(wrapped, 1));
            }
            
            // O alvo da invocação é um tipo que queremos mutacionar
            else if (this.isValid(inv.getTarget().getType(), targetType) && !(inv.getParent() instanceof CtBlock)) {
                CtConstructorCall wrapped = this.wrapTarget(replacementType, inv.getTarget());
                CtInvocation newParent = inv.clone();
                newParent.setTarget(wrapped);
                result.add(new MutantCtElement(newParent, 1));
            }
        }
        return result;
    }

    /**
     * @brief Verifica se o tipo é uma implementação da interface `targetType`.
     * Ex: Se `targetype = Map`, então `HashMap -> true` e `ArrayList - false`.
     * @param typeRef O tipo
     * @return Se o tipo é uma implementação (ou subtipo) de `targetype`.
     */
    private boolean isValid(CtTypeReference<?> typeRef, CtTypeReference targetType) {
        if (typeRef == null) return false;
        try {
            CtTypeReference<?> erased = typeRef.getTypeErasure();
            if (erased == null || erased instanceof CtTypeParameterReference) return false;
            return erased.isSubtypeOf(targetType);
        } catch (Exception e) {
          return false;
        }
    }
    
    /**
     * @brief Cria um novo objeto com o tipo desejado envolto dele.
     * @param replacementType O tipo desejado.
     * @param target O objeto alvo.
     * @return Um novo objeto com o tipo desejado envolto dele.
     */
    private CtConstructorCall wrapTarget(CtTypeReference<?> replacementType, CtExpression<?> target) {
        CtConstructorCall wrapped = factory.createConstructorCall();
        wrapped.setType(replacementType);
        wrapped.setArguments(Arrays.asList(target.clone()));
        return wrapped;
    }
}
