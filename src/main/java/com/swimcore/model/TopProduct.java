package com.swimcore.model;

public class TopProduct {
    private String name;
    private int quantity;

    public TopProduct(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public int getQuantity() { return quantity; }
}