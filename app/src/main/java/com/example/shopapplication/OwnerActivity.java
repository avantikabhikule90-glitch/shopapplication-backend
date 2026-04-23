package com.example.shopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class OwnerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<JSONObject> orders = new ArrayList<>();
    private SessionManager session;
    private TextView tvTotalOrders, tvPendingOrders, tvCompletedOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner);

        session = new SessionManager(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        tvCompletedOrders = findViewById(R.id.tvCompletedOrders);

        findViewById(R.id.btnRefresh).setOnClickListener(v -> loadOrders());
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
        findViewById(R.id.btnReset).setOnClickListener(v -> resetAllData());

        loadOrders();
    }

    private void logout() {
        session.clearSession();
        startActivity(new Intent(OwnerActivity.this, EmailActivity.class));
        finish();
    }

    private void updateStats(JSONArray arr) {
        int total = arr.length();
        int pending = 0;
        int completed = 0;
        for (int i = 0; i < arr.length(); i++) {
            try {
                String status = arr.getJSONObject(i).getString("status");
                if ("Pending".equals(status)) pending++;
                else if ("Completed".equals(status)) completed++;
            } catch (Exception e) {}
        }
        tvTotalOrders.setText(String.valueOf(total));
        tvPendingOrders.setText(String.valueOf(pending));
        tvCompletedOrders.setText(String.valueOf(completed));
    }

    private void resetAllData() {
        ApiClient.post("/admin/reset-users", "{}", new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    Toast.makeText(OwnerActivity.this, "All data reset!", Toast.LENGTH_SHORT).show();
                    loadOrders();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(OwnerActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadOrders() {
        ApiClient.get("/orders/all", new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject j = new JSONObject(response);
                        JSONArray arr = j.getJSONArray("orders");
                        orders.clear();
                        for (int i = 0; i < arr.length(); i++) {
                            orders.add(arr.getJSONObject(i));
                        }
                        updateStats(arr);
                        recyclerView.setAdapter(new OrderAdapter(orders,
                                (orderId, status) -> updateStatus(orderId, status), true));

                        if (orders.isEmpty()) {
                            Toast.makeText(OwnerActivity.this,
                                    "No orders yet", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(OwnerActivity.this,
                                "Error loading orders", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(OwnerActivity.this,
                                "Connection error", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateStatus(String orderId, String status) {
        try {
            JSONObject body = new JSONObject();
            body.put("orderId", orderId);
            body.put("status", status);
            ApiClient.post("/orders/update-status", body.toString(), new ApiClient.Callback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        Toast.makeText(OwnerActivity.this,
                                "Order marked as " + status, Toast.LENGTH_SHORT).show();
                        loadOrders();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() ->
                            Toast.makeText(OwnerActivity.this,
                                    "Error: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
