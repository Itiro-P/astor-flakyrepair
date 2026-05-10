package fr.inria.astor.approaches.flakyrepair.extension;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.faultlocalization.FaultLocalizationStrategy;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.faultlocalization.gzoltar.GzoltarTestClassesFinder;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
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
            boolean hasAssert = spoonMethod.getElements(new TypeFilter<>(CtInvocation.class))
            .stream()
            .anyMatch(inv -> {
                String methodName = inv.getExecutable().getSimpleName();

                if (!methodName.startsWith("assert")) return false;

                CtTypeReference<?> declaringType = inv.getExecutable().getDeclaringType();
                if (declaringType == null) return false;

                String qualifiedName = declaringType.getQualifiedName();

                return qualifiedName.startsWith("org.junit")
                    || qualifiedName.contains("Assertions")
                    || qualifiedName.contains("Assert");
            });

            if ((spoonMethod.getAnnotation(org.junit.Test.class) == null && !hasAssert) ||
                spoonMethod.getDeclaringType().getModifiers().contains(ModifierKind.ABSTRACT) ||
                spoonMethod.getBody() == null || 
                spoonMethod.getBody().getStatements().isEmpty()) {
                continue;
            }

            String className = spoonMethod.getDeclaringType().getQualifiedName();
            String methodName = spoonMethod.getSimpleName();

            for (CtStatement statement : spoonMethod.getBody().getStatements()) {
                if (!statement.getPosition().isValidPosition()) continue;

                int lineNumber = statement.getPosition().getLine();
                SuspiciousCode sc = new SuspiciousCode(className, methodName, 1.0);
                sc.setLineNumber(lineNumber);
                suspicious.add(sc);
                System.out.println("Suspicious: " + className + "#" + methodName + " at line " + lineNumber);
            }
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