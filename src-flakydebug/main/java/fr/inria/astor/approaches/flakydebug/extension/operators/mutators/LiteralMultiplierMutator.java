package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Mutator que multiplica literais numéricos de certos métodos por um fator (ex: 2x). 
 * Útil para lidar com testes flaky causados por valores limite ou condições de corrida que dependem de tempos ou contagens específicas.
 * @author Pedro Itiro Nagao
 */
public class LiteralMultiplierMutator extends SpoonMutator<CtLiteral<Number>> {
    // Lista de fatores para multiplicar o literal alvo.
    private static final List<Double> multiplicationFactors = Arrays.asList(0.1, 0.4, 0.5, 0.9, 1.1, 1.5, 2.0, 5.0, 10.0);

    public LiteralMultiplierMutator(Factory factory) {
        super(factory);
    }

    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        if (!(toMutate instanceof CtLiteral)) return result;
        CtLiteral<?> literal = (CtLiteral<?>) toMutate;

        for(double factor : multiplicationFactors) {
            // Clonamos o literal original e aplicamos o fator
            CtLiteral mutatedLiteral = factory.Core().clone(literal);
            if (mutatedLiteral.getValue() instanceof Number) {
                Number originalValue = (Number) mutatedLiteral.getValue();
                double newValue = originalValue.doubleValue() * factor;
                // Preserva o tipo original do literal (int, long, float, double)
                if (originalValue instanceof Integer) {
                    mutatedLiteral.setValue((int) newValue);
                } else if (originalValue instanceof Long) {
                    mutatedLiteral.setValue((long) newValue);
                } else if (originalValue instanceof Float) {
                    mutatedLiteral.setValue((float) newValue);
                } else {
                    mutatedLiteral.setValue(newValue);
                }
                result.add(new MutantCtElement(mutatedLiteral, 1));
            }
        }
        return result;
    }

    @Override
	public String key() {
		return "literalMultiplierMutator";
	}

	@Override
	public void setup() {
	}


    @Override
	public int levelMutation() {
		return 1;
	}
}
