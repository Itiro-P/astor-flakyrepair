package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Mutator que troca operandos Float de lado. Isso pode levar a erros de imprecisão que (talvez) não foram considerados.
 */
public class FloatReverseMutator extends SpoonMutator<CtBinaryOperator<Float>> {
    public FloatReverseMutator(Factory factory) {
        super(factory);
    }

    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        if (!(toMutate instanceof CtBinaryOperator)) return result;

        CtBinaryOperator<Float> operation = (CtBinaryOperator<Float>) toMutate;

        CtBinaryOperator<Float> cloned = operation.clone();

        cloned.setLeftHandOperand(operation.getRightHandOperand());
        cloned.setRightHandOperand(operation.getLeftHandOperand());

        result.add(new MutantCtElement(cloned, 1));
        return result;
    }

    @Override
	public String key() {
		return "floatReverseMutator";
	}

	@Override
	public void setup() {
	}


    @Override
	public int levelMutation() {
		return 1;
	}
}

