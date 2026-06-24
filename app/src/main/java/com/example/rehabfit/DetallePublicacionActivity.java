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
        //inizializar los componentes visuales
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
        //  obtiene el id de la publicacion enviado desde la pantalla anterior
        idPublicacion = getIntent().getStringExtra("idPublicacion");
        // valida que exista una publicacion seleccionada
        if (idPublicacion == null) {
            Toast.makeText(this, "Publicación no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // referencia a la publicacion dentro de firebase
        refPublicacion = FirebaseDatabase.getInstance()
                .getReference("publicacionesComunidad")
                .child(idPublicacion);
        // referencia a los comentarios de la publicacion
        refComentarios = refPublicacion.child("comentarios");
        // referencia a los usuarios registrados
        refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");
        // inicializa la lista de comentarios
        listaComentarios = new ArrayList<>();
        // crea el adaptador de comentarios
        comentarioAdapter = new ComentarioAdapter(listaComentarios, comentario -> {
            // muestra una confirmacion antes de eliminar un comentario
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar comentario")
                    .setMessage("¿Seguro que deseas eliminar este comentario?")
                    .setPositiveButton("Eliminar", (dialog, which) -> eliminarComentario(comentario))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
        // configura el recycler view
        rvComentarios.setLayoutManager(new LinearLayoutManager(this));
        rvComentarios.setAdapter(comentarioAdapter);
// evento para enviar comentario
        btnEnviarComentario.setOnClickListener(v -> guardarComentario());
        // evento para apoyar o quitar apoyo
        btnInspirar.setOnClickListener(v -> agregarApoyo());
        // evento para eliminar una publicacion
        btnEliminarPublicacion.setOnClickListener(v -> {
            // valida que exista una publicacion cargada
            if (publicacionActual == null) return;

            eliminandoPublicacion = true;

            String uidAutor = publicacionActual.getUid();
            String id = publicacionActual.getId();
// elimina la publicacion de firebase
            refPublicacion.removeValue()
                    .addOnSuccessListener(unused -> {
                        // elimina la referencia almacenada en el perfil del usuario
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
    // obtiene los datos de la publicacion desde firebase
    private void cargarDetalle() {
        refPublicacion.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // convierte los datos recibidos en un objeto publicacion
                publicacionActual = snapshot.getValue(PublicacionComunidad.class);
// valida si la publicacion aun existe
                if (publicacionActual == null) {
                    if (!eliminandoPublicacion) {
                        Toast.makeText(DetallePublicacionActivity.this, "La publicación ya no existe", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    return;
                }
// muestra los datos en pantalla
                txtNombreDetalle.setText(publicacionActual.getNombreUsuario());
                txtFechaDetalle.setText(publicacionActual.getFecha() + " · Comunidad RehabFit");
                txtEjercicioDetalle.setText(publicacionActual.getEjercicio());
                txtDatosDetalle.setText(publicacionActual.getZona() + " - " + publicacionActual.getDuracion());
                txtDificultadDetalle.setText("Dificultad: " + publicacionActual.getDificultad());
                txtExperienciaDetalle.setText(publicacionActual.getExperiencia());
                // verifica si el usuario ya apoyo la publicacion
                verificarApoyo();
                // valida si el usuario puede eliminar la publicacion
                validarPermisoEliminarPublicacion();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // muestra error si no se pudo cargar la informacion
                Toast.makeText(DetallePublicacionActivity.this, "Error al cargar detalle", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // valida si la publicacion pertenece al usuario actual
    private void validarPermisoEliminarPublicacion() {
        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioActual != null
                && publicacionActual.getUid() != null
                && publicacionActual.getUid().equals(usuarioActual.getUid())) {
            // muestra el boton eliminar
            btnEliminarPublicacion.setVisibility(android.view.View.VISIBLE);
        } else {
            // oculta el boton eliminar
            btnEliminarPublicacion.setVisibility(android.view.View.GONE);
        }
    }
    // verifica si el usuario ya dio apoyo a la publicacion
    private void verificarApoyo() {
        // obtiene el usuario que tiene la sesion iniciada
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
    // agrega o elimina apoyo a la publicacion
    private void agregarApoyo() {
        // obtiene el usuario que tiene la sesion iniciada
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        // si no hay usuario, se detiene el metodo
        if (usuario == null) return;
// crea la referencia donde se guarda el apoyo del usuario actual
        DatabaseReference refApoyo = refPublicacion
                .child("usuariosInspirados")
                .child(usuario.getUid());
// consulta una sola vez si el usuario ya habia dado apoyo
        refApoyo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // si ya existe el apoyo, lo elimina
                if (snapshot.exists()) {
                    refApoyo.removeValue().addOnSuccessListener(unused -> {
                        // actualiza el icono del boton de apoyo
                        verificarApoyo();
                        // muestra mensaje al usuario
                        Toast.makeText(DetallePublicacionActivity.this, "Apoyo eliminado", Toast.LENGTH_SHORT).show();
                    });
                } else { // si no existe el apoyo, lo registra en firebase
                    refApoyo.setValue(true).addOnSuccessListener(unused -> {
                        // actualiza el icono del boton de apoyo
                        verificarApoyo();
                        // muestra mensaje al usuario
                        Toast.makeText(DetallePublicacionActivity.this, "Gracias por apoyar", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // muestra un mensaje si ocurre un error al registrar el apoyo
                Toast.makeText(DetallePublicacionActivity.this, "Error al registrar apoyo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarComentarios() {
        // obtiene los comentarios ordenados por la fecha en que fueron creados
        refComentarios.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // limpia la lista antes de volver a llenarla
                listaComentarios.clear();
// recorre todos los comentarios encontrados en firebase
                for (DataSnapshot data : snapshot.getChildren()) {
                    // convierte cada registro en un objeto comentario
                    Comentario comentario = data.getValue(Comentario.class);
// valida que el comentario no sea nulo
                    if (comentario != null) {
                        // agrega el comentario a la lista
                        listaComentarios.add(comentario);
                    }
                }
// actualiza el recycler view para mostrar los comentarios
                comentarioAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // muestra un mensaje si ocurre un error al cargar comentarios
                Toast.makeText(DetallePublicacionActivity.this, "Error al cargar comentarios", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // metodo para guardar un nuevo comentario
    private void guardarComentario() {
        // obtiene el texto escrito por el usuario y elimina espacios al inicio y al final
        String texto = edtComentario.getText().toString().trim();
        // valida que el campo de comentario no este vacio
        if (texto.isEmpty()) {
            edtComentario.setError("Escribe un comentario");
            return;
        }
// obtiene el usuario que tiene la sesion iniciada
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            // valida que exista un usuario autenticado
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }
        // obtiene el nombre del usuario desde firebase
        refUsuarios.child(usuario.getUid()).child("nombre").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // guarda el nombre obtenido desde la base de datos
                String nombre = snapshot.getValue(String.class);
                // valida si el nombre esta vacio o no existe
                if (nombre == null || nombre.isEmpty()) {
                    // si existe correo, se usa como nombre
                    if (usuario.getEmail() != null) {
                        nombre = usuario.getEmail();
                        // si no hay correo, se asigna un nombre por defecto
                    } else {
                        nombre = "Usuario RehabFit";
                    }
                }// genera un id unico para el comentario

                String idComentario = refComentarios.push().getKey();
// valida que el id se haya generado correctamente
                if (idComentario == null) {
                    Toast.makeText(DetallePublicacionActivity.this, "Error al comentar", Toast.LENGTH_SHORT).show();
                    return;
                }
                // genera la fecha del comentario
                String fecha = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES")).format(new Date());
                // obtiene la hora exacta en milisegundos para ordenar comentarios
                long timestamp = System.currentTimeMillis();
                // crea el objeto comentario con todos sus datos
                Comentario comentario = new Comentario(
                        idComentario,
                        usuario.getUid(),
                        nombre,
                        texto,
                        fecha,
                        timestamp
                );
                // guarda el comentario dentro de firebase
                refComentarios.child(idComentario).setValue(comentario).addOnSuccessListener(unused -> {
                    // limpia el campo de texto despues de comentar
                    edtComentario.setText("");
                    // muestra mensaje de confirmacion
                    Toast.makeText(DetallePublicacionActivity.this, "Comentario publicado", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // muestra un mensaje si no se pudo obtener el usuario
                Toast.makeText(DetallePublicacionActivity.this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // metodo para eliminar un comentario
    private void eliminarComentario(Comentario comentario) {
        if (comentario == null || comentario.getId() == null) return;
// valida que el comentario exista y tenga id
        refComentarios.child(comentario.getId())
                .removeValue()
                .addOnSuccessListener(unused ->
                        // muestra mensaje si el comentario fue eliminado
                        Toast.makeText(this, "Comentario eliminado", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        // muestra mensaje si no se pudo eliminar
                        Toast.makeText(this, "No se pudo eliminar el comentario", Toast.LENGTH_SHORT).show()
                );
    }
}