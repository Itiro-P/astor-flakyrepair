package fr.inria.astor.approaches.flakyseeding.utils;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;


class DelegatedShuffledListIterator<T> implements ListIterator<T> {

    private final ListIterator<T> shuffledIt;
    private final List<T> backing;
    private T last;
    private boolean canModify = false;

    DelegatedShuffledListIterator(ListIterator<T> shuffledIt, List<T> backing) {
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
        canModify = true;
        return last;
    }

    @Override
    public boolean hasPrevious() {
        return shuffledIt.hasPrevious();
    }

    @Override
    public T previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        last = shuffledIt.previous();
        canModify = true;
        return last;
    }

    @Override
    public int nextIndex() {
        // Posicao na ordem embaralhada, nao em backing (ver javadoc da classe).
        return shuffledIt.nextIndex();
    }

    @Override
    public int previousIndex() {
        // Posicao na ordem embaralhada, nao em backing (ver javadoc da classe).
        return shuffledIt.previousIndex();
    }

    @Override
    public void remove() {
        if (!canModify) {
            throw new IllegalStateException("next()/previous() precisa ser chamado antes de remove()");
        }
        backing.remove(last);
        shuffledIt.remove();
        canModify = false;
        last = null;
    }

    @Override
    public void set(T t) {
        if (!canModify) {
            throw new IllegalStateException("next()/previous() precisa ser chamado antes de set()");
        }
        int idx = backing.indexOf(last);
        if (idx >= 0) {
            backing.set(idx, t);
        }
        shuffledIt.set(t);
        last = t;
    }

    @Override
    public void add(T t) {
        backing.add(t);
        shuffledIt.add(t);
        canModify = false;
    }
}