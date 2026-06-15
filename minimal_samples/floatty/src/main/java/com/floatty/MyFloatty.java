package com.floatty;

import java.util.ArrayList;

public class MyFloatty {

    private final ArrayList<Float> itens = new ArrayList<>();

    public void adicionar(Float produto) {
        itens.add(produto);
    }

    public ArrayList<Float> getItens() {
        return itens;
    }
}