package fr.inria.astor.approaches.flakyseeding.extension;

import fr.inria.astor.approaches.flakydebug.utils.Constants;
import fr.inria.astor.core.validation.results.TestResult;

public class FsTestResult extends TestResult {
    @Override
    public boolean wasSuccessful() {
        return failures > 0 && failures < Constants.EXECUTIONS;
    }
}