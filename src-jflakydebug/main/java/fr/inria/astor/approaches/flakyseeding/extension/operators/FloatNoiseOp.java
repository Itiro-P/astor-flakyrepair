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
		// Vemos se é um opareando.
		if(!(element instanceof CtBinaryOperator)) return false;
		CtBinaryOperator operation = (CtBinaryOperator) element;

		// Vemos se algum dos números é um número de ponto flututante (float ou double)
		boolean firstMatch = this.types.stream().anyMatch(type -> {
			return (
				operation.getRightHandOperand().getType().isSubtypeOf(type) ||
				operation.getLeftHandOperand().getType().isSubtypeOf(type) ||
				operation.getType().isSubtypeOf(type)
			);
		});
		return firstMatch;
	}
}