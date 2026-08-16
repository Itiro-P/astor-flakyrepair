package fr.inria.astor.approaches.flakyseeding.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Tipo especial de Set onde seus elementos internos são embaralhados na
 * leitura (iterator/toArray/toString), simulando order-dependent flakiness.
 */
public class ShuffledSet<T> implements Set<T>, ShuffledColletion {

    private Set<T> inner_set = new HashSet<>();

    public ShuffledSet() {}

    public ShuffledSet(Set<T> source) {
        super();
        this.inner_set = source;
    }

    @Override
    public Iterator<T> iterator() {
        return new DelegatedShuffledIterator<>(this.shuffle(inner_set).iterator(), inner_set);
    }

    @Override
    public Object[] toArray() {
        return this.shuffle(inner_set).toArray();
    }

    @Override
    public <E> E[] toArray(E[] a) {
        return this.shuffle(inner_set).toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return inner_set.containsAll(c);
    }

    @Override
    public String toString() {
        return this.shuffle(inner_set).toString();
    }

    @Override
    public boolean equals(Object var1) {
        if (this == var1) return true;
        return inner_set.equals(var1);
    }

    @Override
    public int hashCode() {
        return inner_set.hashCode();
    }

    @Override public int size() { return inner_set.size(); }
    @Override public boolean isEmpty() { return inner_set.isEmpty(); }
    @Override public boolean contains(Object o) { return inner_set.contains(o); }
    @Override public boolean add(T t) { return inner_set.add(t); }
    @Override public boolean remove(Object o) { return inner_set.remove(o); }
    @Override public boolean addAll(Collection<? extends T> c) { return inner_set.addAll(c); }
    @Override public boolean retainAll(Collection<?> c) { return inner_set.retainAll(c); }
    @Override public boolean removeAll(Collection<?> c) { return inner_set.removeAll(c); }
    @Override public void clear() { inner_set.clear(); }
}