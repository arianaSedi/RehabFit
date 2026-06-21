package com.example.rehabfit;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rehabfit.models.PublicacionComunidad;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetallePublicacionActivity extends AppCompatActivity {

    private TextView txtNombreDetalle;
    private TextView txtFechaDetalle;
    private TextView txtEjercicioDetalle;
    private TextView txtDatosDetalle;
    private TextView txtDificultadDetalle;
    private TextView txtExperienciaDetalle;

    private ImageButton btnInspirar;

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

        refPublicacion = FirebaseDatabase.getInstance().getReference("publicacionesComunidad").child(idPublicacion);
        cargarDetalle();
        btnInspirar.setOnClickListener(v -> toggleApoyo());
    }

    private void cargarDetalle() {

        refPublicacion.addValueEventListener(
                new ValueEventListener() {

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

                        verificarApoyo();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(DetallePublicacionActivity.this, "Error al cargar detalle", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void verificarApoyo() {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) return;

        refPublicacion
                .child("usuariosInspirados")
                .child(usuario.getUid())
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (snapshot.exists()) {
                                    btnInspirar.setImageResource(R.drawable.ic_like);

                                } else {
                                    btnInspirar.setImageResource(R.drawable.ic_no_like);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
    }

    private void agregarApoyo() {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) return;

        DatabaseReference refApoyo = refPublicacion.child("usuariosInspirados").child(usuario.getUid());

        refApoyo.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            refApoyo.removeValue().addOnSuccessListener(unused -> {
                                        Toast.makeText(DetallePublicacionActivity.this, "Apoyo eliminado", Toast.LENGTH_SHORT).show();
                            });

                        } else {
                            refApoyo.setValue(true).addOnSuccessListener(unused -> {
                                        Toast.makeText(DetallePublicacionActivity.this, "Gracias por apoyar", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DetallePublicacionActivity.this, "Error al registrar apoyo", Toast.LENGTH_SHORT).show();
                    }
        });
    }
}