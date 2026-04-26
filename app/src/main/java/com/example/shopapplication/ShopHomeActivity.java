package com.example.shopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ShopHomeActivity extends AppCompatActivity {

    private TextView tvCartCount, tvPoints, tvOrderCount;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_home);
        session = new SessionManager(this);
        tvCartCount = findViewById(R.id.tvCartCount);
        tvPoints = findViewById(R.id.tvPoints);
        tvOrderCount = findViewById(R.id.tvOrderCount);

        findViewById(R.id.btnCart).setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));

        findViewById(R.id.layoutOrders).setOnClickListener(v ->
                startActivity(new Intent(this, OrdersActivity.class)));

        findViewById(R.id.btnOrders).setOnClickListener(v ->
                startActivity(new Intent(this, OrdersActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.clearSession();
            CartManager.getInstance().clear();
            startActivity(new Intent(this, EmailActivity.class));
            finish();
        });

        setupProducts();
        loadPoints();
        updateCartCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartCount();
        loadPoints();
    }

    private void loadPoints() {
        ApiClient.get("/user/points/" + session.getEmail(), new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        tvPoints.setText(String.valueOf(new org.json.JSONObject(response).getInt("points")));
                    } catch (Exception e) {
                        tvPoints.setText("0");
                    }
                });
            }
            @Override
            public void onError(String error) {}
        });
    }

    private void updateCartCount() {
        int count = CartManager.getInstance().getCount();
        tvCartCount.setText(String.valueOf(count));
    }

    private void setupProducts() {
        List<Product> list = new ArrayList<>();

        list.add(new Product("Paracetamol 500mg", "Medicine", "Rs 25", "💊", 25));
        list.add(new Product("Vitamin C Tablets", "Medicine", "Rs 120", "🍊", 120));
        list.add(new Product("Bandage Roll", "First Aid", "Rs 45", "🩹", 45));
        list.add(new Product("Hand Sanitizer", "Hygiene", "Rs 80", "🧴", 80));
        list.add(new Product("Face Mask x10", "Medicine", "Rs 60", "😷", 60));
        list.add(new Product("Thermometer", "Devices", "Rs 350", "🌡️", 350));
        list.add(new Product("Cough Syrup", "Medicine", "Rs 95", "🍶", 95));
        list.add(new Product("Eye Drops", "Medicine", "Rs 75", "👁️", 75));
        list.add(new Product("BP Monitor", "Devices", "Rs 1500", "❤️", 1500));
        list.add(new Product("Glucometer", "Devices", "Rs 900", "🩸", 900));
        list.add(new Product("Antiseptic Cream", "First Aid", "Rs 55", "🏥", 55));
        list.add(new Product("Multivitamin", "Supplements", "Rs 200", "💪", 200));

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new ProductAdapter(list, p -> {
            CartManager.getInstance().addItem(p);
            updateCartCount();
            Toast.makeText(this, p.getName() + " added!", Toast.LENGTH_SHORT).show();
        }));
    }
}