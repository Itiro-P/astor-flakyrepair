package fr.inria.astor.approaches.flakyseeding.extension;

import fr.inria.astor.approaches.flakydebug.Configuration;
import fr.inria.astor.core.validation.results.TestResult;

public class FsTestResult extends TestResult {
    @Override
    public boolean wasSuccessful() {
        return failures > 0 && failures < Configuration.getInstance().getExecutions();
    }
}