package com.example.shopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EmailActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendOtp, btnLogin, btnAdminLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        etEmail = findViewById(R.id.etEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnLogin = findViewById(R.id.btnLogin);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        btnSendOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim().toLowerCase();
            if (email.isEmpty() || !email.contains("@")) {
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            sendOtp(email);
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim().toLowerCase();
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(this, LoginActivity.class);
            i.putExtra("email", email);
            startActivity(i);
        });

        btnAdminLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminLoginActivity.class));
        });
    }

    private void sendOtp(String email) {
        btnSendOtp.setEnabled(false);
        String body = "{\"email\":\"" + email + "\"}";
        ApiClient.post("/auth/send-otp", body, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    btnSendOtp.setEnabled(true);
                    if (response.contains("\"success\":true")) {
                        Toast.makeText(EmailActivity.this, "OTP sent!", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(EmailActivity.this, OtpActivity.class);
                        i.putExtra("email", email);
                        startActivity(i);
                    } else {
                        Toast.makeText(EmailActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnSendOtp.setEnabled(true);
                    Toast.makeText(EmailActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}