package fr.inria.astor.approaches.flakyrepair.extension.operators.mutators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

@SuppressWarnings("rawtypes")
public class LinkedInjectorMutator extends SpoonMutator<CtInvocation> {

    private static Map<String, String> classReplacements = new HashMap<>();

    public LinkedInjectorMutator(Factory factory) {
        super(factory);
        /**
         * Sample: druid
         * - https://github.com/alibaba/druid/pull/4717/
         */
        classReplacements.put("java.util.HashMap", "java.util.LinkedHashMap");
    }

    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        if (toMutate instanceof CtConstructorCall) {
            CtConstructorCall ctc = (CtConstructorCall) toMutate;
            String className = ctc.getType().getQualifiedName();

            if (classReplacements.containsKey(className)) {
                String replacement = classReplacements.get(className);
                CtTypeReference replacementRef = this.getFactory().Type().createReference(replacement);             

                CtConstructorCall mutantCtc = ctc.clone();
                mutantCtc.setType(replacementRef);

                MutantCtElement mutant = new MutantCtElement(mutantCtc, 1);
                result.add(mutant);
            }
        } else if (toMutate instanceof CtLocalVariable) {
            CtLocalVariable ctl = (CtLocalVariable) toMutate;
            String className = ctl.getAssignment().getType().getQualifiedName();

            if (classReplacements.containsKey(className)) {
                String replacement = classReplacements.get(className);
                CtTypeReference replacementRef = this.getFactory().Type().createReference(replacement);             

                CtLocalVariable mutantVar = ctl.clone();
                CtExpression exp = mutantVar.getAssignment();
                exp.setType(replacementRef);
                mutantVar.setAssignment(exp);

                MutantCtElement mutant = new MutantCtElement(mutantVar, 1);
                result.add(mutant);
            }
        }

        return result;
    }

    @Override
	public String key() {
		return "linkedInjectorOp";
	}

	@Override
	public void setup() {
	}


    @Override
	public int levelMutation() {
		return 1;
	}
}