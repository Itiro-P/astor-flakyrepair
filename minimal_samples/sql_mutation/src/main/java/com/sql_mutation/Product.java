package com.sql_mutation;

import java.util.Objects;

public class Product {
    public int id;
    public String name;
    public double price;
    public boolean active;

    public Product(int id, String name, double price, boolean active) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id && 
               Double.compare(product.price, price) == 0 && 
               active == product.active && 
               Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, active);
    }

    @Override
    public String toString() {
        return "Product{" + "id=" + id + ", name='" + name + '\'' + ", price=" + price + ", active=" + active + '}';
    }
}
