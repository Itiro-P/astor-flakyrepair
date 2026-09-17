package fr.inria.astor.approaches.flakyseeding.extension.operators.mutators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * @brief Mutator que injeta ruído em números de ponto flutuante.
 * Alguns testes são instáveis por prezarem demais por precisão que muitas vezes é desnecessária.
 * Exemplo de PR afetado: https://github.com/apache/commons-math/pull/162
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FloatNoiseMutator extends Mutator<CtInvocation<?>> {
    final static float FACTOR = 1.001f;

    public FloatNoiseMutator(Factory factory) {
        super(factory);
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        
        if (!(toMutate instanceof CtInvocation)) {
            return result;
        }

        CtInvocation<?> origInvocation = (CtInvocation<?>) toMutate;
        List<CtExpression<?>> arguments = origInvocation.getArguments();

        for (int i = 0; i < arguments.size(); i++) {
            CtExpression<?> arg = arguments.get(i);

            if (arg instanceof CtLiteral) {
                CtLiteral<?> literal = (CtLiteral<?>) arg;
                Object val = literal.getValue();

                if (val instanceof Double || val instanceof Float) {
                    CtInvocation<?> mutatedInvocation = origInvocation.clone();

                    CtLiteral mutatedLiteral = (CtLiteral) mutatedInvocation.getArguments().get(i);
                    Number originalValue = (Number) literal.getValue();

                    if (originalValue instanceof Double) {
                        double newValue = originalValue.doubleValue() * FloatNoiseMutator.FACTOR;
                        if (Double.isInfinite(newValue) || Double.isNaN(newValue)) continue;
                        mutatedLiteral.setValue(newValue);
                    } else if (originalValue instanceof Float) {
                        float newValue = originalValue.floatValue() * FloatNoiseMutator.FACTOR;
                        if (Float.isInfinite(newValue) || Float.isNaN(newValue)) continue;
                        mutatedLiteral.setValue(newValue);
                    }

                    result.add(new MutantCtElement(mutatedInvocation, 1));
                }
            }

            else if (arg instanceof CtVariableRead && arg.getType() != null) {
                String typeName = arg.getType().getSimpleName();
                boolean isPrimitiveFloat = typeName.equals("float") || typeName.equals("double");
                boolean isWrapperFloat = arg.getType().isSubtypeOf(arg.getFactory().Type().createReference(Double.class)) ||
                                         arg.getType().isSubtypeOf(arg.getFactory().Type().createReference(Float.class));

                if (isPrimitiveFloat || isWrapperFloat) {
                    CtVariableRead<?> varRead = (CtVariableRead<?>) arg;
                    CtInvocation<?> mutatedInvocation = origInvocation.clone();

                    CtVariableRead<?> leftHandSide = (CtVariableRead<?>) mutatedInvocation.getArguments().get(i);

                    CtLiteral<Float> factorLiteral = varRead.getFactory().Code().createLiteral(FloatNoiseMutator.FACTOR);
                    CtBinaryOperator<?> multiplication = varRead.getFactory().Code().createBinaryOperator(
                        leftHandSide, 
                        factorLiteral, 
                        BinaryOperatorKind.MUL
                    );
                    multiplication.setType((spoon.reflect.reference.CtTypeReference) varRead.getType());

                    mutatedInvocation.getArguments().set(i, multiplication);
                    multiplication.setParent(mutatedInvocation);

                    result.add(new MutantCtElement(mutatedInvocation, 1));
                }
            }
        }

        return result;
    }
}
