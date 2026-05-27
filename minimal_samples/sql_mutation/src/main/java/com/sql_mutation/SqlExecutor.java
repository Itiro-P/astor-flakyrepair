package com.sql_mutation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqlExecutor {

    private static final String URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private Connection connection;

    public SqlExecutor() throws SQLException {
        this.connection = DriverManager.getConnection(URL);
        setupDB();
    }

    private void setupDB() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS products");
            stmt.execute("CREATE TABLE products (id INT PRIMARY KEY, name VARCHAR(255), price DOUBLE, active BOOLEAN)");
        }
    }

    public boolean insertProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (id, name, price, active) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, product.id);
            pstmt.setString(2, product.name);
            pstmt.setDouble(3, product.price);
            pstmt.setBoolean(4, product.active);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public List<Product> searchProducts(String sqlQuery) throws SQLException {
        List<Product> results = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sqlQuery)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                boolean active = rs.getBoolean("active");
                results.add(new Product(id, name, price, active));
            }
        }
        return results;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}