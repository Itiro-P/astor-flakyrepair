package fr.inria.astor.approaches.flakyseeding.extension.operators.mutators;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class LiteralMultiplierMutator extends Mutator<CtInvocation<?>> {
    
    private static final List<Double> multiplicationFactors = Arrays.asList(0.9, 1.25, 1.5, 2.0, 5.0);

    public LiteralMultiplierMutator(Factory factory) {
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

            if (arg instanceof CtLiteral && ((CtLiteral<?>) arg).getValue() instanceof Number) {
                CtLiteral<?> literal = (CtLiteral<?>) arg;

                for (double factor : multiplicationFactors) {
                    CtInvocation<?> mutatedInvocation = origInvocation.clone();
                    
                    CtLiteral mutatedLiteral = (CtLiteral) mutatedInvocation.getArguments().get(i);
                    Number originalValue = (Number) literal.getValue();
                    double newValue = originalValue.doubleValue() * factor;
                    
                    if ((originalValue instanceof Integer || originalValue instanceof Long) && newValue <= 0) {
                        continue;
                    }

                    if (originalValue instanceof Integer) {
                        mutatedLiteral.setValue((int) newValue);
                    } else if (originalValue instanceof Long) {
                        mutatedLiteral.setValue((long) newValue);
                    } else {
                        mutatedLiteral.setValue(newValue);
                    }

                    result.add(new MutantCtElement(mutatedInvocation, 1));
                }
            }

            else if (arg instanceof CtVariableRead && arg.getType() != null && 
                     (arg.getType().isPrimitive() || arg.getType().isSubtypeOf(arg.getFactory().Type().createReference(Number.class)))) {
                
                CtVariableRead<?> varRead = (CtVariableRead<?>) arg;

                for (double factor : multiplicationFactors) {
                    CtInvocation<?> mutatedInvocation = origInvocation.clone();
                    
                    CtVariableRead<?> leftHandSide = (CtVariableRead<?>) mutatedInvocation.getArguments().get(i);

                    CtTypeReference<?> originalType = varRead.getType();

                    CtLiteral<Double> factorLiteral = varRead.getFactory().Code().createLiteral(factor);
                    CtBinaryOperator<?> multiplication = varRead.getFactory().Code().createBinaryOperator(
                        leftHandSide,
                        factorLiteral,
                        BinaryOperatorKind.MUL
                    );

                    // o tipo real da expressão é double (fator é double)
                    multiplication.setType((CtTypeReference) varRead.getFactory().Type().DOUBLE);

                    // mas precisa compilar de volta pro tipo original do parâmetro (ex: long)
                    if (originalType.isPrimitive() &&
                        !originalType.getSimpleName().equals("double") &&
                        !originalType.getSimpleName().equals("float")) {
                        multiplication.setTypeCasts(java.util.Collections.singletonList(originalType));
                    }

                    mutatedInvocation.getArguments().set(i, multiplication);
                    multiplication.setParent(mutatedInvocation);
                }
            }
        }

        return result;
    }
}
