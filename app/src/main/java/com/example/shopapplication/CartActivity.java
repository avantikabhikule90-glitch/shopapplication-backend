package com.example.shopapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private TextView tvTotal, tvPoints, tvDiscount, tvItemCount;
    private EditText etPoints;
    private SessionManager session;
    private int userPoints = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        session = new SessionManager(this);
        tvTotal = findViewById(R.id.tvTotal);
        tvPoints = findViewById(R.id.tvPoints);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvItemCount = findViewById(R.id.tvItemCount);
        etPoints = findViewById(R.id.etPoints);

        findViewById(R.id.btnApplyPoints).setOnClickListener(v -> updateDiscount());
        findViewById(R.id.btnPlaceOrder).setOnClickListener(v -> placeOrder());

        List<CartItem> items = CartManager.getInstance().getItems();
        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new CartAdapter(items, this::updateTotal));

        updateTotal();
        loadPoints();
    }

    private void updateTotal() {
        int total = CartManager.getInstance().getTotal();
        int count = CartManager.getInstance().getCount();
        tvItemCount.setText(count + " items");
        tvTotal.setText("Rs " + total);

        if (count == 0) {
            findViewById(R.id.recyclerView).setVisibility(android.view.View.GONE);
            findViewById(R.id.layoutEmpty).setVisibility(android.view.View.VISIBLE);
        } else {
            findViewById(R.id.recyclerView).setVisibility(android.view.View.VISIBLE);
            findViewById(R.id.layoutEmpty).setVisibility(android.view.View.GONE);
            updateDiscount();
        }
    }

    private void updateDiscount() {
        try {
            int pts = Integer.parseInt(etPoints.getText().toString());
            if (pts > userPoints) {
                tvDiscount.setText("Not enough points (max " + userPoints + "%)");
            } else if (pts > 100) {
                tvDiscount.setText("Max 100%");
            } else {
                int subtotal = CartManager.getInstance().getTotal();
                int discount = (int) (subtotal * pts / 100.0);
                int finalTotal = subtotal - discount;
                tvDiscount.setText(pts + "% off = -Rs " + discount);
                tvTotal.setText("Rs " + finalTotal);
            }
        } catch (Exception e) {
            tvDiscount.setText("");
            tvTotal.setText("Rs " + CartManager.getInstance().getTotal());
        }
    }

    private void loadPoints() {
        ApiClient.get("/user/points/" + session.getEmail(), new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        userPoints = new JSONObject(response).getInt("points");
                        tvPoints.setText("Points: " + userPoints);
                    } catch (Exception e) {
                        tvPoints.setText("Points: 0");
                    }
                });
            }
            @Override
            public void onError(String error) {}
        });
    }

    private void placeOrder() {
        List<CartItem> items = CartManager.getInstance().getItems();
        if (items.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        int pointsToUse = 0;
        try { pointsToUse = Integer.parseInt(etPoints.getText().toString()); } catch (Exception e) {}
        if (pointsToUse > userPoints) {
            Toast.makeText(this, "Not enough points", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pointsToUse > 100) {
            Toast.makeText(this, "Max 100 points", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int total = CartManager.getInstance().getTotal();
        try {
            JSONArray itemsArr = new JSONArray();
            for (CartItem item : items) {
                JSONObject obj = new JSONObject();
                obj.put("name", item.getName());
                obj.put("price", item.getPrice());
                obj.put("qty", item.getQty());
                itemsArr.put(obj);
            }
            JSONObject body = new JSONObject();
            body.put("email", session.getEmail());
            body.put("items", itemsArr);
            body.put("total", total);
            body.put("pointsUsed", pointsToUse);

            ApiClient.post("/orders/place", body.toString(), new ApiClient.Callback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject j = new JSONObject(response);
                            if (j.getBoolean("success")) {
                                CartManager.getInstance().clear();
                                int earned = j.getInt("pointsEarned");
                                int totalPts = j.getInt("totalPoints");
                                Toast.makeText(CartActivity.this, "Order placed! +" + earned + " points", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(CartActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(CartActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(CartActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}