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
    public void testFlakyAsyncWithSleep() throws InterruptedException {
        Set<String> asyncCollectedKeys = new HashSet<>();

        // Processamento assíncrono simulado
        Thread worker = new Thread(() -> {
            asyncCollectedKeys.add("data1");
            asyncCollectedKeys.add("data2");
            asyncCollectedKeys.add("data3");
        });
        worker.start();

        // FLAKY 1 (Timing): Presume que 50ms são suficientes para a thread concluir.
        // Em ambientes lentos (como servidores de CI/CD), a asserção roda antes do fim da thread.
        Thread.sleep(50);

        List<String> result = new ArrayList<>(asyncCollectedKeys);

        // FLAKY 2 (Ordering): Converte um HashSet para ArrayList sem ordenar,
        // dependendo da ordem interna da tabela Hash.
        assertEquals(List.of("data1", "data2", "data3"), result);
    }
}