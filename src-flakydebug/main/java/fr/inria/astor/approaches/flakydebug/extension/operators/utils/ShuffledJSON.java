package fr.inria.astor.approaches.flakydebug.extension.operators.utils;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tipo especial de JSONObject onde seus elementos internos são embaralhados
 * na serialização, simulando order-dependent flakiness.
 */
@SuppressWarnings("unchecked")
public class ShuffledJSON extends JSONObject {
	public ShuffledJSON(JSONObject obj) {
		super();
		if(obj != null) this.putAll(obj);
	}

	public ShuffledJSON(String jsonString) {
		super();
		if(jsonString != null) {
			JSONObject obj = (JSONObject) JSONValue.parse(jsonString);
			if(obj != null) this.putAll(obj);
		}
	}

    @Override
    public Set<Map.Entry<?, ?>> entrySet() {
		List<Map.Entry<?, ?>> entries = new ArrayList<>(super.entrySet());
        Collections.shuffle(entries);
        return new LinkedHashSet<>(entries);
    }

    @Override
    public String toJSONString() {
        StringBuffer sb = new StringBuffer();
        boolean first = true;

        sb.append('{');
        for (Map.Entry<?, ?> entry : entrySet()) { // usa o entrySet() embaralhado
            if (!first) sb.append(',');
            first = false;

            sb.append('\"');
            escape(String.valueOf(entry.getKey()), sb);
            sb.append('\"').append(':');
            sb.append(JSONValue.toJSONString(entry.getValue()));
        }
        sb.append('}');
        return sb.toString();
    }

    void escape(String s, StringBuffer sb) {
		for(int i=0;i<s.length();i++){
			char ch=s.charAt(i);
			switch(ch){
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
                //Reference: http://www.unicode.org/versions/Unicode5.1.0/
				if((ch>='\u0000' && ch<='\u001F') || (ch>='\u007F' && ch<='\u009F') || (ch>='\u2000' && ch<='\u20FF')){
					String ss=Integer.toHexString(ch);
					sb.append("\\u");
					for(int k=0;k<4-ss.length();k++){
						sb.append('0');
					}
					sb.append(ss.toUpperCase());
				}
				else{
					sb.append(ch);
				}
			}
		}
    }
    @Override
    public String toString() {
        return toJSONString();
    }
}