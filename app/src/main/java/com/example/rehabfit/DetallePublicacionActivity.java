package com.example.rehabfit;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private RecyclerView rvComentarios;
    private EditText edtComentario;
    private ImageButton btnEnviarComentario;

    private DatabaseReference refComentarios, refUsuarios;
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
        rvComentarios = findViewById(R.id.rvComentarios);
        edtComentario = findViewById(R.id.edtComentario);
        btnEnviarComentario = findViewById(R.id.btnEnviarComentario);

        listaComentarios = new ArrayList<>();
        comentarioAdapter = new ComentarioAdapter(listaComentarios);

        rvComentarios.setLayoutManager(new LinearLayoutManager(this));
        rvComentarios.setAdapter(comentarioAdapter);

        refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");

        //se obtiene el id de la publicacion enviado desde la pantalla anterior
        idPublicacion = getIntent().getStringExtra("idPublicacion");

        //verifica que exista una publicacion para mostrar
        if (idPublicacion == null) {

            Toast.makeText(this, "Publicación no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //referencia a la publicacion especifica en firebase
        refPublicacion = FirebaseDatabase.getInstance().getReference("publicacionesComunidad").child(idPublicacion);
        refComentarios = refPublicacion.child("comentarios");
        cargarComentarios();

        btnEnviarComentario.setOnClickListener(v -> guardarComentario());
        cargarDetalle();

        btnInspirar.setOnClickListener(v -> agregarApoyo());
    }

    //metodo qur obtiene los datos de la publicacion desde firebase
    private void cargarDetalle() {

        refPublicacion.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //convierte los datos obtenidos al objeto PublicacionComunidad
                publicacionActual = snapshot.getValue(PublicacionComunidad.class);

                //verifica que la publicacion siga existiendo
                if (publicacionActual == null) {
                    Toast.makeText(DetallePublicacionActivity.this, "La publicación ya no existe", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // muestra los datos de la publicacion en pantalla
                txtNombreDetalle.setText(publicacionActual.getNombreUsuario());
                txtFechaDetalle.setText(publicacionActual.getFecha() + " · Comunidad RehabFit");
                txtEjercicioDetalle.setText(publicacionActual.getEjercicio());
                txtDatosDetalle.setText(publicacionActual.getZona() + " - " + publicacionActual.getDuracion());
                txtDificultadDetalle.setText("Dificultad: " + publicacionActual.getDificultad());
                txtExperienciaDetalle.setText(publicacionActual.getExperiencia());

                verificarApoyo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetallePublicacionActivity.this, "Error al cargar detalle", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verificarApoyo() {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) return;

        refPublicacion.child("usuariosInspirados").child(usuario.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //cambia el icono segun el estado del apoyo
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

    //se agrega o elimina el apoyo del usuario a la publicacion
    private void agregarApoyo() {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) return;

        DatabaseReference refApoyo = refPublicacion.child("usuariosInspirados").child(usuario.getUid());

        refApoyo.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //si ya existe apoyo lo elimina
                if (snapshot.exists()) {

                    refApoyo.removeValue().addOnSuccessListener(unused -> {
                        Toast.makeText(DetallePublicacionActivity.this, "Apoyo eliminado", Toast.LENGTH_SHORT).show();});

                } else {

                    //si no existe registra el apoyo
                    refApoyo.setValue(true).addOnSuccessListener(unused -> {
                        Toast.makeText(DetallePublicacionActivity.this, "Gracias por apoyar", Toast.LENGTH_SHORT).show();});
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetallePublicacionActivity.this, "Error al registrar apoyo", Toast.LENGTH_SHORT).show();}
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

                Comentario comentario = new Comentario(idComentario, usuario.getUid(), nombre, texto, fecha, timestamp);

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
}