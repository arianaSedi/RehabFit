package com.example.rehabfit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rehabfit.R;
import com.example.rehabfit.models.Comentario;
import java.util.List;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {

    private List<Comentario> lista;

    public ComentarioAdapter(List<Comentario> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        Comentario c = lista.get(position);
        holder.txtNombreComentario.setText(c.getNombreUsuario());
        holder.txtTextoComentario.setText(c.getTexto());
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombreComentario, txtTextoComentario;
        public ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombreComentario = itemView.findViewById(R.id.txtNombreComentario);
            txtTextoComentario = itemView.findViewById(R.id.txtTextoComentario);
        }
    }
}