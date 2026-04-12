package fr.inria.astor.approaches.flakyrepair.extension.TestRunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.validation.results.TestResult;

public class TestLauncher {

    private static final int K = 10;

    private static final Random SEED_RANDOM = new Random();

    protected Logger log = Logger.getLogger(Thread.currentThread().getName());
    boolean avoidInterruption = false;

    public TestLauncher(boolean avoidInterruption) {
        this.avoidInterruption = avoidInterruption;
    }

    public TestLauncher() {
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
            if (!finished) {
                p.destroyForcibly();
                testResult.failures++;
                ftemp.delete();
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(ftemp))) {
                int failed = parseOutput(reader, test);
                testResult.failures += failed;
                log.info("[NonDex] Test " + test + " failed " + testResult.failures + " times of " + TestLauncher.K + " iterations.\n");
            } finally {
                ftemp.delete();
            }

            p.destroyForcibly();

        } catch (IOException | InterruptedException e) {
            testResult.failures += TestLauncher.K;
        }

        log.info("[NonDex] Done — failures=" + testResult.failures + "/" + K);
        return testResult;
    }

    /**
     * Builds the Java command with NonDex via -Xbootclasspath/p
     * and TestRunner as the main class.
     */
    private List<String> buildCommand(String envOS, int seed, String classpath, String test) {
        List<String> cmd = new ArrayList<>();
        String[] cs = classpath.split(File.pathSeparator);
        String firstPath = cs[0];
        for(String c: cs) {
            if(c.contains(File.separator + "bin" + File.separator + "variant-")) {
                firstPath = c;
                break;
            }
        }
        String pomPath = ConfigurationProperties.getProperty("location") + "pom.xml";

        File mutatedFolder = new File(firstPath);
        File pastaTargetClasses = new File(ConfigurationProperties.getProperty("location"), "target/test-classes");

        try {
            org.apache.commons.io.FileUtils.copyDirectory(mutatedFolder, pastaTargetClasses);
        } catch (IOException e) {}

        cmd.add("mvn");
        cmd.add("-f");
        cmd.add("\"" + pomPath + "\"");

        cmd.add("--quiet"); 
        cmd.add("--batch-mode");
        
        cmd.add("-Dmaven.test.additionalClasspath=\"" + firstPath + "\"");
        
        cmd.add("edu.illinois:nondex-maven-plugin:2.2.1:nondex");
        cmd.add("-DnondexSeed=" + seed);
        cmd.add("-DnondexRuns=" + TestLauncher.K);
        cmd.add("-Dtest=" + test);
        
        return cmd;
    }

    private int parseOutput(BufferedReader reader, String test) throws IOException {
        String line;
        int timesFailed = 0;
        while ((line = reader.readLine()) != null) {
            if (line.contains("<<< FAILURE! - in")) {
                timesFailed++;
            }
        }
        return timesFailed;
    }

    private String urlArrayToString(URL[] urls) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < urls.length; i++) {
            if (i > 0) sb.append(File.pathSeparator);
            sb.append(new File(urls[i].getFile()).getAbsolutePath());
        }
        return sb.toString();
    }

    private void printCommandToExecute(List<String> command, int waitTime) {
        String cmd = commandToString(command);
        int trunk = ConfigurationProperties.getPropertyInt("commandTrunk");
        String toPrint = (trunk != 0 && cmd.length() > trunk)
                ? cmd.substring(0, trunk) + "..AND " + (cmd.length() - trunk) + " CHARS MORE..."
                : cmd;
        log.debug("Executing process: (timeout" + waitTime / 1000 + "secs) \n" + toPrint);
    }

    private String commandToString(List<String> command) {
        return command.toString().replace("[", "").replace("]", "").replace(",", " ");
    }

    /** Kept for compatibility. */
    public Class<?> laucherClassName() {
        return TestLauncher.class;
    }
}