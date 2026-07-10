package fr.inria.astor.approaches.flakydebug.extension.operators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.MethodConstraintMutator;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;

/**
 * Operator que substitui invocações por variantes mais restritivas (ex.:
 * métodos que exigem ordem).
 * De certa forma, talvez não seja um operador válido uma vez que consideração de ordem se refere mais ao `ShuffleCollectionOp`.
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class InvocationReplacementOp extends Operator {
	// Mapeia nomes de métodos que costumam impor ordem/contrato -> versões "restritas".
	private static Map<String, List<String>> methodReplacements = new HashMap<>();
	public InvocationReplacementOp() {
		super();
        // Exemplos reais de substituições observadas em PRs de projetos
        // https://github.com/hellokaton/30-seconds-of-java8/pull/5
        // https://github.com/hellokaton/30-seconds-of-java8/pull/6
        methodReplacements.put("containsExactlyInAnyOrder", Arrays.asList("containsExactly"));
        methodReplacements.put("containsExactlyInAnyOrderElementsOf", Arrays.asList("containsExactlyElementsOf"));
        methodReplacements.put("containsOnly", Arrays.asList("containsExactly"));
        // https://github.com/apache/incubator-kie-drools/pull/6187\
        // https://github.com/apache/pulsar/pull/24871
        // https://github.com/AuthMe/AuthMeReloaded/pull/2386
        methodReplacements.put("contains", Arrays.asList("containsExactly"));
        // https://github.com/apache/fory/pull/2738
        methodReplacements.put("sortedCopyOf", Arrays.asList("copyOf"));
        this.mutatorComposite.getMutators().add(new MethodConstraintMutator(this.mutatorComposite.getFactory()));
	}

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
		// Aplica apenas em pontos que sejam invocações (chamadas de método).
		CtElement toMutate = point.getCodeElement();
		if(toMutate instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) toMutate;
            String methodName = invocation.getExecutable().getSimpleName();
            // Se o método é um dos alvos, clona a invocação e altera seu nome.
            return methodReplacements.containsKey(methodName);
        }
        return false;
    }
}
