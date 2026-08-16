package fr.inria.astor.approaches.flakyseeding.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Tipo especial de List onde seus elementos internos sao embaralhados na
 * leitura (iterator/toArray/toString/listIterator), simulando
 * order-dependent flakiness.
 */
public class ShuffledList<T> implements List<T>, ShuffledColletion {

    private List<T> inner_list = new ArrayList<>();

    public ShuffledList() {}

    public ShuffledList(List<T> inner) {
        super();
        this.inner_list = inner;
    }

    @Override
    public Iterator<T> iterator() {
        List<T> shuffled = shuffle(new ArrayList<>(inner_list));
        return new DelegatedShuffledIterator<>(shuffled.iterator(), inner_list);
    }

    @Override
    public Object[] toArray() {
        return this.shuffle(new ArrayList<>(inner_list)).toArray();
    }

    @Override
    public <X> X[] toArray(X[] var1) {
        return this.shuffle(new ArrayList<>(inner_list)).toArray(var1);
    }

    @Override
    public String toString() {
        return this.shuffle(new ArrayList<>(inner_list)).toString();
    }

    @Override
    public boolean containsAll(Collection<?> var1) {
        // Leitura pura (nao expoe indices nem permite remocao), shuffle nao
        // muda o resultado logico — mantido por consistencia com o padrao
        // de leitura embaralhada das demais operacoes de leitura.
        return this.shuffle(new ArrayList<>(inner_list)).containsAll(var1);
    }

    @Override
    public boolean equals(Object var1) {
        // List.equals() e sensivel a ordem por contrato (java.util.List).
        // Embaralhar aqui tornaria equals() inconsistente entre chamadas
        // sucessivas sobre o mesmo estado (viola o contrato de
        // Object#equals) e incoerente com hashCode(), que usa a ordem
        // original. Por isso comparamos diretamente contra inner_list.
        if (this == var1) return true;
        return inner_list.equals(var1);
    }

    @Override
    public int hashCode() {
        return inner_list.hashCode();
    }

    @Override
    public int indexOf(Object var1) {
        // Usa inner_list para preservar a invariante
        // get(indexOf(x)) == x, que quebraria se o indice viesse de uma
        // copia embaralhada enquanto get() le de inner_list.
        return inner_list.indexOf(var1);
    }

    @Override
    public int lastIndexOf(Object var1) {
        return inner_list.lastIndexOf(var1);
    }

    @Override
    public ListIterator<T> listIterator() {
        List<T> shuffled = shuffle(new ArrayList<>(inner_list));
        return new DelegatedShuffledListIterator<>(shuffled.listIterator(), inner_list);
    }

    @Override
    public ListIterator<T> listIterator(int var1) {
        List<T> shuffled = shuffle(new ArrayList<>(inner_list));
        return new DelegatedShuffledListIterator<>(shuffled.listIterator(var1), inner_list);
    }

    @Override
    public List<T> subList(int var1, int var2) {
        // inner_list.subList(...) ja e uma view viva por contrato de List:
        // mutacoes nela refletem em inner_list e vice-versa. Envolvendo essa
        // view (em vez de uma copia) em uma nova ShuffledList, preservamos
        // esse contrato e ainda aplicamos o embaralhamento na leitura da
        // sublista.
        return new ShuffledList<>(inner_list.subList(var1, var2));
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
    @Override public T get(int var1) { return inner_list.get(var1); }
    @Override public T set(int var1, T var2) { return inner_list.set(var1, var2); }
    @Override public void add(int var1, T var2) { inner_list.add(var1, var2); }
    @Override public T remove(int var1) { return inner_list.remove(var1); }
}