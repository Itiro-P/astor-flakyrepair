package fr.inria.astor.approaches.flakydebug.extension;

import fr.inria.astor.core.manipulation.filters.TargetElementProcessor;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;

public class FdTargetElementProcessor extends TargetElementProcessor<CtCodeElement> {
	public FdTargetElementProcessor(){
		super();
	}

	@Override
	public void process(CtCodeElement element) {
		if (element instanceof CtStatement) {
			super.add((CtStatement) element);
		} else if (element instanceof CtExpression) {
			super.add((CtExpression<?>) element);
		}
	}
}
