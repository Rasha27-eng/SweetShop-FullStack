package com.example.sweetshop;

import java.util.List;

public class Order {
    public List<SweetsAdapter.CartItem> items;
    public long timestamp;

    public Order() {
        // Required for Firebase
    }

    public Order(List<SweetsAdapter.CartItem> items, long timestamp) {
        this.items = items;
        this.timestamp = timestamp;
    }
}
