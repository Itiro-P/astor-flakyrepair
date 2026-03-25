package fr.inria.astor.approaches.flakyrepair;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.flakyrepair.extension.FrFaultLocalization;
import fr.inria.astor.approaches.flakyrepair.extension.FrFitnessFunction;
import fr.inria.astor.approaches.flakyrepair.extension.FrInvocationFixProcessor;
import fr.inria.astor.approaches.flakyrepair.extension.FrOperatorSpace;
import fr.inria.astor.approaches.flakyrepair.extension.FrProcessValidator;
import fr.inria.astor.approaches.flakyrepair.extension.FrVariantFactory;
import fr.inria.astor.approaches.jmutrepair.jMutRepairExhaustive;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.manipulation.filters.TargetElementProcessor;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.main.AstorOutputStatus;
import fr.inria.main.evolution.ExtensionPoints;

public class FlakyRepairEngine extends jMutRepairExhaustive {
    public FlakyRepairEngine(MutationSupporter mutationExecutor, ProjectRepairFacade projFacade) throws JSAPException {
        super(mutationExecutor, projFacade);
		ConfigurationProperties.properties.setProperty("population", "1");
        /**
         * Using FR's FL
         */
        ConfigurationProperties.setProperty(ExtensionPoints.FAULT_LOCALIZATION.identifier, FrFaultLocalization.class.getCanonicalName());

        /**
         * Processing patches by re-executing them.
         */
        ConfigurationProperties.setProperty(ExtensionPoints.VALIDATION.identifier, FrProcessValidator.class.getCanonicalName());
        ConfigurationProperties.setProperty("canhavezerosusp", Boolean.TRUE.toString());
        ConfigurationProperties.setProperty("includeTestInSusp", Boolean.TRUE.toString());

        /**
         * By default, the processor's space of jMutRepair are if conditions and return statements.
         * We set the default behaviour of Astor: statements granularity.
         */
        //ConfigurationProperties.setProperty(ExtensionPoints.TARGET_CODE_PROCESSOR.identifier, "statements");
        /*
         * Validation using O. Parry's flakiness formula.
         */
        ConfigurationProperties.setProperty(ExtensionPoints.FITNESS_FUNCTION.identifier, FrFitnessFunction.class.getCanonicalName());

        ConfigurationProperties.setProperty(ExtensionPoints.OPERATORS_SPACE.identifier, FrOperatorSpace.class.getCanonicalName());

        ConfigurationProperties.setProperty(ExtensionPoints.TARGET_CODE_PROCESSOR.identifier, FrInvocationFixProcessor.class.getCanonicalName());

        /**
         * Changing output to represent the results in a more representative way.
         */
        //ConfigurationProperties.setProperty(ExtensionPoints.OUTPUT_RESULTS.identifier, null);
    }

    @Override
    protected void loadTargetElements() throws Exception {

		ExtensionPoints extensionPointpoint = ExtensionPoints.TARGET_CODE_PROCESSOR;

		List<TargetElementProcessor<?>> loadedTargetElementProcessors = loadTargetElements(extensionPointpoint);

		this.setTargetElementProcessors(loadedTargetElementProcessors);
		this.setVariantFactory(new FrVariantFactory(this.getTargetElementProcessors()));
	}

    @Override
	public void startSearch() throws Exception {

		dateInitEvolution = new Date();
		// We don't evolve variants, so the generation is always one.
		generationsExecuted = 1;
		// For each variant (one is enough)
		int maxMinutes = ConfigurationProperties.getPropertyInt("maxtime");

		int v = 0;
		for (ProgramVariant parentVariant : variants) {

			log.debug("\n****\nanalyzing variant #" + (++v) + " out of " + variants.size());
			// We analyze each modifpoint of the variant i.e. suspicious
			// statement
			for (ModificationPoint modifPoint : parentVariant.getModificationPoints()) {
				// We create all operators to apply in the modifpoint
				List<OperatorInstance> operatorInstances = createInstancesOfOperators(
						(SuspiciousModificationPoint) modifPoint);

				if (operatorInstances == null || operatorInstances.isEmpty())
					continue;

				for (OperatorInstance pointOperation : operatorInstances) {

					if (!belowMaxTime(dateInitEvolution, maxMinutes)) {

						this.setOutputStatus(AstorOutputStatus.TIME_OUT);
						log.debug("Max time reached");
						return;
					}

					try {
						log.info("mod_point " + modifPoint);
						log.info("-->op: " + pointOperation);
					} catch (Exception e) {
						log.error(e);
					}

					// We validate the variant after applying the operator
					ProgramVariant solutionVariant = variantFactory.createProgramVariantFromAnother(parentVariant, generationsExecuted);
					solutionVariant.getOperations().put(generationsExecuted, Arrays.asList(pointOperation));

					applyNewMutationOperationToSpoonElement(pointOperation);

					boolean solution = processCreatedVariant(solutionVariant, generationsExecuted);

					// We undo the operator (for try the next one)
					undoOperationToSpoonElement(pointOperation);

					if (solution) {
						this.solutions.add(solutionVariant);

						this.savePatch(solutionVariant);

						if (ConfigurationProperties.getPropertyBool("stopfirst")) {
							this.setOutputStatus(AstorOutputStatus.STOP_BY_PATCH_FOUND);
							return;
						}
					}

					if (!belowMaxTime(dateInitEvolution, maxMinutes)) {

						this.setOutputStatus(AstorOutputStatus.TIME_OUT);
						log.debug("Max time reached");
						return;
					}
				}
			}
		}
		log.debug("End exhaustive navigation");

		this.setOutputStatus(AstorOutputStatus.EXHAUSTIVE_NAVIGATED);
	}

    @Override
    protected void initializePopulation(List<SuspiciousCode> suspicious) throws Exception {

		variantFactory.setMutatorExecutor(getMutatorSupporter());

		this.variants = variantFactory.createInitialPopulation(suspicious,
				ConfigurationProperties.getPropertyInt("population"), projectFacade);

		if (variants.isEmpty()) {
			throw new IllegalArgumentException("Any variant created from list of suspicious");
		}
		// We save the first variant
		this.originalVariant = variants.get(0);

		if (originalVariant.getModificationPoints().isEmpty()) {
			// throw new IllegalStateException("Variant without any modification point. It
			// must have at least one.");
			log.error("[warning] Any modification point in variant");
		}
	}
}
