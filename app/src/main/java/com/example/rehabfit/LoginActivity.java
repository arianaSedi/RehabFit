package com.example.rehabfit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginCorreo, etLoginPassword;
    private AppCompatButton btnLogin;
    private TextView tvIrRegistro, tvOlvidasteContra;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginCorreo = findViewById(R.id.etLoginCorreo);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvIrRegistro = findViewById(R.id.tvIrRegistro);
        tvOlvidasteContra = findViewById(R.id.tvOlvidasteContra);

        auth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> iniciarSesion());

        tvIrRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CrearCuentaActivity.class);
            startActivity(intent);
            finish();
        });

        tvOlvidasteContra.setOnClickListener(v -> recuperarPassword());
    }

    private void iniciarSesion() {
        String correo = etLoginCorreo.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) {
            etLoginCorreo.setError("Ingresa tu correo");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etLoginPassword.setError("Ingresa tu contraseña");
            return;
        }

        auth.signInWithEmailAndPassword(correo, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void recuperarPassword() {
        String correo = etLoginCorreo.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) {
            etLoginCorreo.setError("Escribe tu correo para recuperar contraseña");
            return;
        }

        auth.sendPasswordResetEmail(correo)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Correo de recuperación enviado", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}