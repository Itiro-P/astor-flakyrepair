package fr.inria.astor.approaches.flakyrepair.extension.operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

@SuppressWarnings("rawtypes")
public class MethodConstraintRelaxationMutator extends SpoonMutator<CtInvocation> {

    private static Map<String, String> methodReplacements = new HashMap<>();

    public MethodConstraintRelaxationMutator(Factory factory) {
        super(factory);
        /**
         * Sample: 30-seconds-of-java8
         * - https://github.com/hellokaton/30-seconds-of-java8/pull/5/
         * - https://github.com/hellokaton/30-seconds-of-java8/pull/6/
         */
        methodReplacements.put("containsExactly", "containsOnly");
    }

    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        if (toMutate instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) toMutate;
            String methodName = invocation.getExecutable().getSimpleName();
            if (methodReplacements.containsKey(methodName)) {
                CtInvocation mutatedInvocation = factory.Core().clone(invocation);
                mutatedInvocation.getExecutable().setSimpleName(methodReplacements.get(methodName));
                MutantCtElement mutant = new MutantCtElement(mutatedInvocation, 1);
                result.add(mutant);
            }
        }
        return result;
    }

    @Override
	public String key() {
		return "methodConstraintRelaxationOp";
	}

	@Override
	public void setup() {
	}


    @Override
	public int levelMutation() {
		return 1;
	}
}