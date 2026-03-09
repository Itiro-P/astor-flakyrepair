package fr.inria.astor.approaches.flakyrepair;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.jmutrepair.jMutRepairExhaustive;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.main.evolution.ExtensionPoints;

public class FlakyRepairEngine extends jMutRepairExhaustive {
    public FlakyRepairEngine(MutationSupporter mutationExecutor, ProjectRepairFacade projFacade) throws JSAPException {
        super(mutationExecutor, projFacade);
        //ConfigurationProperties.setProperty(ExtensionPoints.FAULT_LOCALIZATION.identifier, "flakyrepair");
    }
}
