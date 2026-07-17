package fr.inria.astor.approaches.flakydebug.extension;


import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.astor.approaches.flakyseeding.extension.FsTestLauncher;
import fr.inria.astor.core.validation.results.TestResult;

public abstract class FdTestLauncher<T extends TestResult> {
    protected Logger log = Logger.getLogger(Thread.currentThread().getName());
    private List<FdRunner<T>> runners = new ArrayList<>();

    @SafeVarargs
    public FdTestLauncher(FdRunner<T>... r) {
        if (r != null) this.runners.addAll(Arrays.asList(r));
    }

    protected abstract T createTestResult();

    public T execute(String jvmPath, URL[] classpath, List<String> testsToExecute, int waitTime) { 
        T res = this.createTestResult();
        res.successTest.add(testsToExecute.get(0));
        for(FdRunner<T> apRunner: runners) {
            String approachName = apRunner.getClass().getSimpleName();
            log.info("Using approach " + approachName);
            res = apRunner.execute(jvmPath, classpath, testsToExecute, waitTime);
            if(res.wasSuccessful()) {
                log.info("The approach " + approachName + " was sucessfull");
                break;
            } else {
                log.info("The approach " + approachName + " failed");
            }
        }
        return res;
    } 

    /** Kept for compatibility. */
    public Class<?> laucherClassName() {
        return FsTestLauncher.class;
    }
}