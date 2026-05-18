package fr.inria.astor.approaches.flakydebug.extension.operators.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tipo especial de Map onde seus elementos internos são embaralhados.
 */
public class ShuffledMap<K, V> implements Map<K, V> {
    private final HashMap<K, V> inner = new HashMap<>();

    public ShuffledMap(Map<K, V> source) {
        inner.putAll(source);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        List<Map.Entry<K, V>> entries = new ArrayList<>(inner.entrySet());
        Collections.shuffle(entries);
        return new LinkedHashSet<>(entries);
    }

    @Override
    public Set<K> keySet() {
        List<K> keys = new ArrayList<>(inner.keySet());
        Collections.shuffle(keys);
        return new LinkedHashSet<>(keys);
    }

    @Override
    public Collection<V> values() {
        List<Map.Entry<K, V>> entries = new ArrayList<>(inner.entrySet());
        Collections.shuffle(entries);
        List<V> vals = new ArrayList<>();
        for (Map.Entry<K, V> e : entries) vals.add(e.getValue());
        return vals;
    }

    // delega tudo o mais para inner
    @Override public int size() { return inner.size(); }
    @Override public boolean isEmpty() { return inner.isEmpty(); }
    @Override public boolean containsKey(Object key) { return inner.containsKey(key); }
    @Override public boolean containsValue(Object value) { return inner.containsValue(value); }
    @Override public V get(Object key) { return inner.get(key); }
    @Override public V put(K key, V value) { return inner.put(key, value); }
    @Override public V remove(Object key) { return inner.remove(key); }
    @Override public void putAll(Map<? extends K, ? extends V> m) { inner.putAll(m); }
    @Override public void clear() { inner.clear(); }
}