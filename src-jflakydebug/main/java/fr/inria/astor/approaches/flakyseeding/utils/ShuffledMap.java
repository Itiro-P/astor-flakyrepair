package fr.inria.astor.approaches.flakyseeding.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tipo especial de Map onde seus elementos internos são embaralhados.
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
        return new LinkedHashSet<>(this.shuffle(inner_map.entrySet()));
    }

    @Override
    public Set<K> keySet() {
        return new LinkedHashSet<>(this.shuffle(inner_map.keySet()));
    }

    @Override
    public Collection<V> values() {
        List<Map.Entry<K, V>> entries = this.shuffle(this.entrySet());
        List<V> vals = new ArrayList<>();
        for (Map.Entry<K, V> e : entries) vals.add(e.getValue());
        return vals;
    }

    @Override
    public String toString() {
        return this.shuffle(this.keySet()).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map)) return false;
        LinkedHashMap<K, V> shuffled = new LinkedHashMap<>();
        for(Map.Entry<K, V> entry: this.shuffle(this.entrySet())) {
            shuffled.put(entry.getKey(), entry.getValue());
        }
        return shuffled.equals(o);
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
}