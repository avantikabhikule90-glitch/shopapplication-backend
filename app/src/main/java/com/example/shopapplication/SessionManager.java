package com.example.shopapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF = "ShopAppSession";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String token, String email) {
        editor.putString("token", token);
        editor.putString("email", email);
        editor.putBoolean("isAdmin", false);
        editor.apply();
    }

    public void saveAdminSession(String token) {
        editor.putString("token", token);
        editor.putString("email", "admin");
        editor.putBoolean("isAdmin", true);
        editor.apply();
    }

    public boolean isAdmin() {
        return prefs.getBoolean("isAdmin", false);
    }

    public boolean isLoggedIn() {
        return prefs.getString("token", null) != null;
    }

    public String getToken() {
        return prefs.getString("token", "");
    }

    public String getEmail() {
        return prefs.getString("email", "");
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public void setAdminLoggedIn(boolean isAdmin) {
        editor.putString("token", "admin_token");
        editor.putString("email", "admin");
        editor.putBoolean("isAdmin", isAdmin);
        editor.apply();
    }
}
