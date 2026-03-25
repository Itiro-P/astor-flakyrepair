package fr.inria.astor.approaches.flakyrepair.extension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.inria.astor.core.manipulation.filters.TargetElementProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;

public class FrInvocationFixProcessor extends TargetElementProcessor<CtInvocation> {

    private static Set<String> acceptedMethods = new HashSet<>(Arrays.asList(
        "Thread.sleep",
        "Thread.waitFor",
        "Assert.assertEquals",
		"Assert.assertTrue",
		"Assert.assertFalse",
		"Assert.fail",
        "Assertions.assertEquals", // JUnit 5
		"Assertions.assertTrue", // JUnit 5
		"Assertions.assertFalse", // JUnit 5
		"Assertions.fail" // JUnit 5
    ));
    private Logger logger = Logger.getLogger(FrInvocationFixProcessor.class.getName());

    @Override
    public void process(CtInvocation element) {
		if (element.getExecutable().getDeclaringType() == null) return;

        if (element.getParent() instanceof CtBlock) {
            String declaringType = element.getExecutable().getDeclaringType().getSimpleName();
            String methodName = element.getExecutable().getSimpleName();
            String fullName = declaringType + "." + methodName;
			//logger.info(fullName);
            if (acceptedMethods.contains(fullName)) {
                super.add((CtStatement) element);
            }
        }
    }
}
