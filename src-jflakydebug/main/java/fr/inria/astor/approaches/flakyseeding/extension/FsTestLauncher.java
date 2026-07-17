package fr.inria.astor.approaches.flakyseeding.extension;

import fr.inria.astor.approaches.flakyseeding.extension.runners.JUnitRunner;
import fr.inria.astor.approaches.flakydebug.extension.FdTestLauncher;

public class FsTestLauncher extends FdTestLauncher<FsTestResult> {
    public FsTestLauncher() {
        super(new JUnitRunner());
    }

    @Override
    protected FsTestResult createTestResult() {
        return new FsTestResult();
    }
}