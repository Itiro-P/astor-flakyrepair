package fr.inria.astor.approaches.flakydebug.extension.testRunner.runners;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fr.inria.astor.approaches.flakydebug.extension.FdTestResult;
import fr.inria.astor.core.setup.ConfigurationProperties;

public class JUnitRunner extends Runner {
    public JUnitRunner() {
        super();
    }

    public FdTestResult execute(String jvmPath, URL[] classpath, List<String> testsToExecute, int waitTime) {
        String envOS     = System.getProperty("os.name");
        boolean isWindows = envOS != null && envOS.toLowerCase().contains("win");
        String timeZone  = ConfigurationProperties.getProperty("timezone");
        String location  = ConfigurationProperties.getProperty("location");

        String newClasspath = urlArrayToString(classpath);
        String test = testsToExecute.get(0);

        log.info("[JUnit] Running: " + test + " | K=" + K + " executions");

        this.prepareMutatedEnvironment(newClasspath, location);

        List<String> command = buildMavenCommand(test, location);
        FdTestResult testResult = new FdTestResult();
        testResult.casesExecuted = K;
        testResult.failures = 0;

        for (int i = 0; i < K; i++) {
            try {
                File ftemp = File.createTempFile("junit-out", ".txt");
                ProcessBuilder pb;

                if (isWindows) {
                    // No Windows, chama o PowerShell direto com o comando
                    pb = new ProcessBuilder("powershell", "-Command", commandToString(command));
                } else {
                    // No Linux/Mac, usa o bash -c para injetar as variáveis de ambiente de forma limpa
                    String bashCmd = String.format("export TZ=\"%s\" && %s", timeZone, commandToString(command));
                    pb = new ProcessBuilder("/bin/bash", "-c", bashCmd);
                }

                pb.redirectErrorStream(true);
                pb.redirectOutput(ftemp);
                pb.directory(new File(location));

                printCommandToExecute(command, waitTime);
                Process p = pb.start();

                // Só interage com o stdin se NÃO for Windows e se necessário (neste caso, 'bash -c' elimina a necessidade)
                // Mantemos o fechamento do stdin para garantir que o processo não mude de comportamento
                try {
                    p.getOutputStream().close(); 
                } catch (IOException e) {
                    // Ignora se já estiver fechado
                }
                
                boolean finished = p.waitFor(waitTime, TimeUnit.MILLISECONDS);
                int exitCode = 0;

                if (!finished) {
                    log.info("Test exceeded wait time.\n");
                    p.destroyForcibly();
                    p.waitFor();
                    exitCode = 1;
                } else {
                    exitCode = p.exitValue();
                }

                if (exitCode != 0) {
                    testResult.failures++;
                }
                
                ftemp.delete();

            } catch (IOException | InterruptedException e) {
                log.error("[JUnit] Execution " + (i+1) + " error", e);
                testResult.failures++;
            }
        }
        List<String> successList = new ArrayList<>();
        successList.add(test);
        testResult.setSuccessTest(successList);

        log.info("[JUnit] Done — failures=" + testResult.failures + "/" + K);
        return testResult;
    }

    /**
     * Cópia de arquivos para fora do loop de execução.
     */
    protected void prepareMutatedEnvironment(String classpath, String location) {
        String[] cs = classpath.split(File.pathSeparator);
        String firstPath = cs[0];
        boolean foundVariant = false;

        for (String c : cs) {
            if (c.contains(File.separator + "bin" + File.separator + "variant-")) {
                firstPath = c;
                foundVariant = true;
                break;
            }
        }

        if (foundVariant) {
            File mutatedFolder = new File(firstPath);
            for (String target : new String[]{"/target/test-classes", "/target/classes"}) {
                File dest = new File(location + target);
                try {
                    org.apache.commons.io.FileUtils.copyDirectory(mutatedFolder, dest);
                } catch (IOException e) {
                    log.error("Failed to copy mutated folder: " + e.getMessage());
                }
            }
            try {
                File srcDir  = new File("target/classes/fr/inria/astor/approaches/flakydebug/extension/operators/utils/");
                File destDir = new File(location + "/target/test-classes/fr/inria/astor/approaches/flakydebug/extension/operators/utils/");
                org.apache.commons.io.FileUtils.copyDirectory(srcDir, destDir);
            } catch (IOException e) {
                log.error("Failed to copy utils package: " + e.getMessage());
            }
        }
    }

    /**
     * Apenas gera a lista de argumentos do Maven.
     */
    protected List<String> buildMavenCommand(String test, String location) {
        List<String> cmd = new ArrayList<>();
        String pomPath = location + "/pom.xml";

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