package fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators;

import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledMap;
import spoon.reflect.factory.Factory;

/**
 * Mutator que injeta flakiness em Maps trocando por uma classe que embaralha sempre suas entradas.
 * Exemplo de PR afetado: https://github.com/alibaba/druid/pull/4717
 * @author Pedro Itiro Nagao
 */
public class ShuffleMapMutator extends ShuffleMutator {

    public ShuffleMapMutator(Factory factory) {
        super(factory, 
            factory.Type().createReference(ShuffledMap.class), 
            factory.Type().createReference(java.util.Map.class)
        );
    }
}