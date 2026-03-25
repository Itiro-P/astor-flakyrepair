package fr.inria.astor.approaches.flakyrepair.extension.TestRunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import fr.inria.astor.approaches.tos.core.MetaGenerator;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectConfiguration;
import fr.inria.astor.core.validation.results.TestResult;

public class TestLauncher {
	private static final int K = 10;
    protected Logger log = Logger.getLogger(Thread.currentThread().getName());
    boolean outputInFile = ConfigurationProperties.getPropertyBool("processoutputinfile");
	boolean avoidInterruption = false;

    public TestLauncher(boolean avoidInterruption) {
		super();
		this.avoidInterruption = avoidInterruption;
	}

	public TestLauncher() {
		super();
		this.avoidInterruption = false;
	}

	public TestResult execute(String jvmPath, URL[] classpath, List<String> testsToExecute, int waitTime) {
		String envOS = System.getProperty("os.name");
		String timeZone = ConfigurationProperties.getProperty("timezone");
		UUID procWinUUID = null;
		TestResult res = null;

		String newJvmPath = jvmPath + File.separator + "java";
		String newClasspath = urlArrayToString(classpath);
		if (ConfigurationProperties.getPropertyBool("runjava7code") || ProjectConfiguration.isJDKLowerThan8()) {
			newClasspath = (new File(ConfigurationProperties.getProperty("executorjar")).getAbsolutePath())
					+ File.pathSeparator + newClasspath;
		}

		try {
			List<String> baseCommand = new ArrayList<String>();
			baseCommand.add("\"" + newJvmPath + "\"");
			baseCommand.add("-Xmx2048m");

			String[] ids = ConfigurationProperties.getProperty(MetaGenerator.METALL).split(File.pathSeparator);
			for (String mutid : ids) {
				baseCommand.add("-D" + MetaGenerator.MUT_IDENTIFIER + mutid + "="
						+ ConfigurationProperties.getProperty(MetaGenerator.MUT_IDENTIFIER + mutid));
			}
			if (envOS.contains("Windows")) {
				procWinUUID = UUID.randomUUID();
				baseCommand.add("-DwinProcUUID=" + procWinUUID);
				System.setProperty("user.timezone", timeZone);
			}
			String test = testsToExecute.get(0);
			baseCommand.add("-cp");
			baseCommand.add("\"" + newClasspath + "\"");
			baseCommand.add(laucherClassName().getCanonicalName());
			List<String> command = new ArrayList<String>(baseCommand);
			TestResult testResult = new TestResult();
			testResult.casesExecuted = K;
			List<String> t = new ArrayList<String>(); 
			t.add(test);
			testResult.setSuccessTest(t);

			command.add(test);
			ProcessBuilder pb;
			if (!envOS.contains("Windows")) {
				printCommandToExecute(command, waitTime);
				pb = new ProcessBuilder("/bin/bash");
			} else {
				command.set(0, "'" + newJvmPath + "'");
				command.set(5, "'" + newClasspath + "'");
				pb = new ProcessBuilder("powershell", "-Command", "& " + toString(command));
			}

			pb.redirectErrorStream(true);
			pb.directory(new File(ConfigurationProperties.getProperty("location")));

			for (int i = 1; i <= K; i++) {
				File ftemp = File.createTempFile("out", "txt");
				TestResult curTest = null;
				Process p = null;
				pb.redirectOutput(ftemp);

				log.info("Running iteration " + i + " of test " + test);
				try {
					long t_start = System.currentTimeMillis();
					p = pb.start();

					BufferedWriter p_stdin = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
					try {
						if (!envOS.contains("Windows")) {
							p_stdin.write("TZ=\"" + timeZone + "\"");
							p_stdin.newLine(); p_stdin.flush();
							p_stdin.write("export TZ");
							p_stdin.newLine(); p_stdin.flush();
							p_stdin.write("echo $TZ");
							p_stdin.newLine(); p_stdin.flush();
							p_stdin.write(toString(command));
							p_stdin.newLine(); p_stdin.flush();
						}
						p_stdin.write("exit");
						p_stdin.newLine(); p_stdin.flush();
					} catch (IOException e) {
						log.error(e);
					}

					if (!p.waitFor(waitTime, TimeUnit.MILLISECONDS)) {
						killProcess(p, waitTime, procWinUUID);
						continue;
					}

					long t_end = System.currentTimeMillis();
					log.debug("Execution time " + ((t_end - t_start) / 1000) + "seconds");

					if (!avoidInterruption) {
						p.exitValue();
					}

					BufferedReader output = new BufferedReader(new FileReader(ftemp.getAbsolutePath()));;
					curTest = getTestResult(output);
					p.destroyForcibly();
				} catch (IOException | IllegalThreadStateException | InterruptedException ex) {
					log.info("The Process that runs JUnit test cases had problems: " + ex.getMessage());
					ftemp.delete();
					killProcess(p, waitTime, procWinUUID);
					continue;
				}
				if (curTest == null) continue;
				log.info("\nFailures: " + curTest.failures);
				if(curTest.failures > 0) {
					testResult.failures += curTest.failures;
				}
			}
			res = testResult;
		} catch (IOException ex) {
			log.info("The Process that runs JUnit test cases had problems: " + ex.getMessage());
		}
		return res;
	}

