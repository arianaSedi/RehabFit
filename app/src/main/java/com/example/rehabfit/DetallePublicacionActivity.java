package com.example.rehabfit;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rehabfit.models.PublicacionComunidad;
import com.google.firebase.database.*;

public class DetallePublicacionActivity extends AppCompatActivity {

    private TextView txtNombreDetalle, txtFechaDetalle, txtEjercicioDetalle;
    private TextView txtDatosDetalle, txtDificultadDetalle, txtExperienciaDetalle;
    private Button btnInspirar;

    private DatabaseReference refPublicacion;
    private String idPublicacion;
    private PublicacionComunidad publicacionActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_publicacion);

        txtNombreDetalle = findViewById(R.id.txtNombreDetalle);
        txtFechaDetalle = findViewById(R.id.txtFechaDetalle);
        txtEjercicioDetalle = findViewById(R.id.txtEjercicioDetalle);
        txtDatosDetalle = findViewById(R.id.txtDatosDetalle);
        txtDificultadDetalle = findViewById(R.id.txtDificultadDetalle);
        txtExperienciaDetalle = findViewById(R.id.txtExperienciaDetalle);
        btnInspirar = findViewById(R.id.btnInspirar);

        idPublicacion = getIntent().getStringExtra("idPublicacion");

        if (idPublicacion == null) {
            Toast.makeText(this, "Publicación no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        refPublicacion = FirebaseDatabase.getInstance()
                .getReference("publicacionesComunidad")
                .child(idPublicacion);

        cargarDetalle();

        btnInspirar.setOnClickListener(v -> sumarLike());
    }
    private void cargarDetalle() {
        refPublicacion.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                publicacionActual = snapshot.getValue(PublicacionComunidad.class);

                if (publicacionActual == null) {
                    Toast.makeText(DetallePublicacionActivity.this, "La publicación ya no existe", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                txtNombreDetalle.setText(publicacionActual.getNombreUsuario());
                txtFechaDetalle.setText(publicacionActual.getFecha() + " · Comunidad RehabFit");
                txtEjercicioDetalle.setText(publicacionActual.getEjercicio());
                txtDatosDetalle.setText("🦵 " + publicacionActual.getZona() + "   ⏱️ " + publicacionActual.getDuracion());
                txtDificultadDetalle.setText("Dificultad: " + publicacionActual.getDificultad());
                txtExperienciaDetalle.setText(publicacionActual.getExperiencia());
                btnInspirar.setText("♡ " + publicacionActual.getLikes() + " Me inspira");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetallePublicacionActivity.this, "Error al cargar detalle", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void sumarLike() {
        if (publicacionActual == null) return;

        int nuevosLikes = publicacionActual.getLikes() + 1;

        refPublicacion.child("likes").setValue(nuevosLikes)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Gracias por apoyar 💚", Toast.LENGTH_SHORT).show()
                );
    }
}