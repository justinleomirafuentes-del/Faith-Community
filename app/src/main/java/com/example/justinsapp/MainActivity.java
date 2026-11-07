package com.example.justinsapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button superButtonEvents;
    private Button superButtonMinistries;
    private Button superButtonGive;
    private Button superButtonConnect;
    private TextView tvUserStatus;
    private Button btnAuthAction;
    private Button btnRegisterAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind buttons
        superButtonEvents = findViewById(R.id.superButtonEvents);
        superButtonMinistries = findViewById(R.id.superButtonMinistries);
        superButtonGive = findViewById(R.id.superButtonGive);
        superButtonConnect = findViewById(R.id.superButtonConnect);
        tvUserStatus = findViewById(R.id.tvUserStatus);
        btnAuthAction = findViewById(R.id.btnAuthAction);
        btnRegisterAction = findViewById(R.id.btnRegisterAction);

        // 1. Events Button Function
        superButtonEvents.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EventsActivity.class);
            startActivity(intent);
        });

        // 2. Ministries Button Function
        superButtonMinistries.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MinistriesActivity.class);
            startActivity(intent);
        });

        // 3. Give Button Function
        superButtonGive.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GiveActivity.class);
            startActivity(intent);
        });

        // 4. Connect Button Function
        superButtonConnect.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserSession();
    }

    private void checkUserSession() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences pref = getSharedPreferences(LoginActivity.PREF_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = pref.getBoolean(LoginActivity.KEY_IS_LOGGED_IN, false) || user != null;

        if (isLoggedIn && user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = pref.getString(LoginActivity.KEY_USER_NAME, "User");
            }
            tvUserStatus.setText("Welcome, " + name + "!\n(" + user.getEmail() + ")");
            btnAuthAction.setText("Log Out");
            btnAuthAction.setBackgroundResource(R.drawable.bg_btn_red);
            btnAuthAction.setOnClickListener(v -> handleLogout());
            btnRegisterAction.setVisibility(View.GONE);
        } else if (isLoggedIn) {
            String email = pref.getString(LoginActivity.KEY_USER_EMAIL, "");
            String name = pref.getString(LoginActivity.KEY_USER_NAME, "User");
            tvUserStatus.setText("Welcome, " + name + "!\n(" + email + ")");
            btnAuthAction.setText("Log Out");
            btnAuthAction.setBackgroundResource(R.drawable.bg_btn_red);
            btnAuthAction.setOnClickListener(v -> handleLogout());
            btnRegisterAction.setVisibility(View.GONE);
        } else {
            tvUserStatus.setText("Not signed in");
            btnAuthAction.setText("Sign In");
            btnAuthAction.setBackgroundResource(R.drawable.bg_btn_blue);
            btnAuthAction.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            });
            btnRegisterAction.setVisibility(View.VISIBLE);
            btnRegisterAction.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }
    }

    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences pref = getSharedPreferences(LoginActivity.PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().clear().apply();

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        checkUserSession();
    }
}