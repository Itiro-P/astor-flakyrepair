package fr.inria.astor.approaches.flakyrepair.extension.operators.mutators;

import java.util.ArrayList;
import java.util.Arrays;
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
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

/**
 * Mutator que troca coleções por suas versões "encadeadas".
 *
 * Substitui usos de coleções mutáveis padrão (ex: HashMap/HashSet)
 * por suas contrapartes "linked" (ex: LinkedHashMap/LinkedHashSet). Útil
 * quando a ordem de iteração pode afetar comportamento flaky (não-determinístico).
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings("rawtypes")
public class LinkedInjectorMutator extends SpoonMutator<CtInvocation> {

    private static Map<String, List<String>> classReplacements = new HashMap<>();

    public LinkedInjectorMutator(Factory factory) {
        super(factory);
        // Mapeamento de classes alvo -> classe substituta.
        // Exemplo real: druid PR que troca HashMap por LinkedHashMap.
        // Na maioria dos projetos onde foi usado HashMap, trocar para LinkedHashMap resolveu o rpoblema
        // https://github.com/alibaba/druid/pull/4717/
        // Uso de TreeMap em: https://github.com/apache/linkis/pull/5005
        classReplacements.put("java.util.HashMap", Arrays.asList("java.util.LinkedHashMap", "java.util.TreeMap"));
        // https://github.com/apache/iotdb/pull/13961
        classReplacements.put("java.util.HashSet", Arrays.asList("java.util.LinkedHashSet"));
    }

    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        // Checamos qual o tipo de CtElement temos.
        // Pois chamar com 'new' é diferente de 'HashMap<K, V> a = ...' por exemplo.
        if (toMutate instanceof CtConstructorCall) {
            CtConstructorCall ctc = (CtConstructorCall) toMutate;
            String className = ctc.getType().getQualifiedName();

            if (classReplacements.containsKey(className)) {
                for(String replacement: classReplacements.get(className)) {
                    CtTypeReference replacementRef = this.getFactory().Type().createReference(replacement);             

                    CtConstructorCall mutantCtc = ctc.clone();
                    mutantCtc.setType(replacementRef);

                    MutantCtElement mutant = new MutantCtElement(mutantCtc, 1);
                    result.add(mutant);
                }
            }
        } else if (toMutate instanceof CtLocalVariable) {
            CtLocalVariable ctl = (CtLocalVariable) toMutate;
            String className = ctl.getAssignment().getType().getQualifiedName();

            if (classReplacements.containsKey(className)) {
                for(String replacement: classReplacements.get(className)) {
                    CtTypeReference replacementRef = this.getFactory().Type().createReference(replacement);             

                    CtLocalVariable mutantVar = ctl.clone();
                    CtExpression exp = mutantVar.getAssignment();
                    exp.setType(replacementRef);
                    mutantVar.setAssignment(exp);

                    MutantCtElement mutant = new MutantCtElement(mutantVar, 1);
                    result.add(mutant);
                    }
            }
        }

        return result;
    }

    // key/metadata helpers: identificadores e nível de mutação.

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