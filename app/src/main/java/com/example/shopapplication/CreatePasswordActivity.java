package com.example.shopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CreatePasswordActivity extends AppCompatActivity {

    private EditText etPassword, etConfirmPassword;
    private Button btnCreatePassword;
    private ProgressBar progressBar;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);

        email = getIntent().getStringExtra("email");
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreatePassword = findViewById(R.id.btnCreatePassword);
        progressBar = findViewById(R.id.progressBar);

        btnCreatePassword.setOnClickListener(v -> {
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            createPassword(password);
        });
    }

    private void createPassword(String password) {
        setLoading(true);
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        ApiClient.post("/auth/create-password", body, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (response.contains("\"success\":true")) {
                        try {
                            org.json.JSONObject j = new org.json.JSONObject(response);
                            String token = j.getString("token");
                            new SessionManager(CreatePasswordActivity.this).saveSession(token, email);
                            Toast.makeText(CreatePasswordActivity.this,
                                    "Account created! Welcome!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CreatePasswordActivity.this, ShopHomeActivity.class));
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(CreatePasswordActivity.this,
                                    "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CreatePasswordActivity.this,
                                "Failed: " + response, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(CreatePasswordActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        btnCreatePassword.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
