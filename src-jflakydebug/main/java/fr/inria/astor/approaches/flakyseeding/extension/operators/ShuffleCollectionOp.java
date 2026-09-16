package fr.inria.astor.approaches.flakyseeding.extension.operators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.shufflemutators.ShuffleJSONMutator;
import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.shufflemutators.ShuffleListMutator;
import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.shufflemutators.ShuffleMapMutator;
import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.shufflemutators.ShuffleSetMutator;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffleGuards;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledJSON;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledList;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledMap;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledSet;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

/**
 * Operador que troca implementações de coleções por versões que embaralham seus elementos.
 * Exemplo de PR afetado: https://github.com/apache/fory/pull/2738
 * 
 * @author Pedro Itiro Nagao.
 */
@SuppressWarnings("unchecked")
public class ShuffleCollectionOp extends Operator {
    private final ShuffleGuards guards;

    public ShuffleCollectionOp() {
        super();
        Factory factory = this.mutatorComposite.getFactory();
        
        this.mutatorComposite.getMutators().addAll(Arrays.asList(
            new ShuffleListMutator(factory),
            new ShuffleSetMutator(factory),
            new ShuffleMapMutator(factory),
            new ShuffleJSONMutator(factory)
        ));
        TypeFactory typeFactory = factory.Type();

        this.guards = new ShuffleGuards(
            new HashMap<CtTypeReference<?>, CtTypeReference<?>>() {{
                put(typeFactory.createReference(java.util.List.class), typeFactory.createReference(ShuffledList.class));
                put(typeFactory.createReference(java.util.Set.class), typeFactory.createReference(ShuffledSet.class));
                put(typeFactory.createReference(java.util.Map.class), typeFactory.createReference(ShuffledMap.class));
                put(typeFactory.createReference(org.json.JSONObject.class), typeFactory.createReference(ShuffledJSON.class));
            }},
            new HashSet<CtTypeReference<?>>() {{
                add(typeFactory.createReference(java.util.SortedMap.class));
                add(typeFactory.createReference(java.util.SortedSet.class));
                add(typeFactory.createReference(java.util.LinkedHashMap.class));
                add(typeFactory.createReference(java.util.LinkedHashSet.class));
                add(typeFactory.createReference(java.util.TreeMap.class));
                add(typeFactory.createReference(java.util.TreeSet.class));
            }}
        );
    }

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        CtElement element = point.getCodeElement();

        if (element instanceof CtInvocation) {
            CtInvocation<?> invocation  = (CtInvocation<?>) element;
            return invocation.getArguments().stream().anyMatch(arg -> guards.getMutationTarget(arg) != null);
        }

        if (element instanceof CtExpression) {
            return guards.getMutationTarget((CtExpression<?>) element) != null;
        }
        return false;
    }
}