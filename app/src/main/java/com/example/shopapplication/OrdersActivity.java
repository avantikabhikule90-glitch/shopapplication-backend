package com.example.shopapplication;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        session = new SessionManager(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadOrders();
    }

    private void loadOrders() {
        ApiClient.get("/orders/user/" + session.getEmail(), new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject j = new JSONObject(response);
                        JSONArray arr = j.getJSONArray("orders");
                        List<JSONObject> orders = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            orders.add(arr.getJSONObject(i));
                        }
                        // isOwner = false — no status change buttons for user
                        recyclerView.setAdapter(new OrderAdapter(orders, null, false));
                    } catch (Exception e) {
                        Toast.makeText(OrdersActivity.this,
                                "Error loading orders", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(OrdersActivity.this,
                                "Connection error", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
