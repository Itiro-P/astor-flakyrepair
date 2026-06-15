package fr.inria.astor.approaches.flakydebug.extension.operators;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.FloatReverseMutator;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

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
		CtBinaryOperator operator = (CtBinaryOperator) element;

		boolean firstMatch = this.types.stream().anyMatch(type -> {
			return (
				operator.getRightHandOperand().getType().isSubtypeOf(type) ||
				operator.getLeftHandOperand().getType().isSubtypeOf(type) ||
				operator.getType().isSubtypeOf(type)
			);
		});
		return firstMatch;
	}
}
