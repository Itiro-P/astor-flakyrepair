package fr.inria.astor.approaches.flakyseeding.extension.operators;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.FloatReverseMutator;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.factory.TypeFactory;

/**
 * Mutator que troca operandos Float de lado. Isso pode levar a erros de imprecisão que (talvez) não foram considerados.
 * (Até agora), não há um PR que demonstre esta instabilidade. Mas o IEEE 754 não garante associatividade.
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FloatReverseOp extends Operator {
	Set<CtTypeReference> types;

	public FloatReverseOp() {
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
			// A operação deve ser de adição.

		CtBinaryOperator<?> operation = (CtBinaryOperator<?>) element;
		if(operation.getKind() != BinaryOperatorKind.PLUS) return false;

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