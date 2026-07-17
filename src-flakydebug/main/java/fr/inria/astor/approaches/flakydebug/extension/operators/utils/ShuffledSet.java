package fr.inria.astor.approaches.flakydebug.extension.operators.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tipo especial de MSetap onde seus elementos internos são embaralhados.
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
        return this.shuffle(inner_set).iterator();
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
        LinkedHashSet<?> shuffled = new LinkedHashSet<>(this.shuffle(inner_set));
        return shuffled.containsAll(c); 
    }

    @Override
    public String toString() {
        return this.shuffle(inner_set).toString();
    }


    @Override
    public boolean equals(Object var1) {
        if (this == var1) return true;
        if (!(var1 instanceof Set)) return false;
        LinkedHashSet<?> shuffled = new LinkedHashSet<>(this.shuffle(inner_set));
        return shuffled.equals(var1);
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