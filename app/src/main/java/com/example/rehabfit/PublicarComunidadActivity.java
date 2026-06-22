package com.example.rehabfit;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.rehabfit.models.PublicacionComunidad;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PublicarComunidadActivity extends AppCompatActivity {

    private EditText edtEjercicio, edtDuracion, edtExperiencia;
    private Spinner spZona;
    private RadioGroup rgDificultad;
    private AppCompatButton btnGuardarPublicacion, btnCancelar;

    private FirebaseAuth auth;
    private DatabaseReference refPublicaciones, refUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicar_comunidad);

        edtEjercicio = findViewById(R.id.edtEjercicio);
        edtDuracion = findViewById(R.id.edtDuracion);
        edtExperiencia = findViewById(R.id.edtExperiencia);
        spZona = findViewById(R.id.spZona);
        rgDificultad = findViewById(R.id.rgDificultad);
        btnGuardarPublicacion = findViewById(R.id.btnGuardarPublicacion);
        btnCancelar = findViewById(R.id.btnCancelar);
        RadioButton rbBaja = findViewById(R.id.rbBaja);
        RadioButton rbMedia = findViewById(R.id.rbMedia);
        RadioButton rbAlta = findViewById(R.id.rbAlta);

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {

            if (!isChecked) return;
            rbBaja.setChecked(buttonView == rbBaja);
            rbMedia.setChecked(buttonView == rbMedia);
            rbAlta.setChecked(buttonView == rbAlta);
        };

        rbBaja.setOnCheckedChangeListener(listener);
        rbMedia.setOnCheckedChangeListener(listener);
        rbAlta.setOnCheckedChangeListener(listener);

        //inicializa firebase auth y las referencias de la db
        auth = FirebaseAuth.getInstance();
        refPublicaciones = FirebaseDatabase.getInstance().getReference("publicacionesComunidad");
        refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");

        configurarSpinner();
        btnGuardarPublicacion.setOnClickListener(v -> validarYPublicar());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void configurarSpinner() {
        String[] zonas = {
                "Selecciona una zona",
                "Rodilla",
                "Hombro",
                "Espalda",
                "Cadera",
                "Pierna",
                "Brazo",
                "Cuello",
                "Tobillo"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.spinner_dropdowm_item, zonas);
        spZona.setAdapter(adapter);
    }

    //valida que todos los campos esten completos antes de publicar
    private void validarYPublicar() {
        String ejercicio = edtEjercicio.getText().toString().trim();
        String duracion = edtDuracion.getText().toString().trim();
        String experiencia = edtExperiencia.getText().toString().trim();
        String zona = spZona.getSelectedItem().toString();

        //verificaciones para los campos a publicar
        if (ejercicio.isEmpty()) {
            edtEjercicio.setError("Escribe el ejercicio realizado");
            return;
        }

        if (zona.equals("Selecciona una zona")) {
            Toast.makeText(this, "Selecciona una zona de tu cuerpo trabajada", Toast.LENGTH_SHORT).show();
            return;
        }

        if (duracion.isEmpty()) {
            edtDuracion.setError("Escribe la duración de tu entrenamiendo");
            return;
        }

        if (experiencia.isEmpty()) {
            edtExperiencia.setError("Comparte cómo te sentiste");
            return;
        }


        String dificultad = "";
        RadioButton rbBaja = findViewById(R.id.rbBaja);
        RadioButton rbMedia = findViewById(R.id.rbMedia);
        RadioButton rbAlta = findViewById(R.id.rbAlta);

        if (rbBaja.isChecked()) {
            dificultad = "Baja";
        } else if (rbMedia.isChecked()) {
            dificultad = "Media";
        } else if (rbAlta.isChecked()) {
            dificultad = "Alta";
        } else {
            Toast.makeText(this, "Selecciona la dificultad", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser usuario = auth.getCurrentUser();

        if (usuario == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // obtiene el nombre del usuario antes de guardar la publicacion
        obtenerNombreYGuardar(usuario, ejercicio, zona, duracion, dificultad, experiencia);
    }

    //metodo q obtiene el nombre del usuario desde firebase
    private void obtenerNombreYGuardar(FirebaseUser usuario, String ejercicio, String zona, String duracion, String dificultad, String experiencia) {

        refUsuarios.child(usuario.getUid()).child("nombre").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String nombre = snapshot.getValue(String.class);

                //si no existe nombre guardado busca otras opc
                if (nombre == null || nombre.isEmpty()) {

                    if (usuario.getDisplayName() != null && !usuario.getDisplayName().isEmpty()) {
                        nombre = usuario.getDisplayName();

                    } else if (usuario.getEmail() != null) {
                        nombre = usuario.getEmail();

                    } else {
                        nombre = "Usuario RehabFit";
                    }
                }

                // guarda la publicacion con todos los datos
                guardarPublicacion(usuario.getUid(), nombre, ejercicio, zona, duracion, dificultad, experiencia);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                //se muestra un error si falla la consulta
                Toast.makeText(PublicarComunidadActivity.this, "Error al obtener usuario: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    //metdo que crea y guarda la publicacion en firebase
    private void guardarPublicacion(String uid, String nombre, String ejercicio, String zona, String duracion, String dificultad, String experiencia) {

        //genera un id unico para la publicacion
        String id = refPublicaciones.push().getKey();

        if (id == null) {
            Toast.makeText(this, "Error al generar publicación", Toast.LENGTH_SHORT).show();
            return;
        }

        //genera la fecha actual en formato legible
        String fecha = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES")).format(new Date());

        //guarda la fecha en milisegundos para ordenar publicaciones
        long timestamp = System.currentTimeMillis();

        PublicacionComunidad publicacion = new PublicacionComunidad(id, uid, nombre, fecha, ejercicio, zona, duracion, dificultad, experiencia, timestamp);

        // guarda la publicacion en firebase
        refPublicaciones.child(id).setValue(publicacion).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Publicación realizada", Toast.LENGTH_SHORT).show();
            finish();

        }).addOnFailureListener(e -> {
            // muestra un mensaje si ocurre un error
            Toast.makeText(this, "Error al publicar", Toast.LENGTH_SHORT).show();
        });
    }
}