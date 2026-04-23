package com.example.shopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etPassword;
    private Button btnLogin, btnForgotPassword;
    private ProgressBar progressBar;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = getIntent().getStringExtra("email");
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        progressBar = findViewById(R.id.progressBar);

        TextView tvEmail = findViewById(R.id.tvEmail);
        tvEmail.setText(email);

        btnLogin.setOnClickListener(v -> {
            String password = etPassword.getText().toString().trim();
            if (password.isEmpty()) {
                Toast.makeText(this, "Enter your password", Toast.LENGTH_SHORT).show();
                return;
            }
            login(password);
        });

        btnForgotPassword.setOnClickListener(v -> {
            // Send OTP to reset password
            setLoading(true);
            String body = "{\"email\":\"" + email + "\"}";
            ApiClient.post("/auth/send-otp", body, new ApiClient.Callback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        if (response.contains("\"success\":true")) {
                            Toast.makeText(LoginActivity.this,
                                    "OTP sent! Use it to reset password.", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(LoginActivity.this, OtpActivity.class);
                            i.putExtra("email", email);
                            startActivity(i);
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private void login(String password) {
        setLoading(true);
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        ApiClient.post("/auth/login", body, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (response.contains("\"success\":true")) {
                        try {
                            org.json.JSONObject j = new org.json.JSONObject(response);
                            String token = j.getString("token");
                            new SessionManager(LoginActivity.this).saveSession(token, email);
                            startActivity(new Intent(LoginActivity.this, ShopHomeActivity.class));
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this,
                                    "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Wrong password. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this,
                            "Connection error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
