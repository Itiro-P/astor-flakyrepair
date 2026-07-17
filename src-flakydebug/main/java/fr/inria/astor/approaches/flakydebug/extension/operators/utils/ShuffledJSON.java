package fr.inria.astor.approaches.flakydebug.extension.operators.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tipo especial de JSONObject onde a ordem das chaves é embaralhada em toda
 * forma de leitura/serialização, simulando order-dependent flakiness.
 */
public class ShuffledJSON extends JSONObject {

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

    /**
     * Única fonte de verdade da ordem embaralhada. Qualquer outro método que
     * exponha a ordem das chaves (keys(), names(), toMap(), toString(),
     * write()) deve passar por aqui, direta ou indiretamente.
     */
    @Override
    public Set<String> keySet() {
        List<String> original = new ArrayList<>(super.keySet());
        List<String> shuffledKeys = original;
        Collections.shuffle(shuffledKeys);
        if (shuffledKeys.equals(original)) {
            // Caso raro (1/n! de chance): shuffle coincidiu com a ordem
            // original. Reverse garante uma ordem diferente, já que todas as
            // chaves são distintas.
            Collections.reverse(shuffledKeys);
        }
        return new LinkedHashSet<>(shuffledKeys);
    }

    @Override
    public Iterator<String> keys() {
        // org.json normalmente já delega keys() -> keySet().iterator(),
        // mas sobrescrevemos explicitamente para não depender de detalhe
        // de implementação de uma versão específica da lib.
        return this.keySet().iterator();
    }

    @Override
    public JSONArray names() {
        // A implementação original lê o campo privado "map" diretamente,
        // ignorando qualquer keySet() sobrescrito — por isso precisa de
        // override explícito aqui também.
        Set<String> keys = this.keySet();
        if (keys.isEmpty()) {
            return null;
        }
        return new JSONArray(keys);
    }

    @Override
    public Map<String, Object> toMap() {
        // Mesma razão do names(): a implementação original itera o mapa
        // privado interno diretamente.
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
        for (String key : this.keySet()) { // já vem embaralhado
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
        // Mantém o comportamento embaralhado mesmo se chamado com indentação
        return toString();
    }

    @Override
    public Writer write(Writer writer) {
        return this.write(writer, 0, 0);
    }

    @Override
    public Writer write(Writer writer, int indentFactor, int indent) {
        // Ponto de serialização "raiz" usado quando este objeto está
        // aninhado dentro de outro JSONObject/JSONArray que não é
        // ShuffledJSON — sem esse override, o pai serializaria este objeto
        // usando a ordem original (não embaralhada) do mapa interno.
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
        // Number, Boolean, etc.
        return String.valueOf(value);
    }
}