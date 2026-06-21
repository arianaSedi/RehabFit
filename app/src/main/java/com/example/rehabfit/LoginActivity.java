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
        btnLogin = findViewById(R.id.btnLogin);
        tvIrRegistro = findViewById(R.id.tvIrRegistro);
        tvOlvidasteContra = findViewById(R.id.tvOlvidasteContra);

        //activa la opcion de mostrar y ocultar la contra
        configurarVerPassword(etLoginPassword);

        //inicializa firebase auth y la referencia a usuarios
        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        btnLogin.setOnClickListener(v -> iniciarSesion());

        //redirige a la pantalla de crear cuenta
        tvIrRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CrearCuentaActivity.class);
            startActivity(intent);
            finish();
        });

        //envia un correo para recuperar la contraseña
        tvOlvidasteContra.setOnClickListener(v -> recuperarPassword());
    }

    //metodo que valida los datos ingresados e intenta autenticar al usuario
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

        //intenta iniciar sesion con firebase authentication
        auth.signInWithEmailAndPassword(correo, password)
                .addOnCompleteListener(task -> {

                    //si las credenciales son correctas verifica el perfil adaptado
                    if (task.isSuccessful()) {
                        verificarPerfilAdaptado();
                    } else {

                        Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
                    }
                });
    }

    //metodo que verifica si el usuario tiene un perfil adaptado registrado
    private void verificarPerfilAdaptado() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        //obtiene el identificador unico del usuario
        String uid = auth.getCurrentUser().getUid();

        //consulta en firebase si existe el perfil adaptado
        usuariosRef.child(uid).child("perfilAdaptado").get().addOnSuccessListener(snapshot -> {

            //si existe el perfil entra directamente al menu principal
            if (snapshot.exists()) {
                Toast.makeText(this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

            } else {
                // si no existe lo envia a completar su perfil
                Toast.makeText(this, "Completa tu perfil adaptado", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, PerfilAdaptadoActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(e -> {
            //muestra un msj si ocurre un error al consultar la base de datos
            Toast.makeText(this, "Error al verificar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    //metood que envia un correo de recuperacion de contra al usuario
    private void recuperarPassword() {

        String correo = etLoginCorreo.getText().toString().trim();

        //verifica que el usuario haya escrito su correo
        if (TextUtils.isEmpty(correo)) {
            etLoginCorreo.setError("Escribe tu correo para recuperar contraseña");
            return;
        }

        //firebase envia automaticamente el enlace de recuperacion
        auth.sendPasswordResetEmail(correo).addOnSuccessListener(unused -> {

            //confirma que el correo fue enviado correctamente
             Toast.makeText(this, "Correo de recuperación enviado, revisa tu bandeja de spam", Toast.LENGTH_LONG).show();

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    //metodo que permite mostrar/ocultar la contra al tocar el icono del ojo
    @SuppressLint("ClickableViewAccessibility")
    private void configurarVerPassword(EditText editText) {

        //ESTA PARTE LA INVESTIGAMOS
        //detecta cuando el usuario toca el campo de texto
        editText.setOnTouchListener((v, event) -> {

            //verifica que el usuario haya soltado el dedo sobre la pantalla
            if (event.getAction() == MotionEvent.ACTION_UP) {

                //verifica que exista un icono al lado derecho del campo
                if (editText.getCompoundDrawables()[2] == null) {
                    return false;
                }

                //comprueba si el toque fue sobre el icono del ojo
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {

                    //guarda la posicion actual del cursor
                    int cursor = editText.getSelectionEnd();

                    // verifica si actualmente la contra esta oculta
                    boolean oculta = editText.getTransformationMethod()
                            instanceof PasswordTransformationMethod;

                    if (oculta) {
                        //muestra la contra y cambia el icono a ver
                        editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visible, 0);

                    } else {
                        //vuelve a ocultar la contra y restaura el icono de no ver
                        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_no_visible, 0);
                    }

                    //mantiene el cursor en la misma posicion despues del cambio
                    editText.setSelection(cursor);
                    return true;
                }
            }
            return false;
        });
    }
}