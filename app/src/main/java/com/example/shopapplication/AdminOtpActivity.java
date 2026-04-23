package com.example.shopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminOtpActivity extends AppCompatActivity {

    private EditText etOtp;
    private Button btnVerify;
    private ProgressBar progressBar;
    private String email;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_otp);

        email = getIntent().getStringExtra("email");
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerify);
        progressBar = findViewById(R.id.progressBar);

        TextView tvEmail = findViewById(R.id.tvEmail);
        if (tvEmail != null) {
            tvEmail.setText("OTP sent to: " + email);
        }

        btnVerify.setOnClickListener(v -> verifyOtp());
    }

    private void verifyOtp() {
        String otp = etOtp.getText().toString().trim();
        if (otp.isEmpty() || otp.length() != 6) {
            Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        String body = "{\"email\":\"" + email + "\",\"otp\":\"" + otp + "\"}";
        ApiClient.post("/auth/verify-otp", body, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (response.contains("\"success\":true")) {
                        if (isAdmin) {
                            new SessionManager(AdminOtpActivity.this).saveAdminSession("admin_token");
                            Toast.makeText(AdminOtpActivity.this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AdminOtpActivity.this, OwnerActivity.class));
                        } else {
                            Toast.makeText(AdminOtpActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AdminOtpActivity.this, ShopHomeActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(AdminOtpActivity.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(AdminOtpActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        btnVerify.setEnabled(!loading);
        progressBar.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}