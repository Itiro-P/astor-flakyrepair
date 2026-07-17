package fr.inria.astor.approaches.flakyseeding.extension;

import fr.inria.astor.approaches.flakydebug.extension.FdProcessValidator;

public class FsProcessValidator extends FdProcessValidator<FsTestLauncher, FsTestResult, FsTestCasesProgramValidationResult> {
	@Override
    protected FsTestLauncher createTestLauncher() {
        return new FsTestLauncher();
    }

    @Override
    protected FsTestCasesProgramValidationResult createValidationResult(FsTestResult result, boolean wasSuccessful) {
        return new FsTestCasesProgramValidationResult(result, wasSuccessful, false);
    }
}
