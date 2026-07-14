package fr.inria.astor.approaches.flakydebug.extension.operators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators.ShuffleJSONMutator;
import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators.ShuffleListMutator;
import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators.ShuffleMapMutator;
import fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators.ShuffleSetMutator;
import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledJSON;
import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledList;
import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledMap;
import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledSet;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

/**
 * @brief Operador que troca implementações de coleções por versões que embaralham seus elementos.
 * Exemplo de PR afetado: https://github.com/apache/fory/pull/2738
 * @author Pedro Itiro Nagao.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ShuffleCollectionOp extends Operator {
    Map<CtTypeReference<?>, CtTypeReference<?>> mappings = new HashMap<>();

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
        mappings.put(typeFactory.createReference(java.util.List.class),
            typeFactory.createReference(ShuffledList.class));
        mappings.put(typeFactory.createReference(java.util.Set.class),
            typeFactory.createReference(ShuffledSet.class));
        mappings.put(typeFactory.createReference(java.util.Map.class),
            typeFactory.createReference(ShuffledMap.class));
        mappings.put(typeFactory.createReference(org.json.JSONObject.class),
            typeFactory.createReference(ShuffledJSON.class));
    }

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        CtElement element = point.getCodeElement();
        CtTypeReference<?> type = null;
        if (element instanceof CtConstructorCall) {
            type = ((CtConstructorCall) element).getType();
        } else if(element instanceof CtLocalVariable) {
            type = ((CtLocalVariable) element).getType();
        }
        return isCandidate(type);
    }

    private boolean isCandidate(CtTypeReference<?> type) {
        return type != null && !isShuffled(type) && isValidCollection(type);
    }

    private boolean isShuffled(CtTypeReference<?> type) {
        return mappings.values().stream().anyMatch(shuffled -> type.getQualifiedName().equals(shuffled.getQualifiedName()));
    }

    private boolean isValidCollection(CtTypeReference<?> typeRef) {
        if (typeRef == null) return false;
        return this.mappings.keySet().stream().anyMatch(type -> typeRef.isSubtypeOf(type));
    }
}
