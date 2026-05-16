package com.hash_map;

import java.util.HashMap;
import java.util.Map;

public class MyHashMap {

    private final Map<String, Integer> itens = new HashMap<>();

    public void adicionar(String produto, int quantidade) {
        itens.put(produto, quantidade);
    }

    public Map<String, Integer> getItens() {
        return itens;
    }
}