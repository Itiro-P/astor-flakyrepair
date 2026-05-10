package fr.inria.astor.approaches.flakyrepair.extension.testRunner;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.astor.approaches.flakyrepair.extension.testRunner.runners.NondexRunner;
import fr.inria.astor.approaches.flakyrepair.extension.testRunner.runners.Runner;
import fr.inria.astor.core.validation.results.TestResult;

public class TestLauncher {
    protected Logger log = Logger.getLogger(Thread.currentThread().getName());
    private static List<Runner> approaches = new ArrayList<>(Arrays.asList(new NondexRunner()));
    public TestLauncher() {
    }

    public TestResult execute(String jvmPath, URL[] classpath, List<String> testsToExecute, int waitTime) { 
        TestResult res = new TestResult();
        res.successTest.add(testsToExecute.get(0));
        for(Runner apRunner: approaches) {
            String approachName = apRunner.getClass().getSimpleName();
            log.info("Using approach " + approachName);
            TestResult curResult = apRunner.execute(jvmPath, classpath, testsToExecute, waitTime);
            res = curResult;
            if(curResult.wasSuccessful()) {
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
        return TestLauncher.class;
    }
}