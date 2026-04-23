package com.example.shopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminRegisterActivity.class));
        });

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter email or username", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        String body = "{\"username\":\"" + email + "\",\"password\":\"" + password + "\"}";
        ApiClient.post("/admin/login", body, new ApiClient.Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    if (response.contains("\"success\":true")) {
                        SessionManager session = new SessionManager(AdminLoginActivity.this);
                        session.setAdminLoggedIn(true);
                        startActivity(new Intent(AdminLoginActivity.this, OwnerActivity.class));
                        finish();
                    } else {
                        Toast.makeText(AdminLoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(AdminLoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}