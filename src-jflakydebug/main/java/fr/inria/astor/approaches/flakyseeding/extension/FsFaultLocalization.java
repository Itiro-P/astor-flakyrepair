package fr.inria.astor.approaches.flakyseeding.extension;

import java.util.List;

import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.approaches.flakydebug.extension.FdFaultLocalization;
import fr.inria.astor.approaches.flakyseeding.extension.FsFaultLocalization;

/**
 *
 * @author Pedro I. Nagao
 */
public class FsFaultLocalization extends FdFaultLocalization {

    @Override
    public FaultLocalizationResult searchSuspicious(ProjectRepairFacade projectToRepair, List<String> testToRun) throws Exception {
        return super.searchSuspicious(projectToRepair, testToRun, new FsRepairSpace());
    }
}