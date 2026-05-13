package fr.inria.astor.approaches.flakydebug.extension.testRunner.runners;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import fr.inria.astor.core.validation.results.TestResult;
import fr.inria.astor.approaches.flakydebug.extension.FdTestResult;
import fr.inria.astor.core.setup.ConfigurationProperties;

public abstract class Runner {
    protected Logger log = Logger.getLogger(Thread.currentThread().getName());
    protected static final int K = 10;
    protected static final Random SEED_RANDOM = new Random();
    boolean avoidInterruption = false;

    public Runner(boolean avoidInterruption) {
        this.avoidInterruption = avoidInterruption;
    }

    public Runner() {
    }

    public abstract FdTestResult execute(String jvmPath, URL[] classpath, List<String> testsToExecute, int waitTime);

    protected String urlArrayToString(URL[] urls) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < urls.length; i++) {
            if (i > 0) sb.append(File.pathSeparator);
            sb.append(new File(urls[i].getFile()).getAbsolutePath());
        }
        return sb.toString();
    }

    protected String commandToString(List<String> command) {
        return command.toString().replace("[", "").replace("]", "").replace(",", " ");
    }

    protected void printCommandToExecute(List<String> command, int waitTime) {
        String cmd = commandToString(command);
        int trunk = ConfigurationProperties.getPropertyInt("commandTrunk");
        String toPrint = (trunk != 0 && cmd.length() > trunk)
                ? cmd.substring(0, trunk) + "..AND " + (cmd.length() - trunk) + " CHARS MORE..."
                : cmd;
        log.debug("Executing process: (timeout" + waitTime / 1000 + "secs) \n" + toPrint);
    }
}
