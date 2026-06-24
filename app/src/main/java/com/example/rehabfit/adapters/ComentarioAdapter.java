package com.example.rehabfit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.R;
import com.example.rehabfit.models.Comentario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {

    public interface OnComentarioClick {
        void onEliminarComentario(Comentario comentario);
    }

    private List<Comentario> lista;
    private OnComentarioClick listener;

    public ComentarioAdapter(List<Comentario> lista, OnComentarioClick listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        Comentario c = lista.get(position);

        holder.txtNombreComentario.setText(c.getNombreUsuario());
        holder.txtTextoComentario.setText(c.getTexto());

        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioActual != null && c.getUid() != null && c.getUid().equals(usuarioActual.getUid())) {
            holder.btnEliminarComentario.setVisibility(View.VISIBLE);
        } else {
            holder.btnEliminarComentario.setVisibility(View.GONE);
        }

        holder.btnEliminarComentario.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarComentario(c);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ComentarioViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombreComentario;
        TextView txtTextoComentario;
        ImageButton btnEliminarComentario;

        public ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNombreComentario = itemView.findViewById(R.id.txtNombreComentario);
            txtTextoComentario = itemView.findViewById(R.id.txtTextoComentario);
            btnEliminarComentario = itemView.findViewById(R.id.btnEliminarComentario);
        }
    }
}