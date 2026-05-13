package fr.inria.astor.approaches.flakydebug.extension;

import fr.inria.astor.core.validation.results.TestResult;

public class FdTestResult extends TestResult {
    @Override
    public boolean wasSuccessful() {
        return failures > 0;
    }
}