package com.example.shopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OwnerLoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_login);

        etEmail = findViewById(R.id.etEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);

        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminRegisterActivity.class));
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
                        Toast.makeText(OwnerLoginActivity.this, "OTP sent to email!", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(OwnerLoginActivity.this, AdminOtpActivity.class);
                        i.putExtra("email", email);
                        i.putExtra("isAdmin", true);
                        startActivity(i);
                    } else {
                        Toast.makeText(OwnerLoginActivity.this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnSendOtp.setEnabled(true);
                    Toast.makeText(OwnerLoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}