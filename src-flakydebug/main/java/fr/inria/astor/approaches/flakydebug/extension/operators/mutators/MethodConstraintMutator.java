package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Mutator que substitui chamadas de métodos por versões "restritas" que impõem mais restrições
 * (ex: `containsOnly` -> `containsExactly`). Útil para lidar com testes flaky causados por
 * verificações de conteúdo que não exigem ordem ou presença exata.
 *
 * Clona a invocação original e altera seu nome para a versão relaxada, retornando a mutação
 * como um `MutantCtElement`.
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings("rawtypes")
public class MethodConstraintMutator extends SpoonMutator<CtInvocation> {
    // Mapeia nomes de métodos que costumam impor ordem/contrato -> versões "relaxadas".
    private static Map<String, List<String>> methodReplacements = new HashMap<>();

    public MethodConstraintMutator(Factory factory) {
        super(factory);
        // Exemplos reais de substituições observadas em PRs de projetos
        // https://github.com/hellokaton/30-seconds-of-java8/pull/5
        // https://github.com/hellokaton/30-seconds-of-java8/pull/6
        methodReplacements.putIfAbsent("containsExactlyInAnyOrder", Arrays.asList("containsExactly"));
        methodReplacements.putIfAbsent("containsExactlyInAnyOrderElementsOf", Arrays.asList("containsExactlyElementsOf"));
        methodReplacements.putIfAbsent("containsOnly", Arrays.asList("containsExactly"));
        // https://github.com/apache/incubator-kie-drools/pull/6187\
        // https://github.com/apache/pulsar/pull/24871
        // https://github.com/AuthMe/AuthMeReloaded/pull/2386
        methodReplacements.putIfAbsent("contains", Arrays.asList("containsExactly"));
        // https://github.com/apache/fory/pull/2738
        methodReplacements.putIfAbsent("sortedCopyOf", Arrays.asList("copyOf"));
    }

    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        if (toMutate instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) toMutate;
            String methodName = invocation.getExecutable().getSimpleName();
            // Se o método é um dos alvos, clona a invocação e altera seu nome.
            if (methodReplacements.containsKey(methodName)) {
                for(String replacement: methodReplacements.get(methodName)) {
                    CtInvocation mutatedInvocation = factory.Core().clone(invocation);
                    mutatedInvocation.getExecutable().setSimpleName(replacement);
                    MutantCtElement mutant = new MutantCtElement(mutatedInvocation, 1);
                    result.add(mutant);
                }
            }
        }
        return result;
    }

    @Override
	public String key() {
		return "methodConstraintMutator";
	}

	@Override
	public void setup() {
	}


    @Override
	public int levelMutation() {
		return 1;
	}
}