package fr.inria.astor.approaches.flakyrepair.extension;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.faultlocalization.FaultLocalizationStrategy;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.faultlocalization.gzoltar.GzoltarTestClassesFinder;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ProjectRepairFacade;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 *
 * @author Pedro I. Nagao
 */
public class FrFaultLocalization implements FaultLocalizationStrategy {

    @Override
    public FaultLocalizationResult searchSuspicious(ProjectRepairFacade projectToRepair,
            List<String> testToRun) throws Exception {

        List<SuspiciousCode> suspicious = new ArrayList<>();

        List<CtMethod<?>> allMethods = MutationSupporter.factory.getModel().getElements(new TypeFilter<>(CtMethod.class));

        for (CtMethod<?> spoonMethod : allMethods) {
            if (spoonMethod.getAnnotation(org.junit.Test.class) == null ||
                spoonMethod.getDeclaringType().getModifiers().contains(ModifierKind.ABSTRACT) ||
                (spoonMethod.getBody() == null || spoonMethod.getBody().getStatements().isEmpty()) ||
                !spoonMethod.getBody().getStatements().get(0).getPosition().isValidPosition()
                ) {
                continue;
            }

            String className = spoonMethod.getDeclaringType().getQualifiedName();
            String methodName = spoonMethod.getSimpleName();

            int lineNumber = spoonMethod.getBody().getStatements().get(0).getPosition().getLine();

            SuspiciousCode sc = new SuspiciousCode(className, methodName, 1.0);
            sc.setLineNumber(lineNumber);
            suspicious.add(sc);

            System.out.println("FlakyRepairFaultLocalization: suspicious " + className + "#" + methodName + " at line " + lineNumber);
        }

        System.out.println("FlakyRepairFaultLocalization: total suspicious: " + suspicious.size());

        return new FaultLocalizationResult(suspicious, testToRun, testToRun);
    }

    @Override
    public List<String> findTestCasesToExecute(ProjectRepairFacade projectFacade) {
        List<String> testall = null;
        try {
            testall = GzoltarTestClassesFinder.findIn(projectFacade);
            System.out.println("Test all: " + testall);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return testall;
    }
}