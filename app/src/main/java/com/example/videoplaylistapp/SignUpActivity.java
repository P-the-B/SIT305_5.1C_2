package com.example.videoplaylistapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videoplaylistapp.database.AppDatabase;
import com.example.videoplaylistapp.model.User;

import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {

    EditText etFullName, etUsername, etPassword, etConfirmPassword;
    Button btnCreateAccount, btnCancel;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        db = AppDatabase.getInstance(this);

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCancel = findViewById(R.id.btnCancel);

        btnCreateAccount.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                // check username isn't already taken
                User existing = db.userDao().findByUsername(username);

                runOnUiThread(() -> {
                    if (existing != null) {
                        Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show();
                    } else {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            User newUser = new User();
                            newUser.fullName = fullName;
                            newUser.username = username;
                            newUser.password = password;
                            db.userDao().insert(newUser);

                            runOnUiThread(() -> {
                                Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_SHORT).show();
                                finish(); // back to login
                            });
                        });
                    }
                });
            });
        });

        // just pop this activity off the stack — login is already underneath
        btnCancel.setOnClickListener(v -> finish());
    }
}