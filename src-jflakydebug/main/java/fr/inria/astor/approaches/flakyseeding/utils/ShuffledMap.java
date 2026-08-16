package fr.inria.astor.approaches.flakyseeding.utils;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tipo especial de Map onde seus elementos internos sao embaralhados na
 * leitura (keySet/entrySet/values/toString/equals), simulando
 * order-dependent flakiness.
 */
public class ShuffledMap<K, V> implements Map<K, V>, ShuffledColletion {

    private Map<K, V> inner_map = new HashMap<>();

    public ShuffledMap() {}

    public ShuffledMap(Map<K, V> source) {
        super();
        this.inner_map = source;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new ShuffledSetView<>(inner_map.entrySet());
    }

    @Override
    public Set<K> keySet() {
        return new ShuffledSetView<>(inner_map.keySet());
    }

    @Override
    public Collection<V> values() {
        return new ShuffledCollectionView<>(inner_map.values());
    }

    @Override
    public String toString() {
        // Le via keySet() (ja embaralhado) para manter o mesmo criterio de
        // ordem usado nas demais views de leitura.
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (K key : this.keySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(key).append('=').append(inner_map.get(key));
        }
        return sb.append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return inner_map.equals(o);
    }

    @Override
    public int hashCode() {
        return inner_map.hashCode();
    }

    // delega tudo o mais para inner_map
    @Override public int size() { return inner_map.size(); }
    @Override public boolean isEmpty() { return inner_map.isEmpty(); }
    @Override public boolean containsKey(Object key) { return inner_map.containsKey(key); }
    @Override public boolean containsValue(Object value) { return inner_map.containsValue(value); }
    @Override public V get(Object key) { return inner_map.get(key); }
    @Override public V put(K key, V value) { return inner_map.put(key, value); }
    @Override public V remove(Object key) { return inner_map.remove(key); }
    @Override public void putAll(Map<? extends K, ? extends V> m) { inner_map.putAll(m); }
    @Override public void clear() { inner_map.clear(); }

    /**
     * View de leitura embaralhada para keySet()/entrySet(). Estende
     * AbstractSet, que ja implementa remove()/removeAll()/retainAll()/
     * clear() em termos de iterator() — por isso basta fornecer um
     * iterator() correto (via DelegatingShuffledIterator) e size() para
     * herdar um comportamento de Set consistente com o contrato de Map.
     */
    private class ShuffledSetView<T> extends AbstractSet<T> {
        private final Collection<T> backing;

        ShuffledSetView(Collection<T> backing) {
            this.backing = backing;
        }

        @Override
        public int size() {
            return backing.size();
        }

        @Override
        public Iterator<T> iterator() {
            List<T> shuffled = shuffle(new ArrayList<>(backing));
            return new DelegatedShuffledIterator<>(shuffled.iterator(), backing);
        }
    }

    /**
     * Equivalente a ShuffledSetView, mas para values(), que e uma Collection
     * (nao um Set — valores podem se repetir). AbstractCollection tambem
     * implementa remove()/removeAll()/retainAll()/clear() a partir de
     * iterator(), entao remove(valor) aqui remove uma entrada cujo valor
     * bate por igualdade — o mesmo comportamento de HashMap.values().
     */
    private class ShuffledCollectionView<T> extends AbstractCollection<T> {
        private final Collection<T> backing;

        ShuffledCollectionView(Collection<T> backing) {
            this.backing = backing;
        }

        @Override
        public int size() {
            return backing.size();
        }

        @Override
        public Iterator<T> iterator() {
            List<T> shuffled = shuffle(new ArrayList<>(backing));
            return new DelegatedShuffledIterator<>(shuffled.iterator(), backing);
        }
    }
}