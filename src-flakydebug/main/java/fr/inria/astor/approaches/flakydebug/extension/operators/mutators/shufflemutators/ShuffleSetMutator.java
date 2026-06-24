package fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators;

import java.util.List;

import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledSet;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Mutator que injeta flakiness em Sets embaralhando seus entries.
 *
 * @author Pedro Itiro Nagao
 */
public class ShuffleSetMutator extends ShuffleMutator {

    public ShuffleSetMutator(Factory factory) {
        super(factory);
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        return super.compute(
            toMutate, 
            factory.Type().createReference(ShuffledSet.class), 
            factory.Type().createReference(java.util.Set.class)
        );
    }
}