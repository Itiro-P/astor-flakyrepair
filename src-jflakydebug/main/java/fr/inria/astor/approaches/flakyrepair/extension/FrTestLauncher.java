package fr.inria.astor.approaches.flakyrepair.extension;

import fr.inria.astor.approaches.flakydebug.extension.FdTestLauncher;
import fr.inria.astor.approaches.flakyrepair.extension.runners.NondexRunner;
import fr.inria.astor.core.validation.results.TestResult;

public class FrTestLauncher extends FdTestLauncher<TestResult> {
    public FrTestLauncher() {
        super(new NondexRunner());
    }

    @Override
    protected TestResult createTestResult() {
        return new TestResult();
    }
}
