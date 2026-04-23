package com.example.shopapplication;

public class Product {
    private String name, category, price, emoji;
    private int priceInt;

    public Product(String name, String category, String price, String emoji, int priceInt) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.emoji = emoji;
        this.priceInt = priceInt;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getPrice() { return price; }
    public String getEmoji() { return emoji; }
    public int getPriceInt() { return priceInt; }
}
