package fr.inria.astor.approaches.flakydebug.extension.testRunner;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.astor.approaches.flakydebug.extension.FdTestResult;
import fr.inria.astor.approaches.flakydebug.extension.testRunner.runners.JUnitRunner;
import fr.inria.astor.approaches.flakydebug.extension.testRunner.runners.Runner;

public class TestLauncher {
    protected Logger log = Logger.getLogger(Thread.currentThread().getName());
    private static List<Runner> approaches = new ArrayList<>(Arrays.asList(new JUnitRunner()));
    public TestLauncher() {
    }

    public FdTestResult execute(String jvmPath, URL[] classpath, List<String> testsToExecute, int waitTime) { 
        FdTestResult res = new FdTestResult();
        res.successTest.add(testsToExecute.get(0));
        for(Runner apRunner: approaches) {
            String approachName = apRunner.getClass().getSimpleName();
            log.info("Using approach " + approachName);
            FdTestResult curResult = apRunner.execute(jvmPath, classpath, testsToExecute, waitTime);
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