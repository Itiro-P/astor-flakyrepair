package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

public class FloatNoiseMutator extends SpoonMutator<CtLiteral<Float>> {
    public FloatNoiseMutator(Factory factory) {
        super(factory);
    }

    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        if (!(toMutate instanceof CtLiteral)) return result;
        CtLiteral<Float> literal = (CtLiteral<Float>) toMutate;

        CtLiteral mutatedLiteral = factory.Core().clone(literal);
        if (mutatedLiteral.getValue() instanceof Float) {
            Float originalValue = (Float) mutatedLiteral.getValue();
            float newValue = originalValue.floatValue() * 1.01f;
            mutatedLiteral.setValue(newValue);
            result.add(new MutantCtElement(mutatedLiteral, 1));
        }
        return result;
    }

    @Override
	public String key() {
		return "floatNoiseMutator";
	}

	@Override
	public void setup() {
	}


    @Override
	public int levelMutation() {
		return 1;
	}
}