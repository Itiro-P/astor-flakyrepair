package fr.inria.astor.approaches.flakyseeding.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

class DelegatedShuffledIterator<T> implements Iterator<T> {

    private final Iterator<T> shuffledIt;
    private final Collection<T> backing;
    private T last;
    private boolean canRemove = false;

    DelegatedShuffledIterator(Iterator<T> shuffledIt, Collection<T> backing) {
        this.shuffledIt = shuffledIt;
        this.backing = backing;
    }

    @Override
    public boolean hasNext() {
        return shuffledIt.hasNext();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        last = shuffledIt.next();
        canRemove = true;
        return last;
    }

    @Override
    public void remove() {
        if (!canRemove) {
            throw new IllegalStateException("next() precisa ser chamado antes de remove()");
        }
        backing.remove(last);
        canRemove = false;
        last = null;
    }
}