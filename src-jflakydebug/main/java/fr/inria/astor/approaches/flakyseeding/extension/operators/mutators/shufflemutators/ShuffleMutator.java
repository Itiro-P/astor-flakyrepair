package fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.shufflemutators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.Mutator;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffleGuards;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledJSON;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledList;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledMap;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledSet;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.cu.position.NoSourcePosition;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class ShuffleMutator extends Mutator<CtElement> {
    private final ShuffleGuards guards;

    public ShuffleMutator(Factory factory) {
        super(factory);
        TypeFactory typeFactory = factory.Type();

        this.guards = new ShuffleGuards(
            new HashMap() {{
                put(typeFactory.createReference(java.util.List.class), typeFactory.createReference(ShuffledList.class));
                put(typeFactory.createReference(java.util.Set.class), typeFactory.createReference(ShuffledSet.class));
                put(typeFactory.createReference(java.util.Map.class), typeFactory.createReference(ShuffledMap.class));
                put(typeFactory.createReference(org.json.JSONObject.class), typeFactory.createReference(ShuffledJSON.class));
            }},
            new HashSet() {{
                add(typeFactory.createReference(java.util.SortedMap.class));
                add(typeFactory.createReference(java.util.SortedSet.class));
                add(typeFactory.createReference(java.util.LinkedHashMap.class));
                add(typeFactory.createReference(java.util.LinkedHashSet.class));
                add(typeFactory.createReference(java.util.TreeMap.class));
                add(typeFactory.createReference(java.util.TreeSet.class));
            }}
        );
    }

    public List<MutantCtElement> compute(CtElement toMutate,
        CtTypeReference replacementType,
        CtTypeReference targetType
    ) {
        List<MutantCtElement> result = new ArrayList<>();
        if (toMutate == null) return result;

        // Caso 1: O nó selecionado é uma invocação (ex: assertEquals(keys, keys2))
        if (toMutate instanceof CtInvocation) {
            CtInvocation<?> inv = (CtInvocation<?>) toMutate;
            List<CtExpression<?>> args = inv.getArguments();

            for (int i = 0; i < args.size(); i++) {
                CtExpression<?> arg = args.get(i);
                
                // Valida se o argumento (ou sua raiz) é elegível para embaralhamento
                CtExpression<?> rootExpr = guards.getMutationTarget(arg, targetType);
                if (rootExpr != null) {
                    // Clona a invocação inteira para preservar a chamada de método original
                    CtInvocation mutatedInv = inv.clone();
                    mutatedInv.setPosition(new NoSourcePosition());

                    // Aplica o wrap no argumento atual (ou na expressão clonada do nó)
                    CtConstructorCall wrappedArg = wrapTarget(replacementType, arg);
                    
                    // Substitui o argumento mutado no clone da invocação
                    List<CtExpression<?>> newArgs = new ArrayList<>(mutatedInv.getArguments());
                    newArgs.set(i, wrappedArg);
                    mutatedInv.setArguments(newArgs);

                    result.add(new MutantCtElement(mutatedInv, 1));
                }
            }

        // Caso 2: O nó selecionado é uma expressão direta (ex: atribuição de variável ou retorno)
        } else if (toMutate instanceof CtExpression) {
            CtExpression<?> expr = (CtExpression<?>) toMutate;
            CtExpression<?> rootExpr = guards.getMutationTarget(expr, targetType);
            if (rootExpr != null) {
                result.add(new MutantCtElement(wrapTarget(replacementType, expr), 1));
            }
        }
        return result;
    }

    private CtConstructorCall wrapTarget(CtTypeReference<?> replacementType, CtExpression<?> target) {
        CtConstructorCall wrapped = factory.createConstructorCall();
        CtExpression<?> clonedTarget = target.clone();
        clonedTarget.setPosition(new NoSourcePosition());
        clonedTarget.getElements(element -> true).forEach(element -> element.setPosition(new NoSourcePosition()));
        wrapped.setType(replacementType);
        wrapped.setArguments(Arrays.asList(clonedTarget));
        return wrapped;
    }
}