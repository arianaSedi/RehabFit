package com.example.rehabfit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.R;
import com.example.rehabfit.models.PublicacionComunidad;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.PublicacionViewHolder> {

    public interface OnPublicacionClick {
        void onDetalleClick(PublicacionComunidad publicacion);
    }

    private List<PublicacionComunidad> lista;
    private OnPublicacionClick listener;

    public PublicacionAdapter(List<PublicacionComunidad> lista, OnPublicacionClick listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PublicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_publicacion_comunidad, parent, false);
        return new PublicacionViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicacionViewHolder holder, int position) {

        PublicacionComunidad p = lista.get(position);

        holder.txtNombre.setText(p.getNombreUsuario());
        holder.txtFecha.setText(p.getFecha());
        holder.txtDificultad.setText(p.getDificultad());
        holder.txtEjercicio.setText(p.getEjercicio());
        holder.txtZona.setText(p.getZona());
        holder.txtDuracion.setText(p.getDuracion());
        holder.txtExperiencia.setText(p.getExperiencia());

        // revisa si el usuario actual ya apoyo esta publicacion
        verificarApoyo(p, holder);

        // actualiza el contador de personas que marcaron me inspira
        contarInspirados(p, holder);

        // actualiza el contador de comentarios de la publicacion
        contarComentarios(p, holder);

        // permite agregar o quitar el apoyo al tocar el icono
        holder.btnLike.setOnClickListener(v -> agregarApoyo(p, holder));

        // abre la pantalla de detalle desde el texto
        holder.txtVerDetalle.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetalleClick(p);
            }
        });

        // tambien abre el detalle al tocar cualquier parte de la tarjeta
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetalleClick(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    private void verificarApoyo(PublicacionComunidad publicacion, PublicacionViewHolder holder) {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            return;
        }

        FirebaseDatabase.getInstance()
                .getReference("publicacionesComunidad")
                .child(publicacion.getId())
                .child("usuariosInspirados")
                .child(usuario.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // si el uid existe en firebase significa que este usuario ya dio apoyo
                        if (snapshot.exists()) {
                            holder.btnLike.setImageResource(R.drawable.ic_like);
                            holder.btnLike.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark
                            ));
                        } else {
                            holder.btnLike.setImageResource(R.drawable.ic_no_like);
                            holder.btnLike.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.texto_secundario
                            ));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
    // metodo para agregar o quitar apoyo a una publicacion desde la lista
    private void agregarApoyo(PublicacionComunidad publicacion, PublicacionViewHolder holder) {

        // obtiene el usuario que tiene la sesion iniciada
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        // si no hay usuario, se detiene el metodo
        if (usuario == null) {
            return;
        }

        // crea la referencia donde se guarda el apoyo del usuario actual
        DatabaseReference refApoyo = FirebaseDatabase.getInstance()
                .getReference("publicacionesComunidad")
                .child(publicacion.getId())
                .child("usuariosInspirados")
                .child(usuario.getUid());

        // consulta una sola vez si el usuario ya habia dado apoyo
        refApoyo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // si el apoyo ya existe, lo elimina
                if (snapshot.exists()) {
                    refApoyo.removeValue().addOnSuccessListener(unused -> {
                        // actualiza el icono del boton
                        verificarApoyo(publicacion, holder);
                        // actualiza el contador de apoyos
                        contarInspirados(publicacion, holder);
                    });
                } else {
                    // si el apoyo no existe, lo registra
                    refApoyo.setValue(true).addOnSuccessListener(unused -> {
                        // actualiza el icono del boton
                        verificarApoyo(publicacion, holder);
                        // actualiza el contador de apoyos
                        contarInspirados(publicacion, holder);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // metodo para contar cuantos usuarios apoyaron una publicacion
    private void contarInspirados(PublicacionComunidad publicacion, PublicacionViewHolder holder) {

        // accede al nodo donde estan los usuarios que dieron apoyo
        FirebaseDatabase.getInstance()
                .getReference("publicacionesComunidad")
                .child(publicacion.getId())
                .child("usuariosInspirados")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // obtiene la cantidad total de apoyos
                        int total = (int) snapshot.getChildrenCount();

                        // muestra el texto en singular si solo hay un apoyo
                        if (total == 1) {
                            holder.txtMeInspira.setText("1 Me inspira");
                        } else {
                            // muestra el texto en plural si hay cero o mas de un apoyo
                            holder.txtMeInspira.setText(total + " Me inspira");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    // metodo para contar cuantos comentarios tiene una publicacion
    private void contarComentarios(PublicacionComunidad publicacion, PublicacionViewHolder holder) {

        // accede al nodo de comentarios de la publicacion
        FirebaseDatabase.getInstance()
                .getReference("publicacionesComunidad")
                .child(publicacion.getId())
                .child("comentarios")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // obtiene la cantidad total de comentarios
                        int total = (int) snapshot.getChildrenCount();

                        // muestra el texto en singular si solo hay un comentario
                        if (total == 1) {
                            holder.txtTotalComentarios.setText("1 comentario");
                        } else {
                            // muestra el texto en plural si hay cero o mas de un comentario
                            holder.txtTotalComentarios.setText(total + " comentarios");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    // view holder que almacena las vistas de cada publicacion
    static class PublicacionViewHolder extends RecyclerView.ViewHolder {

        // imagen del avatar del usuario
        ImageView ivAvatar;
        // textos con la informacion principal de la publicacion
        TextView txtNombre;
        TextView txtFecha;
        TextView txtDificultad;
        TextView txtEjercicio;
        TextView txtZona;
        TextView txtDuracion;
        TextView txtExperiencia;
        // boton para dar o quitar apoyo
        ImageButton btnLike;
        // texto para abrir el detalle de la publicacion
        TextView txtVerDetalle;
        // texto que muestra la cantidad de apoyos
        TextView txtMeInspira;
        // texto que muestra la cantidad de comentarios
        TextView txtTotalComentarios;

        public PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);

            // vincula cada variable con su componente visual del layout
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtDificultad = itemView.findViewById(R.id.txtDificultad);
            txtEjercicio = itemView.findViewById(R.id.txtEjercicio);
            txtZona = itemView.findViewById(R.id.txtZona);
            txtDuracion = itemView.findViewById(R.id.txtDuracion);
            txtExperiencia = itemView.findViewById(R.id.txtExperiencia);
            btnLike = itemView.findViewById(R.id.btnInspirar);
            txtMeInspira = itemView.findViewById(R.id.txtMeInspira);
            txtTotalComentarios = itemView.findViewById(R.id.txtTotalComentarios);
            txtVerDetalle = itemView.findViewById(R.id.txtVerDetalle);
        }
    }
}