package com.example.shopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminRegisterOtpActivity extends AppCompatActivity {

    private EditText etOtp, etPassword;
    private Button btnVerify;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register_otp);

        email = getIntent().getStringExtra("email");

        etOtp = findViewById(R.id.etOtp);
        etPassword = findViewById(R.id.etPassword);
        btnVerify = findViewById(R.id.btnVerify);

        TextView tvEmail = findViewById(R.id.tvEmail);
        tvEmail.setText("OTP sent to: " + email);

        btnVerify.setOnClickListener(v -> verifyAndRegister());
    }

    private void verifyAndRegister() {
        String otp = etOtp.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (otp.isEmpty() || otp.length() != 6) {
            Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        btnVerify.setEnabled(false);

        // First verify OTP
        String verifyBody = "{\"email\":\"" + email + "\",\"otp\":\"" + otp + "\"}";
        ApiClient.post("/auth/verify-otp", verifyBody, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                if (response.contains("\"success\":true")) {
                    // OTP verified, now register admin
                    String username = email.substring(0, email.indexOf("@"));
                    String registerBody = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
                    ApiClient.post("/admin/register", registerBody, new ApiClient.Callback() {
                        @Override
                        public void onSuccess(String regResponse) {
                            runOnUiThread(() -> {
                                btnVerify.setEnabled(true);
                                if (regResponse.contains("\"success\":true")) {
                                    Toast.makeText(AdminRegisterOtpActivity.this, "Admin registered! Please login.", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(AdminRegisterOtpActivity.this, AdminLoginActivity.class));
                                    finish();
                                } else if (regResponse.contains("Username already exists")) {
                                    Toast.makeText(AdminRegisterOtpActivity.this, "Admin already exists. Please login.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(AdminRegisterOtpActivity.this, AdminLoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(AdminRegisterOtpActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                btnVerify.setEnabled(true);
                                Toast.makeText(AdminRegisterOtpActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        btnVerify.setEnabled(true);
                        Toast.makeText(AdminRegisterOtpActivity.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnVerify.setEnabled(true);
                    Toast.makeText(AdminRegisterOtpActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}