package fr.inria.astor.approaches.flakyseeding;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.flakydebug.FdEngine;
import fr.inria.astor.approaches.flakyseeding.extension.FsFaultLocalization;
import fr.inria.astor.approaches.flakyseeding.extension.FsFitnessFunction;
import fr.inria.astor.approaches.flakyseeding.extension.FsProcessValidator;
import fr.inria.astor.approaches.flakyseeding.extension.FsRepairSpace;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.main.evolution.ExtensionPoints;

public class FsEngine extends FdEngine {
    public FsEngine(MutationSupporter mutationExecutor, ProjectRepairFacade projFacade) throws JSAPException {
        super(mutationExecutor, projFacade);

        /**
         * Using FR's FL
         */
        ConfigurationProperties.setProperty(ExtensionPoints.FAULT_LOCALIZATION.identifier, FsFaultLocalization.class.getCanonicalName());

        /**
         * Processing patches by re-executing them.
         */
        ConfigurationProperties.setProperty(ExtensionPoints.VALIDATION.identifier, FsProcessValidator.class.getCanonicalName());
        /*
         * Validation using O. Parry's flakiness formula.
         */
        ConfigurationProperties.setProperty(ExtensionPoints.FITNESS_FUNCTION.identifier, FsFitnessFunction.class.getCanonicalName());

        ConfigurationProperties.setProperty(ExtensionPoints.OPERATORS_SPACE.identifier, FsRepairSpace.class.getCanonicalName());
    }
}