    /**
	 * Workarrond. I will be solved when migrating to java 9.
	 * https://docs.oracle.com/javase/9/docs/api/java/lang/Process.html#descendants--
	 * 
	 * @param waitTime
	 */
	private void killProcess(Process p, int waitTime, UUID procWinUUID) {
		if (p == null)
			return;

		Object pid = null;
		try {
			if (procWinUUID != null) {
				Process survivedPID = Runtime.getRuntime()
						.exec("wmic process where \"commandline like '%-DwinProcUUID=" + procWinUUID
								+ "%' and name like '%java.exe%'\" get processid");
				BufferedReader outputSurvivedPIDs = new BufferedReader(
						new InputStreamReader(survivedPID.getInputStream()));
				String line;
				int i = 0;
				while ((line = outputSurvivedPIDs.readLine()) != null) {
					if (i == 2 && !line.isEmpty()) {
						pid = line.trim();
						break;
					}
					i++;
				} 
			} else {
				Field f = p.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				pid = f.get(p);
			}
		} catch (IOException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			log.error(e);
		}

		p.destroyForcibly();
		log.info("The Process that runs JUnit test cases did not terminate within waitTime of "
				+ TimeUnit.MILLISECONDS.toSeconds(waitTime) + " seconds");
		log.info("Killing the Process that runs JUnit test cases " + pid);

		// workarrond!!
		if (ConfigurationProperties.getPropertyBool("forcesubprocesskilling")) {
			Integer subprocessid = Integer.valueOf(pid.toString()) + 1;
			try {
				Process process;
				if (procWinUUID != null) {
					log.error("Killing Windows process " + pid);
					process = Runtime.getRuntime().exec("taskkill /T /F /PID " + pid);
				} else {
					log.debug("Killing subprocess " + subprocessid);
					process = new ProcessBuilder(new String[] { "kill", subprocessid.toString() }).start();
				}
				process.waitFor();
			} catch (IOException | InterruptedException e) {
				if (procWinUUID != null) 
					log.error("Problems killing Windows process " + pid);
				else
					log.error("Problems killing subprocess " + subprocessid);
				log.error(e);
			}
		}
	}

	protected String urlArrayToString(URL[] urls) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < urls.length; i++) {
			if (i > 0) s.append(File.pathSeparator);
			s.append(urls[i].getPath());
		}
		return s.toString();
	}

	protected String getProcessError(InputStream str) {
		String out = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(str));
			String line;
			while ((line = in.readLine()) != null) {
				out += line + "\n";
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	private void printCommandToExecute(List<String> command, int waitTime) {
		String commandString = toString(command);
		int trunk = ConfigurationProperties.getPropertyInt("commandTrunk");
		String commandToPrint = (trunk != 0 && commandString.length() > trunk)
				? (commandString.substring(0, trunk) + "..AND " + (commandString.length() - trunk) + " CHARS MORE...")
				: commandString;
		log.debug("Executing process: (timeout" + waitTime / 1000 + "secs) \n" + commandToPrint);
	}

	private String toString(List<String> command) {
		String commandString = command.toString().replace("[", "").replace("]", "").replace(",", " ");
		return commandString;
	}

	public Class<?> laucherClassName() {
		return FrExternalExecutor.class;

	}

    	/**
	 * This method analyze the output of the junit executor (i.e.,
	 * {@link JUnitTestExecutor}) and return an entity called TestResult with the
	 * result of the test execution
	 *
	 * @param p
	 * @return
	 */
	protected TestResult getTestResult(BufferedReader in) {
		log.debug("Analyzing output from process");
		TestResult tr = new TestResult();
		boolean success = false;
		String processOut = "";
		try {
			String line;
			while ((line = in.readLine()) != null) {
				processOut += line + "\n";
				if (line.startsWith(FrExternalExecutor.OUTSEP)) {
					String[] resultPrinted = line.split(FrExternalExecutor.OUTSEP);
					int nrtc = Integer.valueOf(resultPrinted[1]);
					tr.casesExecuted = nrtc;
					int nrfailing = Integer.valueOf(resultPrinted[2]);
					tr.failures = nrfailing;
					if (resultPrinted.length > 3 && !"".equals(resultPrinted[3])) {
						String[] failingTestList = resultPrinted[3].replace("[", "").replace("]", "").split(",");
						for (String failingTest : failingTestList) {
							failingTest = failingTest.trim();
							if (!failingTest.isEmpty() && !failingTest.equals("-")) tr.failTest.add(failingTest);
						}
					}
					success = true;
				}
			}
			// log.debug("Process output:\n"+ out);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (success)
			return tr;
		else {
			log.error("Error reading the validation process\n output: \n" + processOut);
			return null;
		}
	}
}
