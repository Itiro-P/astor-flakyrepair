package fr.inria.astor.approaches.flakyrepair.extension;

import fr.inria.astor.approaches.flakydebug.extension.FdTargetElementProcessor;
import spoon.reflect.code.CtCodeElement;

public class FrTargetElementProcessor extends FdTargetElementProcessor {
	public FrTargetElementProcessor() {
		super();
	}

	@Override
	public void process(CtCodeElement element) {
		super.process(element, new FrRepairSpace());
	}
}