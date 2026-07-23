package fr.inria.astor.approaches.flakydebug.extension;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.astor.approaches.flakyseeding.utils.ShuffledMap;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.validation.TestCaseVariantValidationResult;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectConfiguration;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.validation.junit.JUnitProcessValidator;
import fr.inria.astor.core.validation.results.TestResult;
import fr.inria.astor.util.Converters;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;


public abstract class FdProcessValidator<
    L extends FdTestLauncher<R>, 
    R extends TestResult, 
    V extends TestCaseVariantValidationResult> extends JUnitProcessValidator {
    
    protected Logger log = Logger.getLogger(Thread.currentThread().getName());

    // Método abstrato para criar o Launcher específico da subclasse
    protected abstract L createTestLauncher();

    // Método abstrato para processar o resultado e retornar a subclasse de V
    protected abstract V createValidationResult(R result, boolean wasSuccessful);

    /**
	 * Run the validation of the program variant by using re-executions.
	 * 
	 * @param mutatedVariant
	 * @param projectFacade
	 * @param forceExecuteRegression
	 * @return
	 */
	public TestCaseVariantValidationResult validate(ProgramVariant mutatedVariant, ProjectRepairFacade projectFacade) {
		try {
			ProjectConfiguration projConfig = projectFacade.getProperties();
			List<URL> deps = projConfig.getDependencies();
			String astorJar = ShuffledMap.class
				.getProtectionDomain()
				.getCodeSource()
				.getLocation()
				.getPath();
			deps.add(new URL("file://" + astorJar));
			log.debug("astorJar path: " + astorJar);
			projConfig.setDependencies(deps);
			projectFacade.setProperties(projConfig);

			URL[] bc = this.createClassPath(mutatedVariant, projectFacade);

			L testProcessRunner = createTestLauncher();

			log.debug("-Running first validation");

			String jvmPath = ConfigurationProperties.getProperty("jvm4testexecution");

			List<String> tests = new ArrayList<>();
			for(OperatorInstance op: mutatedVariant.getAllOperations()) {
				ModificationPoint mp = op.getModificationPoint();
				CtClass<?> ctClass = mp.getCtClass();
				
				// Se for abstrata, busca subclasses concretas
				if(ctClass.isAbstract()) {
					MutationSupporter.getFactory().Class().getAll().stream()
						.filter(t -> t instanceof CtClass)
						.map(t -> (CtClass<?>) t)
						.filter(c -> !c.isAbstract())
						.filter(c -> c.getSuperclass() != null && 
								c.getSuperclass().getQualifiedName().equals(ctClass.getQualifiedName()))
						.forEach(c -> tests.add(c.getQualifiedName() + "#" + this.getMethodName(mp)));
				} else {
					tests.add(ctClass.getQualifiedName() + "#" + this.getMethodName(mp));
				}
			}
			if (tests.isEmpty()) {
				return null;	
			}
            R trfailing = (R) testProcessRunner.execute(jvmPath, bc, new ArrayList<>(new LinkedHashSet<>(tests)), ConfigurationProperties.getPropertyInt("tmax1"));
			if (trfailing == null) {
				log.debug("**The validation 1 have not finished well**");
				return null;
			}

			log.debug(trfailing);

			super.removeOfCompiledCode(mutatedVariant, projectFacade);
			return createValidationResult(trfailing, trfailing.wasSuccessful());

		} catch (MalformedURLException e) {
			removeOfCompiledCode(mutatedVariant, projectFacade);
			e.printStackTrace();
			return null;
		}

		// WE REMOVE THE bin code generated for validating the variant
	}

    @Override
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

		String astorJar = ShuffledMap.class
			.getProtectionDomain()
			.getCodeSource()
			.getLocation()
			.getPath();

		if (!isGZoltarDependencyFound) {
			File libsfolder = new File("." + File.separator + "lib");
			URL[] newBc = new URL[bc.length + 2];
			newBc[0] = new URL("file://" + libsfolder.getAbsolutePath() + File.separator
				+ "com.gzoltar-0.1.1-jar-with-dependencies.jar");
			newBc[1] = new URL("file://" + astorJar);
			for (int i = 0; i < bc.length; i++) {
				newBc[i + 2] = bc[i];
			}
			return newBc;
		}

		URL[] newBc = new URL[bc.length + 1];
		newBc[0] = new URL("file://" + astorJar);
		for (int i = 0; i < bc.length; i++) {
			newBc[i + 1] = bc[i];
		}

		return newBc;
	}


	private String getMethodName(ModificationPoint mp) {
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
}
