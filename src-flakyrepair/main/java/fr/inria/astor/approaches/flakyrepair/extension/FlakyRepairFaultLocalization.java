package fr.inria.astor.approaches.flakyrepair.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.faultlocalization.FaultLocalizationStrategy;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.faultlocalization.gzoltar.GzoltarTestClassesFinder;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.net.URL;

import javassist.*;

/**
 * Simple Localization Strategy which considers all the test case class a suspicious point each.
 * @author Pedro I. Nagao
 */
public class FlakyRepairFaultLocalization implements FaultLocalizationStrategy {
    @Override
    public FaultLocalizationResult searchSuspicious(ProjectRepairFacade projectToRepair, List<String> testToRun) throws Exception {
        List<SuspiciousCode> suspicious = new ArrayList();
        URL[] testClasses = GzoltarTestClassesFinder.getPath(projectToRepair);
        
        ClassLoader loader = new URLClassLoader(testClasses, Thread.currentThread().getContextClassLoader());
        ClassPool pool = new ClassPool(true);
        pool.appendClassPath(new LoaderClassPath(loader));
        for(String test: testToRun) {
            String className = test.split(",")[1].split("#")[0];
            Class<?> testClass = loader.loadClass(className);
            for(Method method: testClass.getDeclaredMethods()) {
                CtClass cc = pool.get(method.getDeclaringClass().getCanonicalName());
                // Montar os tipos dos parâmetros para o Javassist
                CtClass[] paramTypes = Arrays.stream(method.getParameterTypes())
                    .map(p -> {
                        try { return pool.get(p.getName()); }
                        catch (Exception e) { throw new RuntimeException(e); }
                    })
                    .toArray(CtClass[]::new);

                CtMethod javassistMethod = cc.getDeclaredMethod(method.getName(), paramTypes);
                int linenumber = javassistMethod.getMethodInfo().getLineNumber(0);
                SuspiciousCode sc = new SuspiciousCode(
                    testClass.getName(),
                    method.getName(),
                    1.0
                );
                sc.setLineNumber(linenumber);
                suspicious.add(sc);
            }
        }

        return new FaultLocalizationResult(suspicious, testToRun, testToRun);
    }

	@Override
	public List<String> findTestCasesToExecute(ProjectRepairFacade projectFacade) {
		List<String> testall = null;
		try {
			testall = GzoltarTestClassesFinder.findIn(projectFacade);
			System.out.println("Test all " + testall);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return testall;

	}
}
