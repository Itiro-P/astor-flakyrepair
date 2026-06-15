package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators.Mutator;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Mutator que troca operandos Float de lado. Isso pode levar a erros de imprecisão que (talvez) não foram considerados.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FloatReverseMutator extends Mutator<CtBinaryOperator<Float>> {
    public FloatReverseMutator(Factory factory) {
        super(factory);
    }

    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        if (!(toMutate instanceof CtBinaryOperator)) return result;

        CtBinaryOperator operation = (CtBinaryOperator) toMutate;

        CtBinaryOperator cloned = operation.clone();

        cloned.setLeftHandOperand(operation.getRightHandOperand());
        cloned.setRightHandOperand(operation.getLeftHandOperand());

        result.add(new MutantCtElement(cloned, 1));
        return result;
    }
}

