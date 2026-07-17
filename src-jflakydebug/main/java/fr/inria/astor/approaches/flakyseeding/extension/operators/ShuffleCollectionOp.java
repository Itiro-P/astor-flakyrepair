package fr.inria.astor.approaches.flakyseeding.extension.operators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.shufflemutators.ShuffleJSONMutator;
import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.shufflemutators.ShuffleListMutator;
import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.shufflemutators.ShuffleMapMutator;
import fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.shufflemutators.ShuffleSetMutator;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledJSON;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledList;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledMap;
import fr.inria.astor.approaches.flakyseeding.utils.ShuffledSet;
import fr.inria.astor.core.entities.ModificationPoint;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
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
@SuppressWarnings("unchecked")
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

        if (element instanceof CtConstructorCall) {
            CtConstructorCall<?> ctc = (CtConstructorCall<?>) element;
            return this.isCandidate(ctc.getType());
        }

        // Pegamos atribuições de variáveis
        if (element instanceof CtLocalVariable) {
            CtLocalVariable<?> var = (CtLocalVariable<?>) element;
            return this.isCandidate(var.getType());
        }

        if (element instanceof CtInvocation) {
            CtInvocation<?> inv = (CtInvocation<?>) element;
            // Aqui pode ocorrer 2 casos:
            
            // A invocação retorna um tipo que queremos mutacionar
            if (this.isCandidate(inv.getType())) return true;
            
            // O alvo da invocação é um tipo que queremos mutacionar
            if (this.isCandidate(inv.getTarget().getType()) && !(inv.getParent() instanceof CtBlock)) return true;
        }

        return false;
    }
    
    private boolean isCandidate(CtTypeReference<?> type) {
        if (type == null) return false;
        if (this.mappings.values().stream().anyMatch(shuffled -> type.getQualifiedName().equals(shuffled.getQualifiedName()))) return false;

        return this.mappings.keySet().stream().anyMatch(t -> t.isSubtypeOf(type));
    }
}
