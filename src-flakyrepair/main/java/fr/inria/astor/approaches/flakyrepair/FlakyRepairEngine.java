package fr.inria.astor.approaches.flakyrepair;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.flakyrepair.extension.FlakyRepairFaultLocalization;
import fr.inria.astor.approaches.flakyrepair.extension.FlakyRepairProcessValidator;
import fr.inria.astor.approaches.jmutrepair.jMutRepairExhaustive;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.main.AstorOutputStatus;
import fr.inria.main.evolution.ExtensionPoints;

public class FlakyRepairEngine extends jMutRepairExhaustive {
    public FlakyRepairEngine(MutationSupporter mutationExecutor, ProjectRepairFacade projFacade) throws JSAPException {
        super(mutationExecutor, projFacade);

        /**
         * Using FR's FL
         */
        ConfigurationProperties.setProperty(ExtensionPoints.FAULT_LOCALIZATION.identifier, FlakyRepairFaultLocalization.class.getCanonicalName());

        /**
         * Processing patches by re-executing them.
         */
        ConfigurationProperties.setProperty(ExtensionPoints.VALIDATION.identifier, FlakyRepairProcessValidator.class.getCanonicalName());

        /**
         * By default, the processor's space of jMutRepair are if conditions and return statements.
         * We set the default behaviour of Astor: statements granularity.
         */
        ConfigurationProperties.setProperty(ExtensionPoints.TARGET_CODE_PROCESSOR.identifier, "statements");
        /*
         * Validation using O. Parry's flakiness formula.
         */
        //ConfigurationProperties.setProperty(ExtensionPoints.FITNESS_FUNCTION.identifier, null);

        /**
         * Changing output to represent the results in a more representative way.
         */
        //ConfigurationProperties.setProperty(ExtensionPoints.OUTPUT_RESULTS.identifier, null);
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
					ProgramVariant solutionVariant = variantFactory.createProgramVariantFromAnother(parentVariant,
							generationsExecuted);
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
}
