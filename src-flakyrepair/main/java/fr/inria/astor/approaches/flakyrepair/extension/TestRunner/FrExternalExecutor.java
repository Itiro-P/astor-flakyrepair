package fr.inria.astor.approaches.flakyrepair.extension.TestRunner;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.InitializationError;

import fr.inria.astor.core.validation.junit.filters.TestFilter;


public class FrExternalExecutor {
	List<String> successTest = new ArrayList<String>();

	List<String> failTest = new ArrayList<String>();

	public final static String OUTSEP = "astoroutdel";

	public String createOutput(Result r) {
		String out = "[";
		int count = 0;
		int failures = 0;
		try {
			for (Failure f : r.getFailures()) {
				String s = failureMessage(f);
				if (!s.startsWith("warning")) {
					failures++;
				}
				out += s + "-,";
				count++;
				if (count > 10) {
					out += "...and " + (r.getFailureCount() - 10) + " failures more,";
					// break;
				}
			}
		} catch (Exception e) {
			// We do not care about this exception,
		}
		out = out + "]";
		return (OUTSEP + r.getRunCount() + OUTSEP + failures + OUTSEP + out + OUTSEP);
	}

	protected String failureMessage(Failure f) {
		try {
			return f.toString();
		} catch (Exception e) {
			return f.getTestHeader();
		}
	}

	public static void main(String[] arg) throws Exception, InitializationError {

		FrExternalExecutor re = new FrExternalExecutor();

		Result result = re.run(arg);
		// This sysout is necessary for the communication between process...
		System.out.println(re.createOutput(result));

		System.exit(0);
	}

	public Result run(String[] arg) throws Exception {
		PrintStream original = System.out;
		System.setOut(new PrintStream(new OutputStream() {
			public void write(int b) {
			}
		}));
		System.setErr(new PrintStream(new OutputStream() {
			public void write(int b) {
			}
		}));
        String[] args = arg[0].split("#");
		System.out.println(args[0] + " " + args[1]);
		JUnitCore runner = new JUnitCore();
		Logger.getGlobal().setLevel(Level.OFF);
        Request req = Request.method(Class.forName(args[0]), args[1]);
		Result resultjUnit = runner.run(req);
		System.setOut(original);

		return resultjUnit;
	}

	protected List<Class> getClassesToRun(String[] arg) throws ClassNotFoundException {
		TestFilter tf = new TestFilter();
		List<Class> classes = new ArrayList<Class>();
		for (int i = 0; i < arg.length; i++) {
			String classString = arg[i];
			Class c = Class.forName(classString);
			if (tf.acceptClass(c)) {
				if (!classes.contains(c)) {
					System.out.println("We accept : " + classString);

					classes.add(c);
				}
			} else
				System.out.println("We discart : " + classString);

		}
		return classes;
	}
}