package fr.inria.astor.approaches.flakydebug.extension;

import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.manipulation.filters.TargetElementProcessor;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSpace;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;

public abstract class FdTargetElementProcessor extends TargetElementProcessor<CtCodeElement> {
	public FdTargetElementProcessor() {
		super();
	}

	protected void process(CtCodeElement element, OperatorSpace repairSpace) {
		for (AstorOperator op: repairSpace.getOperators()) {
			if (op.canBeAppliedToPoint(new ModificationPoint(element, null, null))) {
				if (element instanceof CtStatement) {
					super.add((CtStatement) element);
				} else if (element instanceof CtExpression) {
					super.add((CtExpression<?>) element);
				}
			}
		}
	}
}
