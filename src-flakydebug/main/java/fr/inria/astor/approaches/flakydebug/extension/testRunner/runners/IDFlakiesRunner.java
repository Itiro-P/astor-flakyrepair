package fr.inria.astor.approaches.flakydebug.extension.testRunner.runners;

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

import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.validation.results.TestResult;

public class IDFlakiesRunner extends Runner {
    public IDFlakiesRunner() {
        super();
    }

    public TestResult execute(String jvmPath, URL[] classpath, List<String> testsToExecute, int waitTime) {
        String envOS     = System.getProperty("os.name");
        String timeZone  = ConfigurationProperties.getProperty("timezone");
        String location  = ConfigurationProperties.getProperty("location");

        String newClasspath = urlArrayToString(classpath);

        String test = testsToExecute.get(0);

        log.info("[IDFlakies] Running: " + test + " | K=" + K + " seeds");

        TestResult testResult = new TestResult();
        testResult.casesExecuted = K;
        testResult.failures = 0;
        List<String> successList = new ArrayList<>();
        successList.add(test);
        testResult.setSuccessTest(successList);

        int seed = SEED_RANDOM.nextInt() & Integer.MAX_VALUE;

        List<String> command = buildCommand(envOS, seed, newClasspath, test);

        try {
            File ftemp = File.createTempFile("idflakies-out", ".txt");

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
                log.info("[IDFlakies] Test " + test + " failed " + testResult.failures + " times of " + K + " iterations.\n");
            } finally {
                ftemp.delete();
            }

            p.destroyForcibly();

        } catch (IOException | InterruptedException e) {
            testResult.failures += K;
        }

        log.info("[IDFlakies] Done — failures=" + testResult.failures + "/" + K);
        return testResult;
    }


    protected List<String> buildCommand(String envOS, int seed, String classpath, String test) {
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

        String pl = ConfigurationProperties.getProperty("mavenmodule");
        if (pl != null && !pl.isEmpty()) {
            cmd.add("-pl");
            cmd.add(pl);
            cmd.add("--also-make");
        }

        cmd.add("--quiet"); 
        cmd.add("--batch-mode");
        
        cmd.add("edu.illinois.cs:idflakies-maven-plugin:2.0.0:detect");
        cmd.add("-Ddetector.detector_type=random-class-method");
        cmd.add("-Ddt.randomize.rounds=" + K);
        cmd.add("-Ddt.detector.original_order.all_must_pass=false");        
        return cmd;
    }

    protected int parseOutput(BufferedReader reader, String test) throws IOException {
        String line;
        int timesFailed = 0;
        while ((line = reader.readLine()) != null) {
            if (line.contains("<<< FAILURE! - in")) {
                timesFailed++;
            }
        }
        return timesFailed;
    }
}
