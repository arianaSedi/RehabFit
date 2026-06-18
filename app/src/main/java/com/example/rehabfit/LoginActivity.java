package com.example.rehabfit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginCorreo, etLoginPassword;
    private AppCompatButton btnLogin;
    private TextView tvIrRegistro, tvOlvidasteContra;

    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginCorreo = findViewById(R.id.etLoginCorreo);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        configurarVerPassword(etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvIrRegistro = findViewById(R.id.tvIrRegistro);
        tvOlvidasteContra = findViewById(R.id.tvOlvidasteContra);

        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

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
                        verificarPerfilAdaptado();
                    } else {
                        Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void verificarPerfilAdaptado() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        usuariosRef.child(uid).child("perfilAdaptado").get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Toast.makeText(this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Completa tu perfil adaptado", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, PerfilAdaptadoActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al verificar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(this, "Correo de recuperación enviado, revisa tu bandeja de spam", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    //metodo para ver y no ver la password
    @SuppressLint("ClickableViewAccessibility")
    private void configurarVerPassword(EditText editText) {

        editText.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (editText.getCompoundDrawables()[2] == null) {
                    return false;
                }

                if (event.getRawX() >=
                        (editText.getRight()
                                - editText.getCompoundDrawables()[2].getBounds().width())) {

                    int cursor = editText.getSelectionEnd();

                    boolean oculta =
                            editText.getTransformationMethod()
                                    instanceof PasswordTransformationMethod;

                    if (oculta) {

                        editText.setTransformationMethod(
                                HideReturnsTransformationMethod.getInstance());

                        editText.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_lock,
                                0,
                                R.drawable.ic_visible,
                                0);

                    } else {

                        editText.setTransformationMethod(
                                PasswordTransformationMethod.getInstance());

                        editText.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_lock,
                                0,
                                R.drawable.ic_no_visible,
                                0);
                    }

                    editText.setSelection(cursor);
                    return true;
                }
            }

            return false;
        });
    }
}