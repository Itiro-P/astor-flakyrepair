package fr.inria.astor.approaches.flakydebug.extension.operators.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tipo especial de JSONObject onde a ordem das chaves é embaralhada na
 * serialização, simulando order-dependent flakiness.
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

    @Override
    public String toString() {
        List<String> keys = new ArrayList<>(this.keySet());
        Collections.shuffle(keys);

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (String key : keys) {
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