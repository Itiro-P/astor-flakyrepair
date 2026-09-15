package fr.inria.astor.approaches.flakyseeding.extension.operators;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.FloatReverseMutator;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.code.CtLiteral;

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

        this.mutatorComposite.getMutators().add(new FloatReverseMutator(this.mutatorComposite.getFactory()));
		TypeFactory typeFactory = this.mutatorComposite.getFactory().Type();
		this.types = new HashSet<>(Arrays.asList(
			typeFactory.createReference(java.lang.Float.class),
			typeFactory.createReference(java.lang.Double.class)
		));
	}

	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement element = point.getCodeElement();
		if (!(element instanceof CtLiteral)) return false;

		CtLiteral<?> literal = (CtLiteral<?>) element;
		Object value = literal.getValue();

		if (value instanceof Double) {
			Double d = (Double) value;
			return !d.isNaN() && !d.isInfinite();
		}
		if (value instanceof Float) {
			Float f = (Float) value;
			return !f.isNaN() && !f.isInfinite();
		}

		return false;
	}
}