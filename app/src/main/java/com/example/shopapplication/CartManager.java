package com.example.shopapplication;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> items = new ArrayList<>();

    public static CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    public void addItem(Product p) {
        for (CartItem item : items) {
            if (item.getName().equals(p.getName())) {
                item.incrementQty();
                return;
            }
        }
        items.add(new CartItem(p.getName(), p.getPriceInt(), 1));
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
    }

    public int getCount() {
        int count = 0;
        for (CartItem i : items) count += i.getQty();
        return count;
    }

    public int getTotal() {
        int total = 0;
        for (CartItem i : items) total += i.getPrice() * i.getQty();
        return total;
    }
}
