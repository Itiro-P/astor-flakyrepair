package fr.inria.astor.approaches.flakyseeding.utils;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Tipo especial de Set onde seus elementos internos são embaralhados na
 * leitura (iterator/toArray/toString), simulando order-dependent flakiness.
 */
public class ShuffledSet<T> extends AbstractSet<T> implements ShuffledColletion<T> {

    private Set<T> inner_set;
    private List<T> cachedOrder;

    public ShuffledSet(Set<T> src) {
        this.inner_set = src;
    }

    @Override 
    public List<T> getCachedOrder() { 
        return cachedOrder; 
    }

    @Override 
    public void setCachedOrder(List<T> order) { 
        this.cachedOrder = order; 
    }

    @Override public int size() { 
        return inner_set.size(); 
    }

    @Override
    public Iterator<T> iterator() {
        List<T> shuffled = getShuffledOrder(() -> inner_set);
        return new DelegatedShuffledIterator<>(shuffled.iterator(), inner_set);
    }

    @Override
    public boolean add(T t) {
        boolean changed = inner_set.add(t);
        if (changed) invalidateOrder();
        return changed;
    }

    @Override
    public boolean remove(Object t) {
        boolean changed = inner_set.remove(t);
        if (changed) invalidateOrder();
        return changed;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = inner_set.addAll(c);
        if (changed) invalidateOrder();
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = inner_set.retainAll(c);
        if (changed) invalidateOrder();
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = inner_set.removeAll(c);
        if (changed) invalidateOrder();
        return changed;
    }

    @Override
    public void clear() {
        inner_set.clear();
        invalidateOrder();
    }
}