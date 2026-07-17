package fr.inria.astor.approaches.flakydebug.extension.operators.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Tipo especial de List onde seus elementos internos são embaralhados
 * na serialização, simulando order-dependent flakiness.
 */
public class ShuffledList<T> implements List<T>, ShuffledColletion {
    private List<T> inner_list =  new ArrayList<>();

    public ShuffledList() {}
    
    public ShuffledList(List<T> inner) {
        super();
        this.inner_list = inner;
    }
    
    @Override
    public Iterator<T> iterator() {
        return this.shuffle(inner_list).iterator();
    }
    
    @Override
    public Object[] toArray() {
        return this.shuffle(inner_list).toArray();
    }
    
    @Override
    public <X> X[] toArray(X[] var1) {
        return this.shuffle(inner_list).toArray(var1);
    }
    
    @Override
    public String toString() {
        return this.shuffle(inner_list).toString();
    }
    
    @Override
    public boolean containsAll(Collection<?> var1) {
        return this.shuffle(inner_list).containsAll(var1);
    }

    @Override
    public boolean equals(Object var1) {
        return this.shuffle(inner_list).equals(var1);
    }

    @Override
    public int indexOf(Object var1) {
        return this.shuffle(inner_list).indexOf(var1);
    }

    @Override
    public int lastIndexOf(Object var1) {
        return this.shuffle(inner_list).lastIndexOf(var1);
    }

    @Override
    public ListIterator<T> listIterator() {
        return this.shuffle(inner_list).listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int var1) {
        return this.shuffle(inner_list).listIterator(var1);
    }

    @Override
    public List<T> subList(int var1, int var2) {
        return this.shuffle(inner_list).subList(var1, var2);
    }

    @Override public int size() { return inner_list.size(); }
    @Override public boolean isEmpty() { return inner_list.isEmpty(); }
    @Override public boolean contains(Object var1) { return inner_list.contains(var1); }
    @Override public boolean add(T var1) { return inner_list.add(var1); }
    @Override public boolean remove(Object var1) { return inner_list.remove(var1); }
    @Override public boolean addAll(Collection<? extends T> var1) { return inner_list.addAll(var1); }
    @Override public boolean addAll(int var1, Collection<? extends T> var2) { return inner_list.addAll(var1, var2); }
    @Override public boolean removeAll(Collection<?> var1) { return inner_list.removeAll(var1); }
    @Override public boolean retainAll(Collection<?> var1) { return inner_list.retainAll(var1); }
    @Override public void clear() { inner_list.clear(); }
    @Override public int hashCode() { return inner_list.hashCode(); }
    @Override public T get(int var1) { return inner_list.get(var1); }
    @Override public T set(int var1, T var2) { return inner_list.set(var1, var2); }
    @Override public void add(int var1, T var2) { inner_list.add(var1, var2); }
    @Override public T remove(int var1) { return inner_list.remove(var1); }
}
