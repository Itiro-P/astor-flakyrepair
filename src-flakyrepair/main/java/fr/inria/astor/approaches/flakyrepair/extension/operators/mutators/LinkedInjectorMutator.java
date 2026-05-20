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
import spoon.reflect.declaration.CtType;
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

    private static Map<CtTypeReference, List<CtTypeReference>> classReplacements = new HashMap<>();

    public LinkedInjectorMutator(Factory factory) {
        super(factory);
        // Mapeamento de classes alvo -> classe substituta.
        // Exemplo real: druid PR que troca HashMap por LinkedHashMap.
        // Na maioria dos projetos onde foi usado HashMap, trocar para LinkedHashMap resolveu o rpoblema
        // https://github.com/alibaba/druid/pull/4717/
        // Uso de TreeMap em: https://github.com/apache/linkis/pull/5005
        classReplacements.putIfAbsent(
            this.getFactory().createCtTypeReference(java.util.HashMap.class), 
            Arrays.asList(
                this.getFactory().createCtTypeReference(java.util.LinkedHashMap.class), 
                this.getFactory().createCtTypeReference(java.util.TreeMap.class)
            )
        );
        // https://github.com/apache/iotdb/pull/13961
        classReplacements.putIfAbsent(
            this.getFactory().createCtTypeReference(java.util.HashSet.class), 
            Arrays.asList(
                this.getFactory().createCtTypeReference(java.util.LinkedHashSet.class), 
                this.getFactory().createCtTypeReference(java.util.TreeSet.class)
            )
        );
    }

    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();
        // Checamos qual o tipo de CtElement temos.
        // Pois chamar com 'new' é diferente de 'HashMap<K, V> a = ...' por exemplo.
        if (toMutate instanceof CtConstructorCall) {
            CtConstructorCall ctc = (CtConstructorCall) toMutate;
            CtTypeReference classType = ctc.getType();

            if (classReplacements.containsKey(classType)) {
                for(CtTypeReference replacement: classReplacements.get(classType)) {   

                    CtConstructorCall mutantCtc = ctc.clone();
                    mutantCtc.setType(replacement);

                    MutantCtElement mutant = new MutantCtElement(mutantCtc, 1);
                    result.add(mutant);
                }
            }
        } else if (toMutate instanceof CtLocalVariable) {
            CtLocalVariable ctl = (CtLocalVariable) toMutate;
            CtTypeReference classType = ctl.getType();

            if (classReplacements.containsKey(classType)) {
                for(CtTypeReference replacement: classReplacements.get(classType)) {          

                    CtLocalVariable mutantVar = ctl.clone();
                    CtExpression exp = mutantVar.getAssignment();
                    exp.setType(replacement);
                    mutantVar.setAssignment(exp);

                    MutantCtElement mutant = new MutantCtElement(mutantVar, 1);
                    result.add(mutant);
                    }
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