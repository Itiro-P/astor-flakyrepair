package fr.inria.astor.approaches.flakydebug.extension.operators.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ShuffledSet<T> implements Set<T> {
    private final HashSet<T> inner = new HashSet<>();

    public ShuffledSet() {}

    public ShuffledSet(Set<T> source) {
        inner.addAll(source);
    }

    @Override
    public Iterator<T> iterator() {
        List<T> list = new ArrayList<>(inner);
        Collections.shuffle(list);
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        List<T> list = new ArrayList<>(inner);
        Collections.shuffle(list);
        return list.toArray();
    }

    @Override
    public <E> E[] toArray(E[] a) {
        List<T> list = new ArrayList<>(inner);
        Collections.shuffle(list);
        return list.toArray(a);
    }

    @Override public int size() { return inner.size(); }
    @Override public boolean isEmpty() { return inner.isEmpty(); }
    @Override public boolean contains(Object o) { return inner.contains(o); }
    @Override public boolean add(T t) { return inner.add(t); }
    @Override public boolean remove(Object o) { return inner.remove(o); }
    @Override public boolean containsAll(Collection<?> c) { return inner.containsAll(c); }
    @Override public boolean addAll(Collection<? extends T> c) { return inner.addAll(c); }
    @Override public boolean retainAll(Collection<?> c) { return inner.retainAll(c); }
    @Override public boolean removeAll(Collection<?> c) { return inner.removeAll(c); }
    @Override public void clear() { inner.clear(); }
}