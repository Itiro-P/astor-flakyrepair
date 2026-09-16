package com.array;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;



public class ArrayTest {
    @Test
    public void testArray() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
 
        List<String> keys = new ArrayList<>(List.of("a", "b", "c"));
        List<String> keys2 = new ArrayList<>(map.keySet());

        // Flaky: assume ordem de inserção, que HashMap não preserva
        // Ordem de inserção não deve importar aqui
        //assertEquals(keys, keys2);
        assertEquals(keys, keys2);
    }

    @Test
    public void testGroupingByCollector() {
        List<String> names = List.of("alice", "bob", "alex", "carol");

        Map<Character, List<String>> groupedByFirstLetter = names.stream()
                .collect(Collectors.groupingBy(name -> name.charAt(0)));

        assertEquals(2, groupedByFirstLetter.get('a').size());
        assertEquals(List.of("alice", "alex"), groupedByFirstLetter.get('a'));
        assertEquals(List.of("bob"), groupedByFirstLetter.get('b'));
    }

    @Test
    public void testJoiningCollector() {
        List<String> words = List.of("Flaky", "Seeding", "Mutation");

        String result = words.stream()
                .collect(Collectors.joining("-"));

        assertEquals("Flaky-Seeding-Mutation", result);
    }

    @Test
    public void testFlakyGroupingFromSet() {
        Set<String> tags = new HashSet<>();
        tags.add("flaky");
        tags.add("junit");
        tags.add("astor");

        // FLAKY: A fonte é um HashSet (sem ordem).
        // groupingBy agrupa os elementos em uma List mantendo a ordem de iteração da Stream.
        Map<Integer, List<String>> groupedByLength = tags.stream()
                .collect(Collectors.groupingBy(String::length));

        // Asserção assume uma ordem específica ("flaky" antes de "junit")
        assertEquals(List.of("astor", "flaky", "junit"), groupedByLength.get(5));
    }
}