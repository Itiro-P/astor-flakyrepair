package fr.inria.astor.approaches.flakyseeding.extension;

import fr.inria.astor.approaches.flakydebug.extension.FdTargetElementProcessor;
import spoon.reflect.code.CtCodeElement;

public class FsTargetElementProcessor extends FdTargetElementProcessor {
	public FsTargetElementProcessor() {
		super();
	}

	@Override
	public void process(CtCodeElement element) {
		super.process(element, new FsRepairSpace());
	}
}
