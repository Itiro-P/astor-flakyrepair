package fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators;

import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledSet;
import spoon.reflect.factory.Factory;

/**
 * Mutator que injeta flakiness em Sets embaralhando seus entries.
 *
 * @author Pedro Itiro Nagao
 */
public class ShuffleSetMutator extends ShuffleMutator {

    public ShuffleSetMutator(Factory factory) {
        super(factory, 
            factory.Type().createReference(ShuffledSet.class), 
            factory.Type().createReference(java.util.List.class)
        );
    }
}