package fr.inria.astor.approaches.flakyrepair;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.flakyrepair.extension.FrFaultLocalization;
import fr.inria.astor.approaches.flakyrepair.extension.FrFitnessFunction;
import fr.inria.astor.approaches.flakyrepair.extension.FrProcessValidator;
import fr.inria.astor.approaches.jmutrepair.jMutRepairExhaustive;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
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
        ConfigurationProperties.setProperty(ExtensionPoints.TARGET_CODE_PROCESSOR.identifier, "statements");
        /*
         * Validation using O. Parry's flakiness formula.
         */
        ConfigurationProperties.setProperty(ExtensionPoints.FITNESS_FUNCTION.identifier, FrFitnessFunction.class.getCanonicalName());

        /**
         * Changing output to represent the results in a more representative way.
         */
        //ConfigurationProperties.setProperty(ExtensionPoints.OUTPUT_RESULTS.identifier, null);
    }
}
