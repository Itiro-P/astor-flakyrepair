package fr.inria.astor.approaches.flakyrepair.extension;

import java.util.List;

import fr.inria.astor.approaches.flakydebug.extension.FdFaultLocalization;
import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.setup.ProjectRepairFacade;

/**
 *
 * @author Pedro I. Nagao
 */
public class FrFaultLocalization extends FdFaultLocalization {

    @Override
    public FaultLocalizationResult searchSuspicious(ProjectRepairFacade projectToRepair, List<String> testToRun) throws Exception {
        return super.searchSuspicious(projectToRepair, testToRun, new FrRepairSpace());
    }
}