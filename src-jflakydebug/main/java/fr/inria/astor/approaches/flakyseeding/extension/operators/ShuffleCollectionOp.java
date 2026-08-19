package fr.inria.astor.approaches.flakyseeding.extension.operators;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import spoon.reflect.declaration.CtType;
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

    private final Map<CtTypeReference<?>, CtTypeReference<?>> mappings = new HashMap<>();

    private static final Set<String> POINTWISE_METHODS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "get", "contains", "containsKey", "containsValue",
            "remove", "put", "size", "isEmpty", "equals", "hashCode"
        ))
    );

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
        this.mappings.put(typeFactory.createReference(java.util.List.class), typeFactory.createReference(ShuffledList.class));
        this.mappings.put(typeFactory.createReference(java.util.Set.class), typeFactory.createReference(ShuffledSet.class));
        this.mappings.put(typeFactory.createReference(java.util.Map.class), typeFactory.createReference(ShuffledMap.class));
        this.mappings.put(typeFactory.createReference(org.json.JSONObject.class), typeFactory.createReference(ShuffledJSON.class));
    }

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        CtElement element = point.getCodeElement();

        if (element instanceof CtConstructorCall) {
            return isCandidate(((CtConstructorCall<?>) element).getType());
        }

        if (element instanceof CtLocalVariable) {
            return isCandidate(((CtLocalVariable<?>) element).getType());
        }

        if (element instanceof CtInvocation) {
            return checkInvocation((CtInvocation<?>) element);
        }

        return false;
    }

    private boolean checkInvocation(CtInvocation<?> inv) {
        CtTypeReference<?> type = inv.getType();
        if (type == null) return false;

        CtType<?> typeDec = type.getTypeDeclaration();
        if (typeDec == null || inv.getParent() instanceof CtBlock) return false;

        // A invocação retorna um tipo candidato a mutação
        if (isCandidate(type)) {
            return this.mappings.keySet().stream().anyMatch(t -> typeDec.getReference().isSubtypeOf(t));
        }

        // O alvo (target) da invocação é um tipo candidato a mutação
        if (inv.getTarget() != null && isCandidate(inv.getTarget().getType())) {
            String typeName = typeDec.getSimpleName().toLowerCase();
            boolean isHashBased = typeName.contains("hash") && !typeName.contains("linkedhash");
            
            String methodName = inv.getExecutable().getSimpleName();
            return isHashBased && !POINTWISE_METHODS.contains(methodName);
        }

        return false;
    }

    private boolean isCandidate(CtTypeReference<?> type) {
        if (type == null) return false;

        CtType<?> typeDec = type.getTypeDeclaration();
        if (typeDec == null) return false;

        // Evita re-processar classes que já são as coleções embaralhadas
        boolean isAlreadyShuffled = this.mappings.values().stream().anyMatch(shuffled -> typeDec.getQualifiedName().equals(shuffled.getQualifiedName()));
        if (isAlreadyShuffled) return false;

        // Verifica se é subtipo de alguma das coleções suportadas
        return this.mappings.keySet().stream().anyMatch(supportedType -> typeDec.getReference().isSubtypeOf(supportedType));
    }
}