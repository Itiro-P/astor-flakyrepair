package fr.inria.astor.approaches.flakyseeding.extension.operators.mutators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * @brief Mutator que injeta ruído em números de ponto flutuante.
 * Alguns testes são instáveis por prezarem demais por precisão que muitas vezes é desnecessária.
 * Exemplo de PR afetado: https://github.com/apache/commons-math/pull/162
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FloatNoiseMutator extends Mutator<CtLiteral<Number>> {
    final static float FACTOR = 1.001f;
    public FloatNoiseMutator(Factory factory) {
        super(factory);
    }

    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        if (!(toMutate instanceof CtLiteral)) return result;
        CtLiteral<Number> literal = (CtLiteral<Number>) toMutate;

        CtLiteral mutatedLiteral = literal.clone();
        if (mutatedLiteral.getValue() instanceof Number) {
            Number originalValue = (Number) mutatedLiteral.getValue();
            // Preserva o tipo original do literal (float, double)
            if (originalValue instanceof Double) {
                double newValue = originalValue.doubleValue() * FloatNoiseMutator.FACTOR;
                mutatedLiteral.setValue((double) newValue);
            } else if (originalValue instanceof Float) {
                float newValue = originalValue.floatValue() * FloatNoiseMutator.FACTOR;
                mutatedLiteral.setValue((float) newValue);
            }
            result.add(new MutantCtElement(mutatedLiteral, 1));
        }
        return result;
    }
}