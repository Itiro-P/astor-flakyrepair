package fr.inria.astor.approaches.flakydebug;

import java.util.List;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.flakydebug.extension.FdVariantFactory;
import fr.inria.astor.approaches.jmutrepair.jMutRepairExhaustive;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.manipulation.filters.TargetElementProcessor;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.main.evolution.ExtensionPoints;

/**
 * @brief Base class for implementing the jFlakyDebug engines.
 * @author Pedro Itiro Nagao
 */
public abstract class FdEngine extends jMutRepairExhaustive {
    public FdEngine(MutationSupporter mutationExecutor, ProjectRepairFacade projFacade) throws JSAPException {
        super(mutationExecutor, projFacade);
		ConfigurationProperties.properties.setProperty("population", "1");
        ConfigurationProperties.setProperty("canhavezerosusp", Boolean.TRUE.toString());
        ConfigurationProperties.setProperty("includeTestInSusp", Boolean.TRUE.toString());
        ConfigurationProperties.setProperty("tmax1", "" + 30000);
    }

    @Override
    protected void loadTargetElements() throws Exception {
        ExtensionPoints extensionPointpoint = ExtensionPoints.TARGET_CODE_PROCESSOR;

		List<TargetElementProcessor<?>> loadedTargetElementProcessors = loadTargetElements(extensionPointpoint);

		this.setTargetElementProcessors(loadedTargetElementProcessors);
		this.setVariantFactory(new FdVariantFactory());
	}
}
