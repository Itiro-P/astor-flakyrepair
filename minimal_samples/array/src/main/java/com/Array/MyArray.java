package com.array;

import java.util.ArrayList;

public class MyArray {

    private final ArrayList<String> itens = new ArrayList<>();

    public void adicionar(String produto) {
        itens.add(produto);
    }

    public ArrayList<String> getItens() {
        return itens;
    }
}