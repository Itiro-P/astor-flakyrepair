package com.sql_mutation;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.assertEquals;

public class SqlExecutorTest {

    private SqlExecutor executor;

    @Before
    public void setUp() throws SQLException {
        executor = new SqlExecutor();
    }

    @After
    public void tearDown() throws SQLException {
        executor.close();
    }

    @Test
    public void sqlTest() throws SQLException {
        List<Product> products = new ArrayList<>(List.of(
            new Product(1, "Teclado Mecânico", 350.00, true),
            new Product(3, "Monitor Antigo", 200.00, false),
            new Product(2, "Mouse Wireless", 150.00, true)
        ));

        for(Product product : products) {
            executor.insertProduct(product);
        }

        String sql = "SELECT * FROM products ORDER BY price DESC";
        
        List<Product> productsSearch = executor.searchProducts(sql);
        
        assertEquals(products, productsSearch);
    }
}