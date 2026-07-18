package fr.inria.astor.approaches.flakyrepair;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.flakydebug.FdEngine;
import fr.inria.astor.approaches.flakyrepair.extension.FrFaultLocalization;
import fr.inria.astor.approaches.flakyrepair.extension.FrFitnessFunction;
import fr.inria.astor.approaches.flakyrepair.extension.FrProcessValidator;
import fr.inria.astor.approaches.flakyrepair.extension.FrRepairSpace;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.main.evolution.ExtensionPoints;

public class FrEngine extends FdEngine {
	public FrEngine(MutationSupporter mutationExecutor, ProjectRepairFacade projFacade) throws JSAPException {
        super(mutationExecutor, projFacade);

        /**
         * Using FR's FL
         */
        ConfigurationProperties.setProperty(ExtensionPoints.FAULT_LOCALIZATION.identifier, FrFaultLocalization.class.getCanonicalName());

        /**
         * Processing patches by re-executing them.
         */
        ConfigurationProperties.setProperty(ExtensionPoints.VALIDATION.identifier, FrProcessValidator.class.getCanonicalName());
        /*
         * Validation using O. Parry's flakiness formula.
         */
        ConfigurationProperties.setProperty(ExtensionPoints.FITNESS_FUNCTION.identifier, FrFitnessFunction.class.getCanonicalName());

        ConfigurationProperties.setProperty(ExtensionPoints.OPERATORS_SPACE.identifier, FrRepairSpace.class.getCanonicalName());
    }
}