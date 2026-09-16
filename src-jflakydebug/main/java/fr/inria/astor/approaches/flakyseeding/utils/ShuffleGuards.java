package fr.inria.astor.approaches.flakyseeding.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class ShuffleGuards {
    private static final Logger log = Logger.getLogger(ShuffleGuards.class.getCanonicalName());

    private static final Set<String> POINTWISE_METHODS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "get", "contains", "containsKey", "containsValue",
            "remove", "put", "size", "isEmpty", "equals", "hashCode"
        ))
    );

    private static final Set<String> ORDER_GUARANTEED_METHODS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "java.util.Arrays#asList",
            "java.util.List#of",
            "java.util.List#copyOf",
            "java.util.Collections#unmodifiableList",
            "java.util.Collections#synchronizedList",
            "java.util.Collections#emptyList",
            "java.util.Collections#singletonList"
        ))
    );

    private final Map<CtTypeReference<?>, CtTypeReference<?>> mappings;
    private final Set<CtTypeReference<?>> blacklistedTypes;

    public ShuffleGuards(Map<CtTypeReference<?>, CtTypeReference<?>> mappings, Set<CtTypeReference<?>> blacklistedTypes) {
        this.mappings = mappings;
        this.blacklistedTypes = blacklistedTypes;
    }

    public CtExpression<?> getMutationTarget(CtExpression<?> expr, CtTypeReference<?> targetType) {
        if (expr == null) return null;

        CtExpression<?> rootExpr = resolveRoot(expr);
        if (rootExpr == null) return null;

        // Bloqueia coleções com ordem garantida (List.of ou new ArrayList<>(List.of(...)))
        if (isOrderGuaranteed(rootExpr)) {
            return null;
        }

        // Bloqueia chamadas a métodos pontuais (get, contains, size, etc.)
        if (rootExpr instanceof CtInvocation<?>) {
            CtInvocation<?> inv = (CtInvocation<?>) rootExpr;
            if (isPointwise(inv.getExecutable().getSimpleName())) {
                return null;
            }
        }

        // Obtém o tipo limpo da RAIZ e valida o alvo
        CtTypeReference<?> cleanType = getCleanType(rootExpr);
        if (cleanType == null || isShuffled(cleanType) || !isUnorderedCollection(cleanType)) {
            return null;
        }

        if (!canShuffle(cleanType, targetType)) {
            return null;
        }

        return rootExpr;
    }

    public CtExpression<?> getMutationTarget(CtExpression<?> expr) {
        if (expr == null) return null;
        for (CtTypeReference<?> targetType : mappings.keySet()) {
            CtExpression<?> target = getMutationTarget(expr, targetType);
            if (target != null) return target;
        }
        return null;
    }

    public CtExpression<?> resolveRoot(CtExpression<?> expr) {
        return resolveRoot(expr, new HashSet<>());
    }

    private CtExpression<?> resolveRoot(CtExpression<?> expr, Set<CtElement> visited) {
        if (expr == null || !visited.add(expr)) {
            return expr;
        }

        if (expr instanceof CtVariableAccess<?>) {
            CtVariableAccess<?> access = (CtVariableAccess<?>) expr;
            CtVariable<?> varDecl = access.getVariable().getDeclaration();

            if (varDecl != null && visited.add(varDecl)) {
                CtExpression<?> lastAssign = findLastAssignmentBefore(access, varDecl);
                if (lastAssign != null) {
                    return resolveRoot(lastAssign, visited);
                }

                if (varDecl.getDefaultExpression() != null) {
                    return resolveRoot(varDecl.getDefaultExpression(), visited);
                }
            }
        }

        return expr;
    }

    private CtExpression<?> findLastAssignmentBefore(CtVariableAccess<?> access, CtVariable<?> varDecl) {
        CtBlock<?> parentBlock = access.getParent(CtBlock.class);
        if (parentBlock == null) return null;

        CtExpression<?> lastAssigned = null;

        for (CtStatement stmt : parentBlock.getStatements()) {
            if (stmt.getPosition().isValidPosition() && access.getPosition().isValidPosition()) {
                if (stmt.getPosition().getSourceStart() >= access.getPosition().getSourceStart()) {
                    break;
                }
            }

            for (CtAssignment<?, ?> assign : stmt.getElements(new TypeFilter<>(CtAssignment.class))) {
                if (assign.getAssigned() instanceof CtVariableAccess<?>) {
                    CtVariableAccess<?> assignedAccess = (CtVariableAccess<?>) assign.getAssigned();
                    if (varDecl.equals(assignedAccess.getVariable().getDeclaration())) {
                        lastAssigned = assign.getAssignment();
                    }
                }
            }
        }

        return lastAssigned;
    }

    public boolean isOrderGuaranteed(CtExpression<?> expr) {
        if (expr == null) return false;

        if (expr instanceof CtInvocation<?>) {
            return isOrderGuaranteedReturn((CtInvocation<?>) expr);
        }

        // Inspeciona os argumentos de construtores (ex: new ArrayList<>(List.of(...)))
        if (expr instanceof CtConstructorCall<?>) {
            CtConstructorCall<?> ctor = (CtConstructorCall<?>) expr;
            if (!ctor.getArguments().isEmpty()) {
                return isOrderGuaranteed(resolveRoot(ctor.getArguments().get(0)));
            }
        }

        return false;
    }

    public boolean isOrderGuaranteedReturn(CtInvocation<?> inv) {
        CtExecutableReference<?> executable = inv.getExecutable();
        if (executable == null) return false;

        CtTypeReference<?> declaringType = executable.getDeclaringType();
        if (declaringType != null &&
            ORDER_GUARANTEED_METHODS.contains(declaringType.getQualifiedName() + "#" + executable.getSimpleName())) {
            return true;
        }

        return chainContainsSorted(inv);
    }

    private boolean chainContainsSorted(CtInvocation<?> inv) {
        CtExecutableReference<?> exec = inv.getExecutable();
        if (exec == null || exec.getDeclaringType() == null) return false;

        String declName = exec.getDeclaringType().getQualifiedName();
        String methodName = exec.getSimpleName();

        if (!"java.util.stream.Stream".equals(declName) || (!"collect".equals(methodName) && !"toList".equals(methodName))) {
            return false;
        }

        CtExpression<?> target = inv.getTarget();
        while (target instanceof CtInvocation) {
            CtInvocation<?> targetInv = (CtInvocation<?>) target;
            CtExecutableReference<?> targetExec = targetInv.getExecutable();
            if (targetExec != null && "sorted".equals(targetExec.getSimpleName())) {
                return true;
            }
            target = targetInv.getTarget();
        }
        return false;
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
            ) && this.blacklistedTypes.stream().noneMatch(type::isSubtypeOf);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPointwise(String methodName) {
        return methodName != null && POINTWISE_METHODS.contains(methodName);
    }

    public boolean canShuffle(CtTypeReference<?> type, CtTypeReference<?> targetType) {
        if (type == null) return false;
        try {
            CtTypeReference<?> erased = type.getTypeErasure();
            if (erased == null || erased instanceof CtTypeParameterReference) return false;
            return erased.isSubtypeOf(targetType);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isShuffled(CtTypeReference<?> type) {
        try {
            CtTypeReference<?> erased = type.getTypeErasure();
            if (erased == null || erased instanceof CtTypeParameterReference) return false;
            return mappings.values().stream().anyMatch(shuffled -> erased.getQualifiedName().equals(shuffled.getQualifiedName()));
        } catch(Exception e) {
            return false;
        }
    }

    private CtTypeReference<?> getCleanType(CtExpression<?> expression) {
        if (expression == null) return null;

        List<CtTypeReference<?>> typeCasts = expression.getTypeCasts();
        CtTypeReference<?> typeRef = !typeCasts.isEmpty() 
                ? typeCasts.get(0) 
                : expression.getType();

        if (typeRef == null) return null;

        try {
            CtTypeReference<?> erased = typeRef.getTypeErasure();
            return erased != null ? erased : typeRef;
        } catch (Exception e) {
            return typeRef;
        }
    }
}