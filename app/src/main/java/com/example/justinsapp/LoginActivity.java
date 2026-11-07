package com.example.justinsapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class LoginActivity extends AppCompatActivity {

    public static final String PREF_NAME = "user_session";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_NAME = "user_name";

    private TextView tvAuthTitle;
    private TextView tvAuthSubtitle;
    private TextView tvToggleMode;
    private Button btnTabLogin;
    private Button btnTabRegister;
    private Button btnBack;
    private AppCompatButton btnSubmit;
    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private LinearLayout layoutName;
    private LinearLayout layoutConfirmPassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind Views
        tvAuthTitle = findViewById(R.id.tvAuthTitle);
        tvAuthSubtitle = findViewById(R.id.tvAuthSubtitle);
        tvToggleMode = findViewById(R.id.tvToggleMode);
        btnTabLogin = findViewById(R.id.btnTabLogin);
        btnTabRegister = findViewById(R.id.btnTabRegister);
        btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        layoutName = findViewById(R.id.layoutName);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword);
        progressBar = findViewById(R.id.progressBar);

        // Check intent extra to launch directly into register mode if requested
        if (getIntent() != null && getIntent().getBooleanExtra("is_register", false)) {
            isLoginMode = false;
        }

        updateUiMode();

        // Mode Tab Click Listeners
        btnTabLogin.setOnClickListener(v -> {
            if (!isLoginMode) {
                isLoginMode = true;
                updateUiMode();
            }
        });

        btnTabRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        tvToggleMode.setOnClickListener(v -> {
            if (isLoginMode) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            } else {
                isLoginMode = true;
                updateUiMode();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        // Form Submission
        btnSubmit.setOnClickListener(v -> handleFormSubmit());
    }

    private void updateUiMode() {
        if (isLoginMode) {
            tvAuthTitle.setText("Welcome Back");
            tvAuthSubtitle.setText("Sign in to your account");
            layoutName.setVisibility(View.GONE);
            layoutConfirmPassword.setVisibility(View.GONE);
            btnSubmit.setText("Sign In");
            tvToggleMode.setText("Don't have an account? Register");

            btnTabLogin.setBackgroundResource(R.drawable.bg_btn_red);
            btnTabLogin.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            btnTabRegister.setBackground(null);
            btnTabRegister.setTextColor(0xFF64748B);
        } else {
            tvAuthTitle.setText("Create Account");
            tvAuthSubtitle.setText("Sign up for a new account");
            layoutName.setVisibility(View.VISIBLE);
            layoutConfirmPassword.setVisibility(View.VISIBLE);
            btnSubmit.setText("Register Account");
            tvToggleMode.setText("Already have an account? Sign In");

            btnTabRegister.setBackgroundResource(R.drawable.bg_btn_red);
            btnTabRegister.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            btnTabLogin.setBackground(null);
            btnTabLogin.setTextColor(0xFF64748B);
        }
    }

    private void handleFormSubmit() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!isLoginMode) {
            String name = etName.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Name is required");
                etName.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }
        }

        setLoading(true);

        if (isLoginMode) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        setLoading(false);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String displayName = (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty())
                                    ? user.getDisplayName() : email.split("@")[0];
                            saveUserSession(email, displayName);
                            Toast.makeText(LoginActivity.this, "Welcome back, " + displayName + "!", Toast.LENGTH_SHORT).show();
                            finishWithSuccess();
                        } else {
                            // Check local fallback credentials
                            SharedPreferences pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                            String savedPwd = pref.getString("reg_pwd_" + email.toLowerCase(), null);
                            String savedName = pref.getString("reg_name_" + email.toLowerCase(), email.split("@")[0]);

                            if (savedPwd != null && savedPwd.equals(password)) {
                                saveUserSession(email, savedName);
                                Toast.makeText(LoginActivity.this, "Welcome back, " + savedName + "!", Toast.LENGTH_SHORT).show();
                                finishWithSuccess();
                            } else if (savedPwd != null) {
                                Toast.makeText(LoginActivity.this, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show();
                            } else {
                                String error = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                                Toast.makeText(LoginActivity.this, "Login Failed: " + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            String name = etName.getText().toString().trim();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                    setLoading(false);
                                    saveUserSession(email, name);
                                    Toast.makeText(LoginActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                    finishWithSuccess();
                                });
                            } else {
                                setLoading(false);
                                saveUserSession(email, name);
                                finishWithSuccess();
                            }
                        } else {
                            setLoading(false);
                            String error = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                            Toast.makeText(LoginActivity.this, "Register Failed: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSubmit.setEnabled(false);
            btnSubmit.setAlpha(0.6f);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            btnSubmit.setAlpha(1.0f);
        }
    }

    private void saveUserSession(String email, String name) {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    private void finishWithSuccess() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}