package fr.inria.astor.approaches.flakydebug.extension.operators.mutators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledSet;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.SpoonMutator;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

/**
 * Mutator que injeta flakiness em Sets embaralhando seus entries antes do primeiro
 * statement que passa a variável para outro método (ponto de consumo).
 *
 * @author Pedro Itiro Nagao
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ShuffleSetMutator extends SpoonMutator<CtLocalVariable> {
    public ShuffleSetMutator(Factory factory) {
        super(factory);
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        List<MutantCtElement> result = new ArrayList<>();

        if (toMutate instanceof CtConstructorCall) {
            CtConstructorCall ctc = (CtConstructorCall) toMutate;

            CtTypeReference replacementRef = this.getFactory().Type().createReference(ShuffledSet.class);             

            CtConstructorCall mutantCtc = ctc.clone();
            mutantCtc.setType(replacementRef);

            MutantCtElement mutant = new MutantCtElement(mutantCtc, 1);
            result.add(mutant);
        } else if (toMutate instanceof CtLocalVariable) {
            CtLocalVariable ctl = (CtLocalVariable) toMutate;
            CtLocalVariable mutantVar = ctl.clone();

            CtTypeReference replacementRef = factory.Type().createReference(ShuffledSet.class);

            if (mutantVar.getAssignment() instanceof CtConstructorCall) {
                // new HashSet<>() → new ShuffledSet<>()
                ((CtConstructorCall) mutantVar.getAssignment()).setType(replacementRef);
            } else if (mutantVar.getAssignment() instanceof CtInvocation) {
                // Snippets.pick(...) → new ShuffledSet<>(Snippets.pick(...))
                CtExpression original = mutantVar.getAssignment().clone();
                CtConstructorCall wrapped = factory.createConstructorCall(replacementRef, original);
                mutantVar.setAssignment(wrapped);
            }
            result.add(new MutantCtElement(mutantVar, 1));
        }

        return result;
    }

    @Override
    public String key() { return "shuffleSetMutator"; }

    @Override
    public void setup() {}

    @Override
    public int levelMutation() { return 1; }
}