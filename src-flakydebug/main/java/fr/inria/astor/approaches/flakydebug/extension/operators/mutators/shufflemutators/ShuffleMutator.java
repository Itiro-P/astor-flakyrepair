package fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.Mutator;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
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
            
        if(toMutate == null) return result;

        CtTypeReference originalType = null;
        List<CtExpression<?>> args = null;

        if (toMutate instanceof CtConstructorCall) {
            // Se for uma chamada se construtor (`new HashMap<>()...`)
            // Precisamos enclausurá-lo em um novo construtor com nossa implementação embaralhada.s
            CtConstructorCall ctc = (CtConstructorCall) toMutate;
            // Pegamos o tipo e seus argumentos
            originalType = ctc.getType();
            args = ctc.getArguments();

            if(!this.isValid(originalType, targetType)) return result;

            // Criamos o novo construtor com os argumentos do alvo (para casos como `HashMap<int>(construtorAntigo)`)
            CtConstructorCall<?> wrapped = factory.createConstructorCall();
            wrapped.setType(replacementType);
            wrapped.setArguments(args);
            result.add(new MutantCtElement(wrapped, 1));
        } else if(toMutate instanceof CtLocalVariable) {
            // É uma variável local (`Map a = new HashMap(b)`)
            CtLocalVariable localVar = (CtLocalVariable) toMutate;
            // Pegamos o tipo e seus argumentos (que aqui é a própria variável)
            originalType = localVar.getType();
            args = Arrays.asList(localVar.getDefaultExpression().clone());

            if(!this.isValid(originalType, targetType)) return result;

            // Criamos o novo construtor com os argumentos do alvo
            CtConstructorCall<?> wrapped = factory.createConstructorCall();
            wrapped.setType(replacementType);
            wrapped.setArguments(args);

            // Como é uma variável, precisamos mudar a ATRIBUIÇÃO dela
            CtLocalVariable mutant = localVar.clone();
            mutant.setAssignment(wrapped);
            result.add(new MutantCtElement(mutant, 1));
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
        return typeRef.isSubtypeOf(targetType);
    }
}