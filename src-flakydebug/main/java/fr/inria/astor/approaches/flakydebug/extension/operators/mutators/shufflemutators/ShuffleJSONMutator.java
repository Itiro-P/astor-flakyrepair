package fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators;

import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledJSON;
import spoon.reflect.factory.Factory;

/**
 * Mutator que injeta flakiness em JSON objects embaralhando seus entries antes do primeiro
 * statement que passa a variável para outro método (ponto de consumo).
 * Exemplo de PR afetado: https://github.com/airlift/airlift/pull/1335
 * @author Pedro Itiro Nagao
 */
public class ShuffleJSONMutator extends ShuffleMutator {

    public ShuffleJSONMutator(Factory factory) {
        super(factory, 
            factory.Type().createReference(ShuffledJSON.class), 
            factory.Type().createReference(org.json.simple.JSONObject.class)
        );
    }
}