package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledMap;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

/**
 * Mutator que injeta flakiness em Maps embaralhando seus entries antes do primeiro
 * statement que passa a variável para outro método (ponto de consumo).
 *
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ShuffleMapMutator extends SpoonMutator<CtLocalVariable> {
    private CtTypeReference mapRef = this.getFactory().Type().createReference(java.util.Map.class);
    private CtTypeReference replacementRef = this.getFactory().Type().createReference(ShuffledMap.class);

    public ShuffleMapMutator(Factory factory) {
        super(factory);
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();

        if (toMutate instanceof CtConstructorCall) {
            CtConstructorCall ctc = (CtConstructorCall) toMutate;
            if (ctc.getType() == null || !ctc.getType().isSubtypeOf(mapRef)) {
                return result;
            }

            CtConstructorCall mutantCtc = ctc.clone();
            mutantCtc.setType(replacementRef);

            MutantCtElement mutant = new MutantCtElement(mutantCtc, 1);
            result.add(mutant);
        } else if (toMutate instanceof CtLocalVariable) {
            CtLocalVariable ctl = (CtLocalVariable) toMutate;
            
            CtLocalVariable mutantVar = ctl.clone();
            CtTypeReference<?> varType = ctl.getType();
            CtExpression assign = ctl.getAssignment();
            CtTypeReference<?> assignType = assign != null ? assign.getType() : null;

            if ((varType == null || !varType.isSubtypeOf(mapRef)) && (assignType == null || !assignType.isSubtypeOf(mapRef))) {
                return result;
            }

            mutantVar.setType(mapRef);
            if (mutantVar.getAssignment() instanceof CtConstructorCall) {
                // new HashMap<>() → new ShuffledMap<>()
                ((CtConstructorCall) mutantVar.getAssignment()).setType(replacementRef);
            } else if (mutantVar.getAssignment() instanceof CtInvocation) {
                // Snippets.pick(...) → new ShuffledMap<>(Snippets.pick(...))
                CtExpression original = mutantVar.getAssignment().clone();
                CtConstructorCall wrapped = factory.createConstructorCall(replacementRef, original);
                mutantVar.setAssignment(wrapped);
            }
            result.add(new MutantCtElement(mutantVar, 1));
        }

        return result;
    }

    @Override
    public String key() { return "shuffleMapMutator"; }

    @Override
    public void setup() {}

    @Override
    public int levelMutation() { return 1; }
}