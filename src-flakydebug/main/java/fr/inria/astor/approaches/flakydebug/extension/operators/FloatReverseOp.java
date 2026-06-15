package fr.inria.astor.approaches.flakydebug.extension.operators;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.FloatReverseMutator;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.declaration.CtElement;

/**
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings("unchecked")
public class FloatReverseOp extends Operator {
	public FloatReverseOp() {
		super();
        this.mutatorComposite.getMutators().add(new FloatReverseMutator(this.mutatorComposite.getFactory()));
	}

	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {
		CtElement element = point.getCodeElement();
		// Vemos se é um opareando.
		return (element instanceof CtBinaryOperator);
	}
}