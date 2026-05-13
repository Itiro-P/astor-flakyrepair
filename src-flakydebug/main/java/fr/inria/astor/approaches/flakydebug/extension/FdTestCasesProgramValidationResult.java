package fr.inria.astor.approaches.flakydebug.extension;

import fr.inria.astor.core.validation.results.TestCasesProgramValidationResult;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FdTestCasesProgramValidationResult  extends TestCasesProgramValidationResult {

	int numberFailingTestCases = 0;
	int numberPassingTestCases = 0;

	boolean regressionExecuted = false;
	boolean resultSucess = false;

	/**
	 * Indicates whether where were a problem during the execution that stop
	 * finishing the complete execution , example Infinite loop
	 **/
	boolean executionError = false;

	FdTestResult testResult;

	public FdTestCasesProgramValidationResult(FdTestResult result) {
		super(result);
		setTestResult(result);
	}

	public FdTestCasesProgramValidationResult(boolean errorExecution) {
		super(errorExecution);
		this.executionError = errorExecution;
		this.testResult = null;
		this.regressionExecuted = false;
		this.resultSucess = false;
		this.numberFailingTestCases = 0;
		this.numberPassingTestCases = 0;
	}

	public FdTestCasesProgramValidationResult(FdTestResult result, boolean resultSucess, boolean regressionExecuted) {
		this(result);
		this.regressionExecuted = regressionExecuted;
		this.resultSucess = resultSucess;
	}

	public boolean isSuccessful() {

		return numberFailingTestCases >= 0 && this.resultSucess;
	}

	public int getFailureCount() {

		return numberFailingTestCases;
	}

	public boolean isRegressionExecuted() {
		return regressionExecuted;
	}

	public void setRegressionExecuted(boolean regressionExecuted) {
		this.regressionExecuted = regressionExecuted;
	}

	public int getPassingTestCases() {
		return numberPassingTestCases;
	}

	public String toString() {
		return printTestResult(this.getTestResult());
	}

	public FdTestResult getTestResult() {
		return testResult;
	}

	public void setTestResult(FdTestResult result) {
		this.testResult = result;
		if (result != null) {
			numberPassingTestCases = result.casesExecuted - result.failures;
			numberFailingTestCases = result.failures;
			resultSucess = (result.casesExecuted == result.failures);
		}
	}

	protected String printTestResult(FdTestResult result) {
		if (this.executionError || (result == null)) {
			return "|" + false + "|" + 0 + "|" + 0 + "|" + "[]" + "|";
		}
	
		return "|" + result.wasSuccessful() + "|" + result.failures + "|" + result.casesExecuted + "|" + result.failTest
				+ "|";
	}

	@Override
	public int getCasesExecuted() {

		return getPassingTestCases() + getFailureCount();
	}

	public boolean isExecutionError() {
		return executionError;
	}

}
