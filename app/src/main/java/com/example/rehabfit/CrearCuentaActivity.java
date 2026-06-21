package com.example.rehabfit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
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

        //activa la opcion de mostrar u ocultar contrasenas
        configurarVerPassword(etPassword);
        configurarVerPassword(etConfirmPassword);

        //inicializa firebase auth y la referencia a la tabla usuarios
        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        //ejecuta el registro cuando se presiona btn registrarme
        btnRegistrarme.setOnClickListener(v -> registrarUsuario());

        tvIrLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CrearCuentaActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    //metodo que valida los datos ingresados y crea la cuenta en firebase
    private void registrarUsuario() {

        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        //verificar que los campos no esten vacios
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

        //firebase exige minimo 6 caracteres
        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener mínimo 6 caracteres");
            return;
        }

        //comprueba que ambas contrasenas sean iguales sino muestra ese msj
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        //crea la cuenta usando correo y contra(pass)
        auth.createUserWithEmailAndPassword(correo, password).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();

                //si el usuario es diferente de nulo
                if (user != null) {

                    //obtiene el ID del usuario
                    String uid = user.getUid();

                    //crea el objeto usuario para guardarlo en la db
                    Usuario usuario = new Usuario(uid, nombre, correo);

                    //guarda la informacion del usuario en realtime database
                    usuariosRef.child(uid).setValue(usuario).addOnSuccessListener(unused -> {

                        Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show();

                        //despues del registro pasa al perfil adaptado
                                Intent intent = new Intent(CrearCuentaActivity.this, PerfilAdaptadoActivity.class);
                                startActivity(intent);
                                finish();

                    }).addOnFailureListener(e -> {
                        //muestra el error si falla el guardado
                        Toast.makeText(this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

            } else {
                Toast.makeText(this, "Error al crear cuenta: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    //permite mostrar y ocultar la contra al tocar el icono del ojo(ver)
    @SuppressLint("ClickableViewAccessibility")
    private void configurarVerPassword(EditText editText) {

        editText.setOnTouchListener((v, event) -> {

            //detecta cuando el usuario deja de presionar la pantalla
            if (event.getAction() == MotionEvent.ACTION_UP) {

                //ESTA PARTE LA INVESTIGAMOS
                //verifica que exista un icono a la derecha
                if (editText.getCompoundDrawables()[2] == null) {
                    return false;
                }

                //comprueba si se presiono  el icono
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {

                    //guarda la posicion actual del cursor
                    int cursor = editText.getSelectionEnd();

                    //verifica si la contrasena esta oculta
                    boolean oculta = editText.getTransformationMethod()
                            instanceof PasswordTransformationMethod;

                    if (oculta) {
                        //muestra la contra y cambia el icono de ver
                        editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visible, 0);

                    } else {
                        //vuelve a ocultar la contra y cambia el icono de no ver
                        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_no_visible, 0);
                    }

                    // devuelve el cursor a la posicion donde estaba
                    editText.setSelection(cursor);
                    return true;
                }
            }
            return false;
        });
    }
}