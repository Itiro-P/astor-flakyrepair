package com.hash_map;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class HashMapTest {
    @Test
    public void pickKeys() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        // Flaky: assume ordem de inserção, que HashMap não preserva
        // Ordem de inserção não deve importar aqui
        assertThat(map.keySet()).containsExactly("a", "b", "c");
    }
}