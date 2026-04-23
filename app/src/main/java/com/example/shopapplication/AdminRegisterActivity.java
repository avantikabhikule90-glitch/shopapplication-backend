package com.example.shopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminRegisterActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        etEmail = findViewById(R.id.etEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
        });

        btnSendOtp.setOnClickListener(v -> sendOtp());
    }

    private void sendOtp() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty() || !email.contains("@")) {
            Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSendOtp.setEnabled(false);
        String body = "{\"email\":\"" + email + "\"}";
        ApiClient.post("/auth/send-otp", body, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    btnSendOtp.setEnabled(true);
                    if (response.contains("\"success\":true")) {
                        Toast.makeText(AdminRegisterActivity.this, "OTP sent!", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(AdminRegisterActivity.this, AdminRegisterOtpActivity.class);
                        i.putExtra("email", email);
                        startActivity(i);
                    } else {
                        Toast.makeText(AdminRegisterActivity.this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnSendOtp.setEnabled(true);
                    Toast.makeText(AdminRegisterActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}