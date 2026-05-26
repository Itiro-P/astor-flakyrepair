package com.json_object;

import org.junit.Test;
import org.json.simple.JSONObject;

import static org.junit.Assert.assertEquals;

public class JsonObjectTest {

    @Test
    public void simpleJSONObject() {
        JSONObject obj1 = new JSONObject();
        obj1.put("name", "alice");
        obj1.put("age", 30);

        JSONObject obj2 = new JSONObject();
        obj2.put("age", 30);
        obj2.put("name", "alice");

        assertEquals(obj1.toJSONString(), obj2.toJSONString());
    }
}
