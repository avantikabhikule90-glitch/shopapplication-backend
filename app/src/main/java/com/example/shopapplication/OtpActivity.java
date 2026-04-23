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

public class OtpActivity extends AppCompatActivity {

    private EditText etOtp;
    private Button btnVerifyOtp, btnResend;
    private ProgressBar progressBar;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        email = getIntent().getStringExtra("email");
        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnResend = findViewById(R.id.btnResend);
        progressBar = findViewById(R.id.progressBar);

        TextView tvEmail = findViewById(R.id.tvEmail);
        tvEmail.setText("OTP sent to: " + email);

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.length() != 6) {
                Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyOtp(otp);
        });

        btnResend.setOnClickListener(v -> resendOtp());
    }

    private void verifyOtp(String otp) {
        setLoading(true);
        String body = "{\"email\":\"" + email + "\",\"otp\":\"" + otp + "\"}";
        ApiClient.post("/auth/verify-otp", body, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (response.contains("\"success\":true")) {
                        Intent i = new Intent(OtpActivity.this, CreatePasswordActivity.class);
                        i.putExtra("email", email);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(OtpActivity.this, "Wrong OTP. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(OtpActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void resendOtp() {
        setLoading(true);
        String body = "{\"email\":\"" + email + "\"}";
        ApiClient.post("/auth/send-otp", body, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(OtpActivity.this, "New OTP sent!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(OtpActivity.this, "Failed to resend", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        btnVerifyOtp.setEnabled(!loading);
        btnResend.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
