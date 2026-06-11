package com.example.rehabfit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.rehabfit.models.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CrearCuentaActivity extends AppCompatActivity {
    private EditText etNombre, etCorreo, etPassword, etConfirmPassword;
    private AppCompatButton btnRegistrarme;
    private TextView tvIrLogin;

    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cuenta);

        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegistrarme = findViewById(R.id.btnRegistrarme);
        tvIrLogin = findViewById(R.id.tvIrLogin);

        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        btnRegistrarme.setOnClickListener(v -> registrarUsuario());

        tvIrLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CrearCuentaActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Ingresa tu nombre");
            return;
        }

        if (TextUtils.isEmpty(correo)) {
            etCorreo.setError("Ingresa tu correo");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Ingresa una contraseña");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener mínimo 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        auth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = auth.getCurrentUser();

                        if (user != null) {
                            String uid = user.getUid();

                            Usuario usuario = new Usuario(uid, nombre, correo);

                            usuariosRef.child(uid).setValue(usuario)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(CrearCuentaActivity.this, PerfilAdaptadoActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }

                    } else {
                        Toast.makeText(this, "Error al crear cuenta: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}