package com.example.rehabfit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        holder.txtEjercicio.setText("💪 " + p.getEjercicio());
        holder.txtZona.setText("🦵 " + p.getZona());
        holder.txtDuracion.setText("⏱️ " + p.getDuracion());
        holder.txtExperiencia.setText(p.getExperiencia());

        verificarApoyo(p, holder);
        holder.btnLike.setOnClickListener(v -> agregarApoyo(p, holder));

        holder.txtVerDetalle.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetalleClick(p);
            }
        });

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

        if (usuario == null) return;

        FirebaseDatabase.getInstance()
                .getReference("publicacionesComunidad")
                .child(publicacion.getId())
                .child("usuariosInspirados")
                .child(usuario.getUid())
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if(snapshot.exists()) {
                                    holder.btnLike.setImageResource(R.drawable.ic_like);

                                } else {
                                    holder.btnLike.setImageResource(R.drawable.ic_no_like);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
    }

    private void agregarApoyo(PublicacionComunidad publicacion, PublicacionViewHolder holder) {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if(usuario == null) return;

        DatabaseReference refApoyo = FirebaseDatabase.getInstance()
                        .getReference("publicacionesComunidad")
                        .child(publicacion.getId())
                        .child("usuariosInspirados")
                        .child(usuario.getUid());

        refApoyo.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()) {
                            refApoyo.removeValue();
                            holder.btnLike.setImageResource(R.drawable.ic_no_like);

                        }
                        else {
                            refApoyo.setValue(true);
                            holder.btnLike.setImageResource(R.drawable.ic_like);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
    static class PublicacionViewHolder extends RecyclerView.ViewHolder {

        TextView txtAvatar;
        TextView txtNombre;
        TextView txtFecha;
        TextView txtDificultad;
        TextView txtEjercicio;
        TextView txtZona;
        TextView txtDuracion;
        TextView txtExperiencia;
        ImageButton btnLike;
        TextView txtVerDetalle;

        public PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);

            txtAvatar = itemView.findViewById(R.id.txtAvatar);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtDificultad = itemView.findViewById(R.id.txtDificultad);
            txtEjercicio = itemView.findViewById(R.id.txtEjercicio);
            txtZona = itemView.findViewById(R.id.txtZona);
            txtDuracion = itemView.findViewById(R.id.txtDuracion);
            txtExperiencia = itemView.findViewById(R.id.txtExperiencia);
            btnLike = itemView.findViewById(R.id.btnInspirar);
            txtVerDetalle = itemView.findViewById(R.id.txtVerDetalle);
        }
    }
}