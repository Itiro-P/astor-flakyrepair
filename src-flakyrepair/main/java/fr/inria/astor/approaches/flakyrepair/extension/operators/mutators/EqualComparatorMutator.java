package fr.inria.astor.approaches.flakyrepair.extension.operators.mutators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * Mutator que troca a asserćão por outra que compara colećões por igualdade.
 * Esse comportamento pode ocorrer quando se compara (principalmente) colećões convertidas em tipos como `string` ou 
 * até mesmo `list`.
 *
 * @author Pedro Itiro Nagao
 */

// https://github.com/apache/cloudstack/pull/6967
public class EqualComparatorMutator extends SpoonMutator<CtMethod> {
    protected Logger log = Logger.getLogger(this.getClass().getName());
    private static HashSet<String> allowedMethods = new HashSet<>(Arrays.asList("toString", "toArray"));
    public EqualComparatorMutator(Factory factory) {
        super(factory);
    }

    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();

        if (!(toMutate instanceof CtMethod)) {
            log.debug("[EqualComparator] NÃO é CtMethod: " + toMutate.getClass().getSimpleName());
            return result;
        }

        CtMethod<?> method = (CtMethod<?>) toMutate;
        CtMethod<?> mutated = factory.Core().clone(method);

        List<CtInvocation> asserts = mutated.getElements(new TypeFilter<>(CtInvocation.class))
            .stream()
            .filter(inv -> inv.getExecutable().getSimpleName().equals("assertEquals"))
            .collect(Collectors.toList());

        log.debug("[EqualComparator] assertEquals encontrados: " + asserts.size());

        for (CtInvocation inv : asserts) {
            List<CtExpression<?>> args = inv.getArguments();
            if (args.size() < 2) continue;

            List<CtExpression<?>> newArgs = new ArrayList<>();
            boolean changed = false;

            for (CtExpression<?> arg : args) {
                CtExpression<?> unwrapped = unwrapConverter(arg);
                log.debug("[EqualComparator] unwrap [" + arg + "] -> " + unwrapped);
                if (unwrapped != null) {
                    newArgs.add(unwrapped);
                    changed = true;
                } else {
                    newArgs.add(arg.clone());
                }
            }

            if (changed) {
                inv.setArguments(newArgs);
                result.add(new MutantCtElement(mutated, 1));
                log.debug("[EqualComparator] Mutante gerado: " + mutated);
                break; // uma mutação por vez
            }
        }

        return result;
    }

    private CtExpression<?> unwrapConverter(CtExpression<?> expr) {
        if (!(expr instanceof CtInvocation<?>)) return null;
        CtInvocation<?> call = (CtInvocation<?>) expr;
        String name = call.getExecutable().getSimpleName();
        if (!allowedMethods.contains(name)) return null;
        return call.getTarget().clone();
    }

    @Override
	public String key() {
		return "equalComparatorMutator";
	}

	@Override
	public void setup() {
	}


    @Override
	public int levelMutation() {
		return 1;
	}
}
