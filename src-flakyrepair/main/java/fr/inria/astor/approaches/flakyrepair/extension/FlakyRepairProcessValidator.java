package fr.inria.astor.approaches.flakyrepair.extension;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import fr.inria.astor.approaches.flakyrepair.extension.TestRunner.TestLauncher;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.validation.TestCaseVariantValidationResult;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.FinderTestCases;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.validation.ProgramVariantValidator;
import fr.inria.astor.core.validation.results.TestCasesProgramValidationResult;
import fr.inria.astor.core.validation.results.TestResult;
import fr.inria.astor.util.Converters;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

public class FlakyRepairProcessValidator extends ProgramVariantValidator {
    protected Logger log = Logger.getLogger(Thread.currentThread().getName());

	@Override
	public TestCaseVariantValidationResult validate(ProgramVariant mutatedVariant, ProjectRepairFacade projectFacade) {
		return this.validate(mutatedVariant, projectFacade,
				Boolean.valueOf(ConfigurationProperties.getProperty("forceExecuteRegression")));

	}

	/**
	 * Run the validation of the program variant by using re-executions.
	 * 
	 * @param mutatedVariant
	 * @param projectFacade
	 * @param forceExecuteRegression
	 * @return
	 */
	public TestCaseVariantValidationResult validate(ProgramVariant mutatedVariant, ProjectRepairFacade projectFacade,
			boolean forceExecuteRegression) {
		try {
			URL[] bc = createClassPath(mutatedVariant, projectFacade);

			TestLauncher testProcessRunner = new TestLauncher();

			log.debug("-Running first validation");

			String jvmPath = ConfigurationProperties.getProperty("jvm4testexecution");

			List<String> tests = new ArrayList<>();
			for(ModificationPoint mp: mutatedVariant.getModificationPoints()) {
				CtClass<?> ctClass = mp.getCtClass();
				
				// Se for abstrata, busca subclasses concretas
				if(ctClass.isAbstract()) {
					MutationSupporter.getFactory().Class().getAll().stream()
						.filter(t -> t instanceof CtClass)
						.map(t -> (CtClass<?>) t)
						.filter(c -> !c.isAbstract())
						.filter(c -> c.getSuperclass() != null && 
								c.getSuperclass().getQualifiedName().equals(ctClass.getQualifiedName()))
						.forEach(c -> tests.add(c.getQualifiedName() + "#" + getMethodName(mp)));
				} else {
					tests.add(ctClass.getQualifiedName() + "#" + getMethodName(mp));
				}
			}
			TestResult trfailing = testProcessRunner.execute(jvmPath, bc, new ArrayList<String>(new HashSet<String>(tests)), ConfigurationProperties.getPropertyInt("tmax1"));

			if (trfailing == null) {
				log.debug("**The validation 1 have not finished well**");
				return null;
			}

			log.debug(trfailing);
			TestCaseVariantValidationResult r = new TestCasesProgramValidationResult(trfailing, trfailing.wasSuccessful(), false);
			removeOfCompiledCode(mutatedVariant, projectFacade);
			return r;

		} catch (MalformedURLException e) {
			removeOfCompiledCode(mutatedVariant, projectFacade);
			e.printStackTrace();
			return null;
		}

		// WE REMOVE THE bin code generated for validating the variant

	}

	protected URL[] createClassPath(ProgramVariant mutatedVariant, ProjectRepairFacade projectFacade)
			throws MalformedURLException {

		List<URL> originalURL = createOriginalURLs(projectFacade);
		URL[] bc;
		if (mutatedVariant.getCompilation() != null) {

			File variantOutputFile = defineLocationOfCompiledCode(mutatedVariant, projectFacade);

			bc = Converters.redefineURL(variantOutputFile, originalURL.toArray(new URL[0]));
		} else {
			bc = originalURL.toArray(new URL[0]);
		}

		boolean isGZoltarDependencyFound = false;

		for (int i = 0; i < bc.length && !isGZoltarDependencyFound; i++) {
			if (bc[i].getFile().contains("gzoltar-0.1.1")) {
				isGZoltarDependencyFound = true;
			}
		}

		if (!isGZoltarDependencyFound) {

			File libsfolder = new File("." + File.separator + "lib");

			URL[] newBc = new URL[bc.length + 1];
			newBc[0] = new URL("file://" + libsfolder.getAbsolutePath() + File.separator
					+ "com.gzoltar-0.1.1-jar-with-dependencies.jar");

			for (int i = 0; i < bc.length; i++) {
				newBc[i + 1] = bc[i];
			}

			return newBc;
		}

		return bc;
	}

	public List<URL> createOriginalURLs(ProjectRepairFacade projectFacade) throws MalformedURLException {
		URL[] defaultSUTClasspath = projectFacade
				.getClassPathURLforProgramVariant(ProgramVariant.DEFAULT_ORIGINAL_VARIANT);
		List<URL> originalURL = new ArrayList<>(Arrays.asList(defaultSUTClasspath));

		String classpath = System.getProperty("java.class.path");

		for (String path : classpath.split(File.pathSeparator)) {

			File f = new File(path);
			// originalURL.add(new URL("file://\"" + f.getAbsolutePath() + "\""));
			originalURL.add(new URL("file://" + f.getAbsolutePath()));

		}

		return originalURL;
	}

	protected File defineLocationOfCompiledCode(ProgramVariant mutatedVariant, ProjectRepairFacade projectFacade) {
		String bytecodeOutput = projectFacade.getOutDirWithPrefix(mutatedVariant.currentMutatorIdentifier());
		File variantOutputFile = new File(bytecodeOutput);

		MutationSupporter.currentSupporter.getOutput().saveByteCode(mutatedVariant.getCompilation(), variantOutputFile);
		return variantOutputFile;
	}

	protected void removeOfCompiledCode(ProgramVariant mutatedVariant, ProjectRepairFacade projectFacade) {
		String bytecodeOutput = projectFacade.getOutDirWithPrefix(mutatedVariant.currentMutatorIdentifier());
		File variantOutputFile = new File(bytecodeOutput);

		try {
			FileUtils.deleteDirectory(variantOutputFile);
		} catch (IOException e) {
			log.error("Cannot we removed variant BIN: " + e.getMessage());

		}

	}

	public String getMethodName(ModificationPoint mp) {
		CtElement element = mp.getCodeElement();
		
		// Sobe na árvore do Spoon até encontrar o CtMethod pai
		CtElement parent = element;
		while (parent != null) {
			if (parent instanceof CtMethod) {
				return ((CtMethod<?>) parent).getSimpleName();
			}
			parent = parent.getParent();
		}
		return null; // elemento não está dentro de um método (ex: campo, inicializador)
	}

	@Override
	public List<String> findTestCasesToExecute(ProjectRepairFacade projectFacade) {
		List<String> testCasesToRun = FinderTestCases.findJUnit4XTestCasesForRegression(projectFacade);

		return testCasesToRun;
	}
}
