package fr.inria.astor.approaches.flakyrepair.extension;

import fr.inria.astor.core.entities.validation.TestCaseVariantValidationResult;
import fr.inria.astor.core.entities.validation.VariantValidationResult;
import fr.inria.astor.core.solutionsearch.population.FitnessFunction;

public class FrFitnessFunction implements FitnessFunction {
    public double calculateFitnessValue(VariantValidationResult validationResult) {
        TestCaseVariantValidationResult vr = (TestCaseVariantValidationResult)validationResult;
        return (2 * Math.min(vr.getCasesExecuted() - vr.getFailureCount(), vr.getFailureCount())) / vr.getCasesExecuted();
    }

	
	public double getWorstMaxFitnessValue() {
        return 1.0;
    }
}
