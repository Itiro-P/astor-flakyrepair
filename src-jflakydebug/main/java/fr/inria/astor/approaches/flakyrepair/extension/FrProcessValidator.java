package fr.inria.astor.approaches.flakyrepair.extension;

import fr.inria.astor.approaches.flakydebug.extension.FdProcessValidator;
import fr.inria.astor.core.validation.results.TestCasesProgramValidationResult;
import fr.inria.astor.core.validation.results.TestResult;

public class FrProcessValidator extends FdProcessValidator<FrTestLauncher, TestResult, TestCasesProgramValidationResult> {
	@Override
    protected FrTestLauncher createTestLauncher() {
        return new FrTestLauncher();
    }

    @Override
    protected TestCasesProgramValidationResult createValidationResult(TestResult result, boolean wasSuccessful) {
        return new TestCasesProgramValidationResult(result, wasSuccessful, false);
    }
}
