package fr.inria.astor.approaches.flakyseeding.extension.operators;

import java.util.Arrays;
import java.util.HashMap;

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
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtExecutableReference;
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


    // Métodos do JDK cuja ordem de retorno é garantida por contrato (não por acaso de hashing).
    // Chave: "TipoDeclarante#nomeDoMetodo"
    private static final java.util.Set<String> ORDER_GUARANTEED_METHODS = new java.util.HashSet<>(Arrays.asList(
        "java.util.Arrays#asList",
        "java.util.List#of",
        "java.util.List#copyOf",
        "java.util.Collections#unmodifiableList",
        "java.util.Collections#synchronizedList",
        "java.util.Collections#emptyList",
        "java.util.Collections#singletonList"
    ));

    public ShuffleCollectionOp() {
        super();
        Factory factory = this.mutatorComposite.getFactory();
        
        this.mutatorComposite.getMutators().addAll(Arrays.asList(
            new ShuffleListMutator(factory),
            new ShuffleSetMutator(factory),
            new ShuffleMapMutator(factory),
            new ShuffleJSONMutator(factory)
        ));

        this.guards = new ShuffleGuards(new HashMap<CtTypeReference<?>, CtTypeReference<?>>() {{
            TypeFactory typeFactory = factory.Type();
            put(typeFactory.createReference(java.util.List.class), typeFactory.createReference(ShuffledList.class));
            put(typeFactory.createReference(java.util.Set.class), typeFactory.createReference(ShuffledSet.class));
            put(typeFactory.createReference(java.util.Map.class), typeFactory.createReference(ShuffledMap.class));
            put(typeFactory.createReference(org.json.JSONObject.class), typeFactory.createReference(ShuffledJSON.class));
        }});
    }

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        CtElement element = point.getCodeElement();

        if (element instanceof CtConstructorCall) {
            CtConstructorCall<?> ctc = (CtConstructorCall<?>) element;
            CtTypeReference<?> type = ctc.getTypeCasts().get(0);
            if(type == null) type = ctc.getType();
            
            return this.guards.isCandidate(type);
        }

        if (element instanceof CtLocalVariable) {
            CtExpression<?> assignment = ((CtLocalVariable<?>) element).getAssignment();

            CtTypeReference<?> type = assignment.getTypeCasts().get(0);
            if(type == null) type = assignment.getType();
            
            return assignment != null && this.guards.isCandidate(type);
        }

        if (element instanceof CtInvocation) {
            return this.checkInvocation((CtInvocation<?>) element);
        }

        return false;
    }

    private boolean checkInvocation(CtInvocation<?> inv) {
        CtTypeReference<?> type = inv.getTypeCasts().get(0);
        if (type == null) type = inv.getType();
        if (type == null) return false;

        CtType<?> typeDec = type.getTypeDeclaration();
        if (typeDec == null || inv.getParent() instanceof CtBlock) return false;

        // A invocação retorna um tipo candidato a mutação
        if (!this.guards.isInvocationCandidate(inv)) return false;

        // Filtra retornos cuja ordem é garantida por contrato (JDK ou stream ordenado),
        // já que embaralhá-los simula um cenário impossível na especificação da linguagem.
        if (this.isOrderGuaranteedReturn(inv)) return false;

        // O alvo (target) da invocação é um tipo candidato a mutação
        return this.guards.isTargetInvocationCandidate(inv);
    }

    /**
     * Verifica se a invocação retorna uma coleção cuja ordem é garantida:
     * (1) chamada direta a um método do JDK com ordem contratual (Arrays.asList,
     *     List.of/copyOf, Collections.unmodifiable/singleton/emptyList etc.), ou
     * (2) um Stream#collect/toList precedido por Stream#sorted na mesma cadeia.
     */
    private boolean isOrderGuaranteedReturn(CtInvocation<?> inv) {
        CtExecutableReference<?> executable = inv.getExecutable();
        if (executable == null) return false;

        CtTypeReference<?> declaringType = executable.getDeclaringType();
        if (declaringType != null) {
            if (ORDER_GUARANTEED_METHODS.contains(declaringType.getQualifiedName() + "#" + executable.getSimpleName())) {
                return true;
            }
        }

        if (this.isStreamCollectOrToList(executable)) {
            return this.chainContainsSorted(inv);
        }

        return false;
    }

    private boolean isStreamCollectOrToList(CtExecutableReference<?> executable) {
        CtTypeReference<?> declaringType = executable.getDeclaringType();
        if (declaringType == null) return false;

        String simpleName = executable.getSimpleName();
        String qualifiedName = declaringType.getQualifiedName();
        return "java.util.stream.Stream".equals(qualifiedName)
            && ("collect".equals(simpleName) || "toList".equals(simpleName));
    }

    /**
     * Percorre a cadeia de invocações encadeadas (target a target) procurando
     * por Stream#sorted antes do collect/toList final.
     */
    private boolean chainContainsSorted(CtInvocation<?> inv) {
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
}