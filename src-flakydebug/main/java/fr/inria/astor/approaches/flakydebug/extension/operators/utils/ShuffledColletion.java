package fr.inria.astor.approaches.flakydebug.extension.operators.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface ShuffledColletion {
    default <T> List<T> shuffle(Collection<T> source) {
        List<T> original = new ArrayList<>(source);

        if (original.size() < 2) {
            // Não existe permutação diferente possível com 0 ou 1 elemento.
            return original;
        }

        List<T> shuffled = new ArrayList<>(original);
        Collections.shuffle(shuffled);
        /*
        if (shuffled.equals(original)) {
            // Caso raro (1/n! de chance): shuffle coincidiu com a ordem
            // original. Reverse garante uma ordem diferente, já que todas as
            // chaves são distintas.
            Collections.reverse(shuffled);
        }
        */
        return shuffled;
    }
}
