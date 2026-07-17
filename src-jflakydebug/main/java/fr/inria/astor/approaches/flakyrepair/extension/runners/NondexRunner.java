package fr.inria.astor.approaches.flakyrepair.extension.runners;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fr.inria.astor.approaches.flakydebug.extension.FdRunner;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.validation.results.TestResult;

public class NondexRunner extends FdRunner<TestResult> {
    private static final int K = 1000;
    public NondexRunner() {
        super();
    }

    public TestResult execute(String jvmPath, URL[] classpath, List<String> testsToExecute, int waitTime) {
        String envOS     = System.getProperty("os.name");
        String timeZone  = ConfigurationProperties.getProperty("timezone");
        String location  = ConfigurationProperties.getProperty("location");

        String newClasspath = urlArrayToString(classpath);

        String test = testsToExecute.get(0);

        log.info("[NonDex] Running: " + test + " | K=" + K + " seeds");

        TestResult testResult = new TestResult();
        testResult.casesExecuted = K;
        testResult.failures = 0;
        List<String> successList = new ArrayList<>();
        successList.add(test);
        testResult.setSuccessTest(successList);

        int seed = SEED_RANDOM.nextInt() & Integer.MAX_VALUE;

        List<String> command = buildCommand(envOS, seed, newClasspath, test);

        try {
            File ftemp = File.createTempFile("nondex-out", ".txt");

            ProcessBuilder pb = new ProcessBuilder("/bin/bash");
            if (envOS != null && envOS.contains("Windows")) {
                pb = new ProcessBuilder("powershell", "-Command", "& " + commandToString(command));
            }
            pb.redirectErrorStream(true);
            pb.redirectOutput(ftemp);
            pb.directory(new File(location));

            printCommandToExecute(command, waitTime);

            Process p = pb.start();

            // Write command to bash stdin.
            try (BufferedWriter stdin = new BufferedWriter(
                new OutputStreamWriter(p.getOutputStream()))) {
                if (envOS == null || !envOS.contains("Windows")) {
                    stdin.write("TZ=\"" + timeZone + "\""); stdin.newLine(); stdin.flush();
                    stdin.write("export TZ");               stdin.newLine(); stdin.flush();
                    stdin.write(commandToString(command));  stdin.newLine(); stdin.flush();
                }
                stdin.write("exit"); stdin.newLine(); stdin.flush();
            } catch (IOException e) {
                log.error(e);
            }

            boolean finished = p.waitFor(waitTime, TimeUnit.MILLISECONDS);
            boolean timedOut = !finished;
            if (timedOut) {
                p.destroyForcibly();
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(ftemp))) {
                int failed = p.exitValue();
                testResult.failures += failed == 1 ? 1 : 0;
                //if (timedOut) testResult.failures++;
                log.info("[NonDex] Test " + test + " failed " + testResult.failures + " times of " + K + "...");
            } finally {
                ftemp.delete();
            }

        } catch (IOException | InterruptedException e) {
            testResult.failures += K;
        }

        log.info("[NonDex] Done — failures=" + testResult.failures + "/" + K);
        return testResult;
    }


    protected List<String> buildCommand(String envOS, int seed, String classpath, String test) {
        List<String> cmd = new ArrayList<>();
        String[] cs = classpath.split(File.pathSeparator);
        String firstPath = cs[0];

        boolean foundVariant = false;
        for(String c: cs) {
            if(c.contains(File.separator + "bin" + File.separator + "variant-")) {
                firstPath = c;
                foundVariant = true;
                break;
            }
        }

        if (foundVariant) {
            File mutatedFolder = new File(firstPath);

            String sampleTarget = ConfigurationProperties.getProperty("location") + "/target/test-classes";
            File pastaTargetClasses = new File(sampleTarget);
            
            try {
                org.apache.commons.io.FileUtils.copyDirectory(mutatedFolder, pastaTargetClasses);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        String pomPath = ConfigurationProperties.getProperty("location") + "/pom.xml";

        cmd.add("mvn");
        cmd.add("-f");
        cmd.add("\"" + pomPath + "\"");

        cmd.add("--quiet"); 
        cmd.add("--batch-mode");

        cmd.add("-Dmaven.main.skip=true");
        cmd.add("-Dmaven.test.compile.skip=true");
        cmd.add("-Dmaven.compiler.skip=true");
        
        cmd.add("edu.illinois:nondex-maven-plugin:2.2.1:nondex");
        cmd.add("-DnondexRuns=" + K);
        cmd.add("-Dtest=" + test);
        
        return cmd;
    }
}
