package fr.inria.astor.approaches.flakydebug.extension;

import fr.inria.astor.core.entities.validation.TestCaseVariantValidationResult;
import fr.inria.astor.core.entities.validation.VariantValidationResult;
import fr.inria.astor.core.solutionsearch.population.FitnessFunction;

public class FdFitnessFunction implements FitnessFunction {
    public double calculateFitnessValue(VariantValidationResult validationResult) {
        TestCaseVariantValidationResult vr = (TestCaseVariantValidationResult)validationResult;
        return ((double) (2 * Math.min(vr.getCasesExecuted() - vr.getFailureCount(), vr.getFailureCount()))) / vr.getCasesExecuted();
    }

	
	public double getWorstMaxFitnessValue() {
        return 0.0;
    }
}
