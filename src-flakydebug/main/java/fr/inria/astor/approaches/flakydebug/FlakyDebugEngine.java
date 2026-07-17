package fr.inria.astor.approaches.flakydebug;

import java.util.List;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.flakydebug.extension.FdFaultLocalization;
import fr.inria.astor.approaches.flakydebug.extension.FdFitnessFunction;
import fr.inria.astor.approaches.flakydebug.extension.FdProcessValidator;
import fr.inria.astor.approaches.flakydebug.extension.FdRepairSpace;
import fr.inria.astor.approaches.flakydebug.extension.FdTargetElementProcessor;
import fr.inria.astor.approaches.flakydebug.extension.FdVariantFactory;
import fr.inria.astor.approaches.jmutrepair.jMutRepairExhaustive;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.manipulation.filters.TargetElementProcessor;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.main.evolution.ExtensionPoints;

public class FlakyDebugEngine extends jMutRepairExhaustive {
    public FlakyDebugEngine(MutationSupporter mutationExecutor, ProjectRepairFacade projFacade) throws JSAPException {
        super(mutationExecutor, projFacade);
		ConfigurationProperties.properties.setProperty("population", "1");
        /**
         * Using FR's FL
         */
        ConfigurationProperties.setProperty(ExtensionPoints.FAULT_LOCALIZATION.identifier, FdFaultLocalization.class.getCanonicalName());

        /**
         * Processing patches by re-executing them.
         */
        ConfigurationProperties.setProperty(ExtensionPoints.VALIDATION.identifier, FdProcessValidator.class.getCanonicalName());
        ConfigurationProperties.setProperty("canhavezerosusp", Boolean.TRUE.toString());
        ConfigurationProperties.setProperty("includeTestInSusp", Boolean.TRUE.toString());
		ConfigurationProperties.setProperty("tmax1", "" + 30000);
        /*
         * Validation using O. Parry's flakiness formula.
         */
        ConfigurationProperties.setProperty(ExtensionPoints.FITNESS_FUNCTION.identifier, FdFitnessFunction.class.getCanonicalName());

        ConfigurationProperties.setProperty(ExtensionPoints.OPERATORS_SPACE.identifier, FdRepairSpace.class.getCanonicalName());

        ConfigurationProperties.setProperty(ExtensionPoints.TARGET_CODE_PROCESSOR.identifier, FdTargetElementProcessor.class.getCanonicalName());
    }

    @Override
    protected void loadTargetElements() throws Exception {

		ExtensionPoints extensionPointpoint = ExtensionPoints.TARGET_CODE_PROCESSOR;

		List<TargetElementProcessor<?>> loadedTargetElementProcessors = loadTargetElements(extensionPointpoint);

		this.setTargetElementProcessors(loadedTargetElementProcessors);
		this.setVariantFactory(new FdVariantFactory(this.getTargetElementProcessors()));
	}
}
