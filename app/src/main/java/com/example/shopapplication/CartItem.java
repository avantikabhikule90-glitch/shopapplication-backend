package com.example.shopapplication;

public class CartItem {
    private String name;
    private int price, qty;

    public CartItem(String name, int price, int qty) {
        this.name = name;
        this.price = price;
        this.qty = qty;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getQty() { return qty; }
    public void incrementQty() { qty++; }
    public void decrementQty() { if (qty > 0) qty--; }
}
