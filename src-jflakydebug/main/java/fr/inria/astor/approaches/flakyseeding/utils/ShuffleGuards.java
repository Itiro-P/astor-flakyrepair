package fr.inria.astor.approaches.flakyseeding.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;

public class ShuffleGuards {
    private static final Logger log = Logger.getLogger(ShuffleGuards.class.getCanonicalName());
    private static final Set<String> POINTWISE_METHODS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "get", "contains", "containsKey", "containsValue",
            "remove", "put", "size", "isEmpty", "equals", "hashCode"
        ))
    );

    private final Map<CtTypeReference<?>, CtTypeReference<?>> mappings;
    private final Set<CtTypeReference<?>> blacklistedTypes;

    public ShuffleGuards(Map<CtTypeReference<?>, CtTypeReference<?>> mappings, Set<CtTypeReference<?>> blacklistedTypes) {
        this.mappings = mappings;
        this.blacklistedTypes = blacklistedTypes;
    }

    public boolean isUnorderedCollection(CtTypeReference<?> type) {
        if (type == null) return false;
        try {
            String typeName = type.getSimpleName().toLowerCase();
            return !(
                    typeName.contains("linked") || 
                    typeName.contains("tree") || 
                    typeName.contains("ordered") ||
                    typeName.contains("sorted")
                ) &&
                this.blacklistedTypes.stream().noneMatch(b_type ->
                    type.isSubtypeOf(b_type)
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
            if (this.isShuffled(erased)) return false;

            // Evita classes que (parecem) não manipular a ordem indevidamente
            if (!this.isUnorderedCollection(erased)) return false;
            // O tipo concreto (ex.: HashMap) precisa ser subtipo de um dos tipos mapeados.
            return erased.isSubtypeOf(targetType);
        } catch (Exception e) {
          return false;
        }
    }

    private boolean isShuffled(CtTypeReference<?> type) {
        CtTypeReference<?> erased = type.getTypeErasure();
        if (erased == null || erased instanceof CtTypeParameterReference) return false;
        return mappings.values().stream().anyMatch(shuffled -> erased.getQualifiedName().equals(shuffled.getQualifiedName()));
    }

    /**
     * @brief Verifica se o tipo é candidato a mutação: subtipo de uma das
     * coleções suportadas (List/Set/Map/JSONObject) e ainda não é uma das
     * versões já embaralhadas.
     */
    public boolean isCandidate(CtTypeReference<?> type) {
        return mappings.keySet().stream().anyMatch(m -> this.isCandidate(type, m));
    }


    public boolean isInvocationCandidate(CtInvocation<?> inv) {
        return this.isCandidate(inv.getType()) && !this.isPointwise(inv.getExecutable().getSimpleName());
    }

    public boolean isTargetInvocationCandidate(CtInvocation<?> inv) {
        if (inv.getParent() instanceof CtBlock) return false;
        return this.isCandidate(inv.getTarget().getType());
    }

    public boolean isInvocationCandidate(CtInvocation<?> inv, CtTypeReference<?> targetType) {
        return this.isCandidate(inv.getType(), targetType) && !this.isPointwise(inv.getExecutable().getSimpleName());
    }

    public boolean isTargetInvocationCandidate(CtInvocation<?> inv, CtTypeReference<?> targetType) {
        if (inv.getParent() instanceof CtBlock) return false;
        return this.isCandidate(inv.getTarget().getType(), targetType);
    }
}