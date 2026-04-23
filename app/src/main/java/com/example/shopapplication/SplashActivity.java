package com.example.shopapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            SessionManager session = new SessionManager(this);
            if (session.isLoggedIn()) {
                if (session.isAdmin()) {
                    startActivity(new Intent(this, OwnerActivity.class));
                } else {
                    startActivity(new Intent(this, ShopHomeActivity.class));
                }
            } else {
                startActivity(new Intent(this, EmailActivity.class));
            }
            finish();
        }, 2000);
    }
}
