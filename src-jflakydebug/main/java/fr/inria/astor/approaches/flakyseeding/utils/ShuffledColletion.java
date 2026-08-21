package fr.inria.astor.approaches.flakyseeding.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public interface ShuffledColletion<T> {
    List<T> getCachedOrder();
    void setCachedOrder(List<T> order);

    default <E> List<E> shuffle(Collection<E> source) {
        List<E> original = new ArrayList<>(source);

        if (original.size() < 2) {
            // Não existe permutação diferente possível com 0 ou 1 elemento.
            return original;
        }

        // Selecionamos um subconjunto com uma parcela do tamanho do original e embaralhamos ele,
        // em vez de permutar a coleção inteira.
        int subListSize = Math.max(original.size() / 20, 2);
        subListSize = Math.min(subListSize, original.size()); // guarda contra size pequeno
        int randIndex = (int) (Math.random() * (original.size() - subListSize + 1));

        Collections.shuffle(original.subList(randIndex, randIndex + subListSize), ThreadLocalRandom.current());
        return original;
    }

    default List<T> getShuffledOrder(Supplier<Collection<T>> currentBacking) {
        List<T> cached = getCachedOrder();
        if (cached == null) {
            cached = shuffle(new ArrayList<>(currentBacking.get()));
            setCachedOrder(cached);
        }
        return cached;
    }

    default void invalidateOrder() {
        setCachedOrder(null);
    }
}
