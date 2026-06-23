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

    private void agregarApoyo(PublicacionComunidad publicacion, PublicacionViewHolder holder) {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            return;
        }

        DatabaseReference refApoyo = FirebaseDatabase.getInstance()
                .getReference("publicacionesComunidad")
                .child(publicacion.getId())
                .child("usuariosInspirados")
                .child(usuario.getUid());

        refApoyo.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // si ya habia apoyado, se elimina el apoyo
                if (snapshot.exists()) {
                    refApoyo.removeValue();
                } else {
                    // si no habia apoyado, se guarda su uid
                    refApoyo.setValue(true);
                }

                // se vuelve a verificar para cambiar el icono
                verificarApoyo(publicacion, holder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void contarInspirados(PublicacionComunidad publicacion, PublicacionViewHolder holder) {

        FirebaseDatabase.getInstance()
                .getReference("publicacionesComunidad")
                .child(publicacion.getId())
                .child("usuariosInspirados")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int total = (int) snapshot.getChildrenCount();

                        if (total == 1) {
                            holder.txtMeInspira.setText("1 Me inspira");
                        } else {
                            holder.txtMeInspira.setText(total + " Me inspira");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void contarComentarios(PublicacionComunidad publicacion, PublicacionViewHolder holder) {

        FirebaseDatabase.getInstance()
                .getReference("publicacionesComunidad")
                .child(publicacion.getId())
                .child("comentarios")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int total = (int) snapshot.getChildrenCount();

                        if (total == 1) {
                            holder.txtTotalComentarios.setText("1 comentario");
                        } else {
                            holder.txtTotalComentarios.setText(total + " comentarios");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    static class PublicacionViewHolder extends RecyclerView.ViewHolder {

        ImageView ivAvatar;
        TextView txtNombre;
        TextView txtFecha;
        TextView txtDificultad;
        TextView txtEjercicio;
        TextView txtZona;
        TextView txtDuracion;
        TextView txtExperiencia;
        ImageButton btnLike;
        TextView txtVerDetalle;
        TextView txtMeInspira;
        TextView txtTotalComentarios;

        public PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);

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