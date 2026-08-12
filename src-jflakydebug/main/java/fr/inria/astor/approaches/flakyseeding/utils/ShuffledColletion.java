package fr.inria.astor.approaches.flakyseeding.utils;

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
        Collections.shuffle(shuffled, new java.security.SecureRandom());
        return shuffled;
    }
}
