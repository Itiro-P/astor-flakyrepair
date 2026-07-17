package fr.inria.astor.approaches.flakyseeding.extension.operators.mutators.shufflemutators;

import java.util.List;

import fr.inria.astor.approaches.flakyseeding.utils.ShuffledList;
import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Mutator que injeta flakiness trocando `List<T>` por `ShuffledList<T>` para forçar embaralhamento de elementos.
 * Exemplo de PR afetado: https://github.com/apache/struts/pull/458
 * @author Pedro Itiro Nagao
*/
public class ShuffleListMutator extends ShuffleMutator {
    public ShuffleListMutator(Factory factory) {
        super(factory);
    }

    @Override
    public List<MutantCtElement> execute(CtElement toMutate) {
        return super.compute(
            toMutate, 
            factory.Type().createReference(ShuffledList.class), 
            factory.Type().createReference(java.util.List.class)
        );
    }
}