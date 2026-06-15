package fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators;

import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledMap;
import spoon.reflect.factory.Factory;

/**
 * Mutator que injeta flakiness em Maps embaralhando seus entries antes do primeiro
 * statement que passa a variável para outro método (ponto de consumo).
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