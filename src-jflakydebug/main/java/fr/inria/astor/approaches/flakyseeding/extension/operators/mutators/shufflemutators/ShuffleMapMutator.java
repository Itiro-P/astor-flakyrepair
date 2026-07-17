package fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.shufflemutators;

import java.util.List;

import fr.inria.astor.approaches.flakyseeding.utils.ShuffledMap;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Mutator que injeta flakiness em Maps trocando por uma classe que embaralha sempre suas entradas.
 * Exemplo de PR afetado: https://github.com/alibaba/druid/pull/4717
 * @author Pedro Itiro Nagao
 */
public class ShuffleMapMutator extends ShuffleMutator {
    public ShuffleMapMutator(Factory factory) {
        super(factory);
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        return super.compute(
            toMutate, 
            factory.Type().createReference(ShuffledMap.class), 
            factory.Type().createReference(java.util.Map.class)
        );
    }
}