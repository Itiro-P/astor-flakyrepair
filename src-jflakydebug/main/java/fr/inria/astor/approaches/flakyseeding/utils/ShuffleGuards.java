package fr.inria.astor.approaches.flakyseeding.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;

public class ShuffleGuards {
    //private static final Logger log = Logger.getLogger(ShuffleGuards.class.getCanonicalName());
    private static final Set<String> POINTWISE_METHODS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "get", "contains", "containsKey", "containsValue",
            "remove", "put", "size", "isEmpty", "equals", "hashCode"
        ))
    );

    private final Map<CtTypeReference<?>, CtTypeReference<?>> mappings;

    public ShuffleGuards(Map<CtTypeReference<?>, CtTypeReference<?>> mappings) {
        this.mappings = mappings;
    }

    /**
     * @brief Verifica se o tipo é uma implementação hash-based (HashMap,
     * HashSet, Hashtable, ...), cuja ordem de iteração não é garantida.
     * Exclui LinkedHashMap/LinkedHashSet, que contêm "hash" no nome mas
     * preservam ordem de inserção.
     */
    public boolean isHashBased(CtTypeReference<?> type) {
        if (type == null) return false;
        try {
            String typeName = type.getSimpleName().toLowerCase();
            return typeName.contains("hash") && 
                !(
                    typeName.contains("linked") || 
                    typeName.contains("tree") || 
                    typeName.contains("ordered") ||
                    typeName.contains("sorted")
                );
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @brief Verifica se o nome de método invocado é uma consulta pontual
     * (get/contains/remove/...) cujo resultado independe da ordem de
     * iteração interna do alvo hash-based.
     * @param methodName O nome do método invocado (ex.: {@code inv.getExecutable().getSimpleName()}).
     */
    public boolean isPointwise(String methodName) {
        return methodName != null && POINTWISE_METHODS.contains(methodName);
    }
    
    /**
     * @brief Verifica se o tipo é candidato a mutação: subtipo de uma das
     * coleções suportadas (List/Set/Map/JSONObject) e ainda não é uma das
     * versões já embaralhadas.
     */
    public boolean isCandidate(CtTypeReference<?> type, CtTypeReference<?> targetType) {
        if (type == null) return false;
        try {
            CtTypeReference<?> erased = type.getTypeErasure();
            if (erased == null || erased instanceof CtTypeParameterReference) return false;
            // Evita re-processar classes que já são as coleções embaralhadas
            boolean isAlreadyShuffled = mappings.values().stream()
                .anyMatch(shuffled -> erased.getQualifiedName().equals(shuffled.getQualifiedName()));
            if (isAlreadyShuffled) return false;

            // Evita classes que (parecem) não manipular a ordem indevidamente
            if (!this.isHashBased(erased)) return false;

            // O tipo concreto (ex.: HashMap) precisa ser subtipo de um dos tipos mapeados.
            return erased.isSubtypeOf(targetType);
        } catch (Exception e) {
          return false;
        }
    }

    /**
     * @brief Verifica se o tipo é candidato a mutação: subtipo de uma das
     * coleções suportadas (List/Set/Map/JSONObject) e ainda não é uma das
     * versões já embaralhadas.
     */
    public boolean isCandidate(CtTypeReference<?> type) {
        try {
            return mappings.keySet().stream().anyMatch(m -> this.isCandidate(type, m));
        } catch (Exception e) {
          return false;
        }
    }


    public boolean isInvocationCandidate(CtInvocation<?> inv) {
        return this.isCandidate(inv.getType()) && !this.isPointwise(inv.getExecutable().getSimpleName());
    }

    public boolean isTargetInvocationCandidate(CtInvocation<?> inv) {
        if (inv.getParent() instanceof CtBlock) return false;
        CtExpression<?> target = inv.getTarget(); 
        return this.isCandidate(target.getType());
    }

    public boolean isInvocationCandidate(CtInvocation<?> inv, CtTypeReference<?> targetType) {
        return this.isCandidate(inv.getType(), targetType) && !this.isPointwise(inv.getExecutable().getSimpleName());
    }

    public boolean isTargetInvocationCandidate(CtInvocation<?> inv, CtTypeReference<?> targetType) {
        if (inv.getParent() instanceof CtBlock) return false;
        CtExpression<?> target = inv.getTarget(); 
        return this.isCandidate(target.getType(), targetType);
    }
}