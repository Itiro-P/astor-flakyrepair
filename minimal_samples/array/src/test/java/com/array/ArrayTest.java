package com.array;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;



public class ArrayTest {
    @Test
    public void pickKeys() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
 
        List<String> keys = new ArrayList<>(map.keySet());
        List<String> keys2 = new ArrayList<>(List.of("a", "b", "c"));

        // Flaky: assume ordem de inserção, que HashMap não preserva
        // Ordem de inserção não deve importar aqui
        //assertEquals(keys, keys2);
        assertThat(keys).containsExactlyElementsOf(keys2);
    }
}