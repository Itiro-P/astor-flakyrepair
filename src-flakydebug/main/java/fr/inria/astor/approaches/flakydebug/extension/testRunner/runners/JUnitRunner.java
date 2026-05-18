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

import fr.inria.astor.approaches.flakydebug.extension.FdProcessValidator;
import fr.inria.astor.approaches.flakydebug.extension.FdTestResult;
import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledMap;
import fr.inria.astor.core.setup.ConfigurationProperties;

public class JUnitRunner extends Runner {
    public JUnitRunner() {
        super();
    }

    public FdTestResult execute(String jvmPath, URL[] classpath, List<String> testsToExecute, int waitTime) {
        String envOS     = System.getProperty("os.name");
        String timeZone  = ConfigurationProperties.getProperty("timezone");
        String location  = ConfigurationProperties.getProperty("location");

        String newClasspath = urlArrayToString(classpath);

        String test = testsToExecute.get(0);

        log.info("[JUnit] Running: " + test + " | K=" + K + " seeds");

        FdTestResult testResult = new FdTestResult();
        testResult.casesExecuted = K;
        testResult.failures = 0;
        List<String> successList = new ArrayList<>();
        successList.add(test);
        testResult.setSuccessTest(successList);

        for (int i = 0; i < K; i++) {
            List<String> command = buildCommand(envOS, newClasspath, test);

            try {
                File ftemp = File.createTempFile("junit-out", ".txt");
                ProcessBuilder pb = new ProcessBuilder("/bin/bash");
                if (envOS != null && envOS.contains("Windows")) {
                    pb = new ProcessBuilder("powershell", "-Command", "& " + commandToString(command));
                }
                pb.redirectErrorStream(true);
                pb.redirectOutput(ftemp);
                pb.directory(new File(location));

                printCommandToExecute(command, waitTime);
                Process p = pb.start();
                try (BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()))) {
                    if (envOS == null || !envOS.contains("Windows")) {
                        stdin.write("TZ=\"" + timeZone + "\""); stdin.newLine(); stdin.flush();
                        stdin.write("export TZ");               stdin.newLine(); stdin.flush();
                        stdin.write(commandToString(command));  stdin.newLine(); stdin.flush();
                    }
                    stdin.write("exit"); stdin.newLine(); stdin.flush();
                } catch (IOException e) {
                    log.error(e);
                }
                
                int exitCode = 0;
                boolean finished = p.waitFor(waitTime, TimeUnit.MILLISECONDS);

                if (!finished) {
                    log.info("Test succeded wait time.\n");
                    p.destroyForcibly();
                    p.waitFor();
                    exitCode = 1;
                } else {
                    exitCode = p.exitValue();
                }

                try (BufferedReader reader = new BufferedReader(new FileReader(ftemp))) {
                    testResult.failures += (exitCode != 0) ? 1 : 0;
                } finally {
                    ftemp.delete();
                }

            } catch (IOException | InterruptedException e) {
                log.error("[JUnit] Execution " + (i+1) + " error", e);
                testResult.failures++;
            }
        }

        log.info("[JUnit] Done — failures=" + testResult.failures + "/" + K);
        return testResult;
    }


    protected List<String> buildCommand(String envOS, String classpath, String test) {
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
            String location = ConfigurationProperties.getProperty("location");

            // Copia para test-classes E classes
            for (String target : new String[]{"/target/test-classes", "/target/classes"}) {
                File dest = new File(location + target);
                try {
                    org.apache.commons.io.FileUtils.copyDirectory(mutatedFolder, dest);
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
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
        cmd.add("test");
        cmd.add("-Dtest=" + test);
        return cmd;
    }
}
