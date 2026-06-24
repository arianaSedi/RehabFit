package com.example.rehabfit;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.adapters.ComentarioAdapter;
import com.example.rehabfit.models.Comentario;
import com.example.rehabfit.models.PublicacionComunidad;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetallePublicacionActivity extends AppCompatActivity {

    private TextView txtNombreDetalle, txtFechaDetalle, txtEjercicioDetalle,
            txtDatosDetalle, txtDificultadDetalle, txtExperienciaDetalle;

    private ImageButton btnInspirar, btnEnviarComentario, btnEliminarPublicacion;
    private RecyclerView rvComentarios;
    private EditText edtComentario;

    private boolean eliminandoPublicacion = false;
    private DatabaseReference refPublicacion, refComentarios, refUsuarios;
    private String idPublicacion;

    private List<Comentario> listaComentarios;
    private ComentarioAdapter comentarioAdapter;

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
        btnEnviarComentario = findViewById(R.id.btnEnviarComentario);
        btnEliminarPublicacion = findViewById(R.id.btnEliminarPublicacion);

        rvComentarios = findViewById(R.id.rvComentarios);
        edtComentario = findViewById(R.id.edtComentario);

        idPublicacion = getIntent().getStringExtra("idPublicacion");

        if (idPublicacion == null) {
            Toast.makeText(this, "Publicación no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        refPublicacion = FirebaseDatabase.getInstance()
                .getReference("publicacionesComunidad")
                .child(idPublicacion);

        refComentarios = refPublicacion.child("comentarios");
        refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");

        listaComentarios = new ArrayList<>();

        comentarioAdapter = new ComentarioAdapter(listaComentarios, comentario -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar comentario")
                    .setMessage("¿Seguro que deseas eliminar este comentario?")
                    .setPositiveButton("Eliminar", (dialog, which) -> eliminarComentario(comentario))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        rvComentarios.setLayoutManager(new LinearLayoutManager(this));
        rvComentarios.setAdapter(comentarioAdapter);

        btnEnviarComentario.setOnClickListener(v -> guardarComentario());
        btnInspirar.setOnClickListener(v -> agregarApoyo());

        btnEliminarPublicacion.setOnClickListener(v -> {
            if (publicacionActual == null) return;

            eliminandoPublicacion = true;

            String uidAutor = publicacionActual.getUid();
            String id = publicacionActual.getId();

            refPublicacion.removeValue()
                    .addOnSuccessListener(unused -> {

                        if (uidAutor != null && id != null) {
                            refUsuarios.child(uidAutor)
                                    .child("publicaciones")
                                    .child(id)
                                    .removeValue();
                        }

                        Toast.makeText(this, "Publicación eliminada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        eliminandoPublicacion = false;
                        Toast.makeText(this, "No se pudo eliminar la publicación", Toast.LENGTH_SHORT).show();
                    });
        });

        cargarDetalle();
        cargarComentarios();
    }

    private void cargarDetalle() {
        refPublicacion.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                publicacionActual = snapshot.getValue(PublicacionComunidad.class);

                if (publicacionActual == null) {
                    if (!eliminandoPublicacion) {
                        Toast.makeText(DetallePublicacionActivity.this, "La publicación ya no existe", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    return;
                }

                txtNombreDetalle.setText(publicacionActual.getNombreUsuario());
                txtFechaDetalle.setText(publicacionActual.getFecha() + " · Comunidad RehabFit");
                txtEjercicioDetalle.setText(publicacionActual.getEjercicio());
                txtDatosDetalle.setText(publicacionActual.getZona() + " - " + publicacionActual.getDuracion());
                txtDificultadDetalle.setText("Dificultad: " + publicacionActual.getDificultad());
                txtExperienciaDetalle.setText(publicacionActual.getExperiencia());

                verificarApoyo();
                validarPermisoEliminarPublicacion();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetallePublicacionActivity.this, "Error al cargar detalle", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validarPermisoEliminarPublicacion() {
        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioActual != null
                && publicacionActual.getUid() != null
                && publicacionActual.getUid().equals(usuarioActual.getUid())) {
            btnEliminarPublicacion.setVisibility(android.view.View.VISIBLE);
        } else {
            btnEliminarPublicacion.setVisibility(android.view.View.GONE);
        }
    }

    private void eliminarPublicacion() {
        if (publicacionActual == null) return;

        refPublicacion.removeValue().addOnSuccessListener(unused -> {
            refUsuarios.child(publicacionActual.getUid())
                    .child("publicaciones")
                    .child(publicacionActual.getId())
                    .removeValue();

            Toast.makeText(this, "Publicación eliminada", Toast.LENGTH_SHORT).show();
            finish();

        }).addOnFailureListener(e ->
                Toast.makeText(this, "No se pudo eliminar la publicación", Toast.LENGTH_SHORT).show()
        );
    }

    private void verificarApoyo() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) return;

        refPublicacion.child("usuariosInspirados")
                .child(usuario.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
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

        DatabaseReference refApoyo = refPublicacion
                .child("usuariosInspirados")
                .child(usuario.getUid());

        refApoyo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    refApoyo.removeValue().addOnSuccessListener(unused -> {
                        verificarApoyo();
                        Toast.makeText(DetallePublicacionActivity.this, "Apoyo eliminado", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    refApoyo.setValue(true).addOnSuccessListener(unused -> {
                        verificarApoyo();
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

    private void cargarComentarios() {
        refComentarios.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaComentarios.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Comentario comentario = data.getValue(Comentario.class);

                    if (comentario != null) {
                        listaComentarios.add(comentario);
                    }
                }

                comentarioAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetallePublicacionActivity.this, "Error al cargar comentarios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarComentario() {
        String texto = edtComentario.getText().toString().trim();

        if (texto.isEmpty()) {
            edtComentario.setError("Escribe un comentario");
            return;
        }

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        refUsuarios.child(usuario.getUid()).child("nombre").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nombre = snapshot.getValue(String.class);

                if (nombre == null || nombre.isEmpty()) {
                    if (usuario.getEmail() != null) {
                        nombre = usuario.getEmail();
                    } else {
                        nombre = "Usuario RehabFit";
                    }
                }

                String idComentario = refComentarios.push().getKey();

                if (idComentario == null) {
                    Toast.makeText(DetallePublicacionActivity.this, "Error al comentar", Toast.LENGTH_SHORT).show();
                    return;
                }

                String fecha = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES")).format(new Date());
                long timestamp = System.currentTimeMillis();

                Comentario comentario = new Comentario(
                        idComentario,
                        usuario.getUid(),
                        nombre,
                        texto,
                        fecha,
                        timestamp
                );

                refComentarios.child(idComentario).setValue(comentario).addOnSuccessListener(unused -> {
                    edtComentario.setText("");
                    Toast.makeText(DetallePublicacionActivity.this, "Comentario publicado", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetallePublicacionActivity.this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarComentario(Comentario comentario) {
        if (comentario == null || comentario.getId() == null) return;

        refComentarios.child(comentario.getId())
                .removeValue()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Comentario eliminado", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "No se pudo eliminar el comentario", Toast.LENGTH_SHORT).show()
                );
    }
}