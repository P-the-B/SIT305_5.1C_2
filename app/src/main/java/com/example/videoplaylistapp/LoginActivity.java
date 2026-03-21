package com.example.videoplaylistapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videoplaylistapp.database.AppDatabase;
import com.example.videoplaylistapp.model.User;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin, btnGoSignUp;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoSignUp = findViewById(R.id.btnGoSignUp);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Room queries must run off the main thread
            Executors.newSingleThreadExecutor().execute(() -> {
                User user = db.userDao().login(username, password);

                runOnUiThread(() -> {
                    if (user != null) {
                        // pass userId to HomeActivity so playlist stays user-specific
                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.putExtra("userId", user.id);
                        intent.putExtra("username", user.username);
                        startActivity(intent);
                        finish(); // remove login from back stack
                    } else {
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        btnGoSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class))
        );
    }
}