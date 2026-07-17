package fr.inria.astor.approaches.flakydebug.extension;

import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.astor.core.manipulation.filters.TargetElementProcessor;
import fr.inria.astor.core.solutionsearch.population.ProgramVariantFactory;

public class FdVariantFactory extends ProgramVariantFactory {
    protected Logger log = Logger.getLogger(Thread.currentThread().getName());

    public FdVariantFactory() {
		super();
	}

	public FdVariantFactory(List<TargetElementProcessor<?>> processors) {
		this();
		this.processors = processors;
	}
}

