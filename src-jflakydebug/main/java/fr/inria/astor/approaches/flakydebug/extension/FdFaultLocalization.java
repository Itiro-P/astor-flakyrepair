package fr.inria.astor.approaches.flakydebug.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.faultlocalization.FaultLocalizationStrategy;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.faultlocalization.gzoltar.GzoltarTestClassesFinder;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSpace;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.filter.TypeFilter;

public abstract class FdFaultLocalization implements FaultLocalizationStrategy {
    protected Logger log = Logger.getLogger(this.getClass().getCanonicalName());

    protected FaultLocalizationResult searchSuspicious(ProjectRepairFacade projectToRepair, List<String> testToRun, OperatorSpace repairSpace) throws Exception {

        // Uma mesma linha pode ser um ponto de mutação válido para mais de
        // um operador (ex: ShuffleCollectionOp E outro operador qualquer).
        // Por isso associamos cada linha suspeita a um CONJUNTO de
        // operadores, em vez de manter só o primeiro que bateu.
        //
        // A chave (className#methodName#lineNumber) garante deduplicação
        // por linha de forma explícita, sem depender de como
        // SuspiciousCode.equals()/hashCode() estão implementados.
        Map<String, SuspiciousCode> suspiciousByKey = new HashMap<>();
        Map<SuspiciousCode, Set<AstorOperator>> operatorsBySuspicious = new HashMap<>();

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

                int lineNumber = statement.getPosition().getLine();
                String key = className + "#" + methodName + "#" + lineNumber;

                ModificationPoint mp = new ModificationPoint(statement, ctClass, null);

                for (AstorOperator op : repairSpace.getOperators()) {
                    if (!op.canBeAppliedToPoint(mp)) continue;

                    SuspiciousCode sc = suspiciousByKey.computeIfAbsent(key, k -> {
                        SuspiciousCode created = new SuspiciousCode(className, methodName, 1.0);
                        created.setLineNumber(lineNumber);
                        return created;
                    });

                    Set<AstorOperator> opsForThisLine = operatorsBySuspicious.computeIfAbsent(sc, k -> new LinkedHashSet<>());
                    boolean isNewOperatorForThisLine = opsForThisLine.add(op);

                    if (isNewOperatorForThisLine) {
                        System.out.println(
                            "Suspicious Line: " + className +
                            "#" + methodName +
                            " at line " + lineNumber +
                            " by operator " + op.name()
                        );
                    }
                }
            }
        }

        List<SuspiciousCode> result = new ArrayList<>();
        // Workaround: FdVariantFactory.createInitialPopulation() itera a partir do
        // índice 1 (comportamento mantido de propósito nesta versão). Para não
        // perder o primeiro suspeito real por conta disso, inserimos um dummy na
        // posição 0, que é sempre descartado por aquele loop.
        result.add(createDummySuspicious());
        result.addAll(suspiciousByKey.values());

        log.info("Total suspicious points: " + suspiciousByKey.size());
        return new FaultLocalizationResult(result, testToRun, testToRun);
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

    private SuspiciousCode createDummySuspicious() {
        SuspiciousCode dummy = new SuspiciousCode("__DUMMY__", "__DUMMY__", 0d);
        dummy.setLineNumber(-1);
        return dummy;
    }
}
