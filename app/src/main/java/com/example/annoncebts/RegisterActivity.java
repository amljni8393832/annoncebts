package com.example.annoncebts;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button btnToLogin = findViewById(R.id.btn_to_login);
        btnToLogin.setOnClickListener(v -> {
            finish(); // Retourne au Login
        });
    }
}