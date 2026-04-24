package fr.inria.astor.approaches.flakyrepair.extension.operators.mutators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Mutator que substitui chamadas de métodos por versões "relaxadas" que impõem menos restrições
 * (ex: `containsExactly` -> `containsOnly`). Útil para lidar com testes flaky causados por
 * verificações de conteúdo que não exigem ordem ou presença exata.
 *
 * Clona a invocação original e altera seu nome para a versão relaxada, retornando a mutação
 * como um `MutantCtElement`.
 */
@SuppressWarnings("rawtypes")
public class MethodConstraintRelaxationMutator extends SpoonMutator<CtInvocation> {

    // Mapeia nomes de métodos que costumam impor ordem/contrato -> versões "relaxadas".
    private static Map<String, String> methodReplacements = new HashMap<>();

    public MethodConstraintRelaxationMutator(Factory factory) {
        super(factory);
        // Exemplos reais de substituições observadas em PRs de projetos
        methodReplacements.put("containsExactly", "containsOnly");
        methodReplacements.put("containOnly", "containsExactlyInAnyOrder");
        methodReplacements.put("contains", "containsInAnyOrder");
        methodReplacements.put("copyOf", "sortedCopyOf");
    }

    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        if (toMutate instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) toMutate;
            String methodName = invocation.getExecutable().getSimpleName();
            // Se o método é um dos alvos, clona a invocação e altera seu nome.
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