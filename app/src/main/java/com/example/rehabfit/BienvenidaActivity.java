package com.example.rehabfit;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class BienvenidaActivity extends AppCompatActivity {

    private AppCompatButton btnCrearCuenta, btnIniciarSesion;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);

        //inicializa firebase authentication
        auth = FirebaseAuth.getInstance();

        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);

        //abre la pantalla de crear una cuenta nueva
        btnCrearCuenta.setOnClickListener(v -> {
            Intent intent = new Intent(BienvenidaActivity.this, CrearCuentaActivity.class);
            startActivity(intent);
        });

        //abre la pantalla de inicio de sesion
        btnIniciarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(BienvenidaActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //verifica si ya hay un usuario autenticado
        FirebaseUser usuarioActual = auth.getCurrentUser();

        if (usuarioActual != null) {
            verificarPerfilAdaptado(usuarioActual.getUid());
        }
    }

    //metodo que revisa si el usuario ya tiene un perfil adaptado guardado
    private void verificarPerfilAdaptado(String uid) {
        FirebaseDatabase.getInstance().getReference("usuarios")
                .child(uid)
                .child("perfilAdaptado")
                .get()
                .addOnSuccessListener(snapshot -> {

                    //si existe el perfil, se entra directamente a la app
                    if (snapshot.exists()) {
                        Intent intent = new Intent(BienvenidaActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        //si no existe lo envia a completar su perfil
                        Intent intent = new Intent(BienvenidaActivity.this, PerfilAdaptadoActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}