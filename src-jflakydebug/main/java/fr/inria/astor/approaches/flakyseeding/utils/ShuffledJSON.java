package fr.inria.astor.approaches.flakyseeding.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShuffledJSON extends JSONObject implements ShuffledColletion<String> {

    private List<String> cachedOrder;

    public ShuffledJSON(JSONObject obj) {
        super();
        if (obj != null) {
            for (String key : obj.keySet()) {
                this.put(key, obj.get(key));
            }
        }
    }

    public ShuffledJSON(String jsonString) {
        super(jsonString != null ? jsonString : "{}");
    }

    @Override public List<String> getCachedOrder() { return cachedOrder; }
    @Override public void setCachedOrder(List<String> order) { this.cachedOrder = order; }

    /**
     * Única fonte de verdade da ordem embaralhada. Qualquer outro método que
     * exponha a ordem das chaves (keys(), names(), toMap(), toString(),
     * write()) deve passar por aqui, direta ou indiretamente.
     */
    @Override
    public Set<String> keySet() {
        List<String> order = getShuffledOrder(super::keySet);
        return new LinkedHashSet<>(order);
    }

    @Override
    public JSONObject put(String key, Object value) {
        JSONObject result = super.put(key, value);
        invalidateOrder();
        return result;
    }

    @Override
    public JSONObject remove(String key) {
        invalidateOrder();
        return this;
    }

    @Override
    public Iterator<String> keys() {
        return this.keySet().iterator();
    }

    @Override
    public JSONArray names() {
        Set<String> keys = this.keySet();
        if (keys.isEmpty()) {
            return null;
        }
        return new JSONArray(keys);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> results = new LinkedHashMap<>();
        for (String key : this.keySet()) {
            Object value = this.get(key);
            if (value instanceof JSONObject) {
                value = ((JSONObject) value).toMap();
            } else if (value instanceof JSONArray) {
                value = ((JSONArray) value).toList();
            }
            results.put(key, value);
        }
        return results;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (String key : this.keySet()) { // mesma ordem cacheada
            if (!first) sb.append(',');
            first = false;
            sb.append(JSONObject.quote(key));
            sb.append(':');
            sb.append(valueToJson(this.get(key)));
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String toString(int indentFactor) {
        return toString();
    }

    @Override
    public Writer write(Writer writer) {
        return this.write(writer, 0, 0);
    }

    @Override
    public Writer write(Writer writer, int indentFactor, int indent) {
        try {
            writer.write(this.toString());
            return writer;
        } catch (IOException e) {
            throw new org.json.JSONException(e);
        }
    }

    private String valueToJson(Object value) {
        if (value == null || value == JSONObject.NULL) {
            return "null";
        }
        if (value instanceof JSONObject || value instanceof JSONArray) {
            return value.toString();
        }
        if (value instanceof String) {
            return JSONObject.quote((String) value);
        }
        return String.valueOf(value);
    }
}