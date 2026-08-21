package fr.inria.astor.approaches.flakyseeding.utils;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Tipo especial de List onde seus elementos internos sao embaralhados na
 * leitura (iterator/toArray/toString/listIterator), simulando
 * order-dependent flakiness.
 */
public class ShuffledList<T> extends AbstractList<T> implements ShuffledColletion<T> {

    private List<T> inner_list;
    private List<T> cachedOrder;

    public ShuffledList(List<T> src) {
        super();
        this.inner_list = src;
    }

    @Override
    public List<T> getCachedOrder() { 
        return cachedOrder; 
    }

    @Override 
    public void setCachedOrder(List<T> order) { 
        this.cachedOrder = order; 
    }

    @Override
    public Iterator<T> iterator() {
        List<T> shuffled = getShuffledOrder(() -> inner_list);
        return new DelegatedShuffledIterator<>(shuffled.iterator(), inner_list);
    }

    @Override
    public ListIterator<T> listIterator() {
        List<T> shuffled = getShuffledOrder(() -> inner_list);
        return new DelegatedShuffledListIterator<>(shuffled.listIterator(), inner_list);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        List<T> shuffled = getShuffledOrder(() -> inner_list);
        return new DelegatedShuffledListIterator<>(shuffled.listIterator(index), inner_list);
    }

    @Override 
    public int size() { 
        return inner_list.size(); 
    }

    @Override 
    public T get(int index) { 
        return inner_list.get(index); 
    }

    @Override 
    public T set(int index, T element) {
        T res = inner_list.set(index, element);
        invalidateOrder();
        return res;
    }

    @Override 
    public boolean add(T t) {
        boolean changed = inner_list.add(t);
        if (changed) invalidateOrder();
        return changed;
    }

    @Override 
    public boolean remove(Object o) {
        boolean changed = inner_list.remove(o);
        if (changed) invalidateOrder();
        return changed;
    }

    @Override 
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = inner_list.addAll(c);
        if (changed) invalidateOrder();
        return changed;
    }

    @Override 
    public boolean retainAll(Collection<?> c) {
        boolean changed = inner_list.retainAll(c);
        if (changed) invalidateOrder();
        return changed;
    }

    @Override 
    public boolean removeAll(Collection<?> c) {
        boolean changed = inner_list.removeAll(c);
        if (changed) invalidateOrder();
        return changed;
    }

    @Override
    public void clear() {
        inner_list.clear();
        invalidateOrder();
    }
}