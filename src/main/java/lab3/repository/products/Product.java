package lab3.repository.products;

import lab3.repository.Column;
import lab3.repository.Entity;
import lab3.repository.impls.ColumnImpl;

import java.util.List;
import java.util.Objects;

public class Product implements Entity {
    private int id;
    private String name;
    private long price;

    public Product(int id, String name, long price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Product() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public static List<Column<Product, ?>> allColumns() {
        return List.of(ID, NAME, PRICE);
    }

    public static Column<Product, Integer> ID = new ColumnImpl<>(
            "ID",
            Product::getId,
            Product::setId,
            Object::toString,
            Integer::parseInt
    );

    public static Column<Product, String> NAME = new ColumnImpl<>(
            "NAME",
            Product::getName,
            Product::setName,
            s -> "\"" + s + "\"",
            s -> s
    );

    public static Column<Product, Long> PRICE = new ColumnImpl<>(
            "PRICE",
            Product::getPrice,
            Product::setPrice,
            Objects::toString,
            Long::parseLong
    );
}
