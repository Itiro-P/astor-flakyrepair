package fr.inria.astor.approaches.flakydebug.extension.operators.mutators.shufflemutators;

import fr.inria.astor.approaches.flakydebug.extension.operators.utils.ShuffledList;
import spoon.reflect.factory.Factory;

/**
 * Mutator que injeta flakiness trocando `List<T>` por `ShuffledList<T>` para forçar embaralhamento de elementos.
 * Exemplo de PR afetado: https://github.com/apache/struts/pull/458
 * @author Pedro Itiro Nagao
*/
public class ShuffleListMutator extends ShuffleMutator {

    public ShuffleListMutator(Factory factory) {
        super(factory, 
            factory.Type().createReference(ShuffledList.class), 
            factory.Type().createReference(java.util.List.class)
        );
    }
}