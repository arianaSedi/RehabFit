package com.example.rehabfit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.R;
import com.example.rehabfit.models.PublicacionComunidad;

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
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_publicacion_comunidad, parent, false);
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
        holder.txtLikes.setText("♡ " + p.getLikes() + " Me inspira");

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

    static class PublicacionViewHolder extends RecyclerView.ViewHolder {

        TextView txtAvatar, txtNombre, txtFecha, txtDificultad;
        TextView txtEjercicio, txtZona, txtDuracion, txtExperiencia;
        TextView txtLikes, txtVerDetalle;

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
            txtLikes = itemView.findViewById(R.id.txtLikes);
            txtVerDetalle = itemView.findViewById(R.id.txtVerDetalle);
        }
    }
}
