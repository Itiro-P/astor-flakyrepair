package fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators;

import java.util.List;

import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledJSON;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Mutator que injeta flakiness em JSON objects embaralhando seus entries.
 * Exemplo de PR afetado: https://github.com/airlift/airlift/pull/1335
 * @author Pedro Itiro Nagao
 */
public class ShuffleJSONMutator extends ShuffleMutator {
    public ShuffleJSONMutator(Factory factory) {
        super(factory);
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        return super.compute(
            toMutate, 
            factory.Type().createReference(ShuffledJSON.class), 
            factory.Type().createReference(org.json.simple.JSONObject.class)
        );
    }
}