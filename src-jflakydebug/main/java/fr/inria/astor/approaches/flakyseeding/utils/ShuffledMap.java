package fr.inria.astor.approaches.flakyseeding.utils;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tipo especial de Map onde a ordem de iteração (entrySet/keySet/values/
 * toString) é embaralhada, simulando order-dependent flakiness.
 * get()/put()/containsKey() continuam O(1), delegando direto a inner_map.
 */
public class ShuffledMap<K, V> extends AbstractMap<K, V> implements ShuffledColletion<K> {

    private Map<K, V> inner_map = new HashMap<>();
    private List<K> cachedOrder;

    public ShuffledMap() {}
    public ShuffledMap(Map<K, V> source) { this.inner_map = source; }

    @Override public List<K> getCachedOrder() { return cachedOrder; }
    @Override public void setCachedOrder(List<K> order) { this.cachedOrder = order; }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        List<K> order = getShuffledOrder(inner_map::keySet);

        return new AbstractSet<Map.Entry<K, V>>() {
            @Override public int size() { return inner_map.size(); }

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                Iterator<K> keyIt = order.iterator();
                return new Iterator<Map.Entry<K, V>>() {
                    K lastKey;

                    @Override public boolean hasNext() { return keyIt.hasNext(); }

                    @Override
                    public Map.Entry<K, V> next() {
                        lastKey = keyIt.next();
                        return new LiveEntry(lastKey); // entry ligada ao mapa real
                    }

                    @Override
                    public void remove() {
                        keyIt.remove();        // tira da ordem cacheada
                        inner_map.remove(lastKey); // tira do mapa real
                        invalidateOrder();
                    }
                };
            }
        };
    }

    /** Entry que lê/escreve direto em inner_map — setValue() afeta o mapa real. */
    private class LiveEntry extends AbstractMap.SimpleEntry<K, V> {
        LiveEntry(K key) { super(key, inner_map.get(key)); }

        @Override
        public V setValue(V value) {
            inner_map.put(getKey(), value);
            return super.setValue(value);
        }
    }

    @Override
    public V put(K key, V value) {
        V old = inner_map.put(key, value);
        invalidateOrder();
        return old;
    }

    @Override
    public V remove(Object key) {
        boolean had = inner_map.containsKey(key);
        V old = inner_map.remove(key);
        if (had) invalidateOrder();
        return old;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        inner_map.putAll(m);
        invalidateOrder();
    }

    @Override
    public void clear() {
        inner_map.clear();
        invalidateOrder();
    }

    @Override public int size() { return inner_map.size(); }
    @Override public boolean containsKey(Object key) { return inner_map.containsKey(key); }
    @Override public boolean containsValue(Object value) { return inner_map.containsValue(value); }
    @Override public V get(Object key) { return inner_map.get(key); }
}