package fr.inria.astor.approaches.flakydebug.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.faultlocalization.FaultLocalizationStrategy;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.faultlocalization.gzoltar.GzoltarTestClassesFinder;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
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
        Map<SuspiciousCode, AstorOperator> suspicious = new HashMap<>();

        List<CtMethod<?>> allMethods = MutationSupporter.factory.getModel().getElements(new TypeFilter<>(CtMethod.class));
        
        for (CtMethod<?> spoonMethod : allMethods) {
            if (!isTestMethod(spoonMethod)) continue;

            String className = spoonMethod.getDeclaringType().getQualifiedName();
            String methodName = spoonMethod.getSimpleName();
            
            CtType<?> declaringType = spoonMethod.getDeclaringType();
            if (!(declaringType instanceof CtClass)) continue; 
            CtClass<?> ctClass = (CtClass<?>) declaringType;

            if (spoonMethod.getBody() == null) continue;

            List<CtStatement> allStatementsInTest = spoonMethod.getBody().getElements(new TypeFilter<>(CtStatement.class));

            for (CtStatement statement : allStatementsInTest) {
                // Ignora blocos compostos vazios (ex: o próprio bloco do 'try' ou 'if' como um todo)
                // Queremos apenas as instruções reais e com posição válida
                if (statement == null || !statement.getPosition().isValidPosition()) continue;
                
                // Evita pegar blocos de código inteiros (CtBlock), focando nas instruções internas
                if (statement instanceof CtBlock) continue;

                ModificationPoint mp = new ModificationPoint(statement, ctClass, null);
            
                for (AstorOperator op : repairSpace.getOperators()) {
                    if (op.canBeAppliedToPoint(mp)) {
                        int lineNumber = statement.getPosition().getLine();
                        SuspiciousCode sc = new SuspiciousCode(className, methodName, 1.0);
                        sc.setLineNumber(lineNumber);
                        if(!suspicious.containsKey(sc)) {
                            suspicious.put(sc, op);
                            System.out.println(
                                "Suspicious Line: " + className + 
                                "#" + methodName + 
                                " at line " + lineNumber +
                                " by operator " + op.name()
                            );
                            break;
                        }
                    }
                    mp.setCodeElement(statement);
                }
            }
        }

        System.out.println("FlakyDebugFaultLocalization: total suspicious: " + suspicious.size());
        return new FaultLocalizationResult(new ArrayList<>(suspicious.keySet()), testToRun, testToRun);
    }

    /**
     * Verifica se o método é um caso de teste.
     */
    private boolean isTestMethod(CtMethod<?> method) {
        if (method == null || method.getDeclaringType() == null) return false;

        boolean hasTestAnnotation = method.getAnnotation(org.junit.Test.class) != null ||
                                    method.getAnnotation(org.junit.jupiter.api.Test.class) != null ||
                                    method.getAnnotation(org.testng.annotations.Test.class) != null;

        return hasTestAnnotation && 
               !(method.getDeclaringType().getModifiers().contains(ModifierKind.ABSTRACT)) &&
               method.getBody() != null && 
               !method.getBody().getStatements().isEmpty();
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