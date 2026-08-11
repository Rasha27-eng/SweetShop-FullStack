package com.example.sweetshop;

public class SweetsItem {
    private String name;
    private double price;
    private int quantity = 1; // default

    public SweetsItem() {}

    public SweetsItem(String name, double price) {
        this.name = name;
        this.price = price;
        this.quantity = 1;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
