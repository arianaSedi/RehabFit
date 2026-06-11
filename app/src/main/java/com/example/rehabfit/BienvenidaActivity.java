package com.example.rehabfit;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BienvenidaActivity extends AppCompatActivity {

    private AppCompatButton btnCrearCuenta, btnIniciarSesion;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);

        auth = FirebaseAuth.getInstance();

        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);

        btnCrearCuenta.setOnClickListener(v -> {
            Intent intent = new Intent(BienvenidaActivity.this, CrearCuentaActivity.class);
            startActivity(intent);
        });

        btnIniciarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(BienvenidaActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser usuarioActual = auth.getCurrentUser();

        if (usuarioActual != null) {
            Intent intent = new Intent(BienvenidaActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}