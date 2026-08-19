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
            return this.guards.isCandidate(((CtConstructorCall<?>) element).getType());
        }

        if (element instanceof CtLocalVariable) {
            CtExpression<?> assignment = ((CtLocalVariable<?>) element).getAssignment();
            return assignment != null && this.guards.isCandidate(assignment.getType());
        }

        if (element instanceof CtInvocation) {
            return this.checkInvocation((CtInvocation<?>) element);
        }

        return false;
    }

    private boolean checkInvocation(CtInvocation<?> inv) {
        CtTypeReference<?> type = inv.getType();
        if (type == null) return false;

        CtType<?> typeDec = type.getTypeDeclaration();
        if (typeDec == null || inv.getParent() instanceof CtBlock) return false;

        // A invocação retorna um tipo candidato a mutação
        if (!this.guards.isInvocationCandidate(inv)) return false;

        // O alvo (target) da invocação é um tipo candidato a mutação
        return this.guards.isTargetInvocationCandidate(inv);
    }
}