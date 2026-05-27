package fr.inria.astor.approaches.flakydebug.extension;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.faultlocalization.FaultLocalizationStrategy;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.faultlocalization.gzoltar.GzoltarTestClassesFinder;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.filter.TypeFilter;
import fr.inria.astor.core.entities.ModificationPoint;

/**
 *
 * @author Pedro I. Nagao
 */
public class FdFaultLocalization implements FaultLocalizationStrategy {

    @Override
    public FaultLocalizationResult searchSuspicious(ProjectRepairFacade projectToRepair, List<String> testToRun) throws Exception {
        FdRepairSpace repairSpace = new FdRepairSpace();

        List<SuspiciousCode> suspicious = new ArrayList<>();

        List<CtMethod<?>> allMethods = MutationSupporter.factory.getModel().getElements(new TypeFilter<>(CtMethod.class));
        
        for (CtMethod<?> spoonMethod : allMethods) {
            if(!isTestMethod(spoonMethod)) continue;

            String className = spoonMethod.getDeclaringType().getQualifiedName();
            String methodName = spoonMethod.getSimpleName();

            for (CtStatement statement : spoonMethod.getBody().getStatements()) {
                if (!statement.getPosition().isValidPosition()) continue;

                CtClass<?> ctClass = (CtClass<?>) spoonMethod.getDeclaringType();
                ModificationPoint mp = new ModificationPoint(statement, ctClass, null);
            
                // checar cada operador usando o mp; se não aplicável, tentar elementos mais específicos
                for (AstorOperator op : repairSpace.getOperators()) {
                    // primeiro teste com o statement inteiro
                    if (op.canBeAppliedToPoint(mp)) {
                        int lineNumber = statement.getPosition().getLine();
                        SuspiciousCode sc = new SuspiciousCode(className, methodName, 1.0);
                        sc.setLineNumber(lineNumber);
                        suspicious.add(sc);
                        System.out.println("Suspicious: " + className + "#" + methodName + " at line " + lineNumber);
                    }
                    // opcional: restaurar mp.setCodeElement(statement) antes de testar o próximo operador
                    mp.setCodeElement(statement);
                }
            }
        }

        System.out.println("FlakyDebugFaultLocalization: total suspicious: " + suspicious.size());

        return new FaultLocalizationResult(suspicious, testToRun, testToRun);
    }

    /**
     * Verifica se o método é um caso de teste.
     * @param method O método a ser verificado.
     * @return `true` se o método for um caso de teste, `false` caso contrário.
     */
    private boolean isTestMethod(CtMethod<?> method) {
        return (method.getAnnotation(org.junit.Test.class) != null ||
               method.getAnnotation(org.junit.jupiter.api.Test.class) != null ||
               method.getAnnotation(org.testng.annotations.Test.class) != null) && 
               !(method.getDeclaringType().getModifiers().contains(ModifierKind.ABSTRACT) ||
                method.getBody() == null || 
                method.getBody().getStatements().isEmpty());
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