package fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private CtTypeReference replacementType = null;
    private CtTypeReference targetType = null;

    public ShuffleMutator(Factory factory, CtTypeReference rep, CtTypeReference target) {
        super(factory);
        this.replacementType = rep;
        this.targetType = target;
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
            
        if(toMutate == null) return result;

        CtTypeReference originalType = null;
        List<CtExpression<?>> args = null;

        if (toMutate instanceof CtConstructorCall) {
            CtConstructorCall ctc = (CtConstructorCall) toMutate;
            originalType = ctc.getType();
            args = ctc.getArguments();

            if(!this.isValid(originalType)) return result;

            CtConstructorCall<?> wrapped = factory.createConstructorCall();
            wrapped.setType(this.replacementType);
            wrapped.setArguments(args);
            result.add(new MutantCtElement(wrapped, 1));
        } else if(toMutate instanceof CtLocalVariable) {
            CtLocalVariable localVar = (CtLocalVariable) toMutate;
            originalType = localVar.getType();
            args = Arrays.asList(localVar.getDefaultExpression().clone());

            if(!this.isValid(originalType)) return result;

            CtConstructorCall<?> wrapped = factory.createConstructorCall();
            wrapped.setType(this.replacementType);
            wrapped.setArguments(args);

            CtLocalVariable mutant = localVar.clone();
            mutant.setAssignment(wrapped);
            result.add(new MutantCtElement(mutant, 1));
        }

        return result;
    }

    private boolean isValid(CtTypeReference<?> typeRef) {
        if (typeRef == null) return false;
        return typeRef.isSubtypeOf(targetType);
    }
}