package fr.inria.astor.approaches.flakyseeding.extension.operators;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.FloatNoiseMutator;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

/**
 * @brief Operador que injeta ruído em números de ponto flutuante.
 * Alguns testes são instáveis por prezarem demais por precisão que muitas vezes é desnecessária.
 * Exemplo de PR afetado: https://github.com/apache/commons-math/pull/162
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FloatNoiseOp extends Operator {
	Set<CtTypeReference> types;

	public FloatNoiseOp() {
		super();

		this.mutatorComposite.getMutators().add(new FloatNoiseMutator(this.mutatorComposite.getFactory()));
		
		TypeFactory typeFactory = this.mutatorComposite.getFactory().Type();
		this.types = new HashSet<>(Arrays.asList(
			typeFactory.createReference(java.lang.Float.class),
			typeFactory.createReference(java.lang.Double.class)
		));
	}

	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement element = point.getCodeElement();
		if (!(element instanceof CtInvocation)) return false;

		CtInvocation<?> invocation = (CtInvocation<?>) element;
		List<CtExpression<?>> arguments = invocation.getArguments();

		// O ponto só será elegível se possuir pelo menos um argumento mutável de ponto flutuante
		return arguments.stream().anyMatch(arg -> {
			if (arg instanceof CtLiteral) {
				Object value = ((CtLiteral<?>) arg).getValue();
				if (value instanceof Double) {
					Double d = (Double) value;
					return !d.isNaN() && !d.isInfinite();
				}
				if (value instanceof Float) {
					Float f = (Float) value;
					return !f.isNaN() && !f.isInfinite();
				}
			}

			if (arg instanceof CtVariableRead && arg.getType() != null) {
				String typeName = arg.getType().getSimpleName();
				boolean isPrimitiveFloat = typeName.equals("float") || typeName.equals("double");
				boolean isWrapperFloat = arg.getType().isSubtypeOf(arg.getFactory().Type().createReference(Double.class)) ||
				                         arg.getType().isSubtypeOf(arg.getFactory().Type().createReference(Float.class));
				
				return isPrimitiveFloat || isWrapperFloat;
			}
			
			return false;
		});
	}
}
