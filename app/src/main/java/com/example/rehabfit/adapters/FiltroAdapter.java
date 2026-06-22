package com.example.rehabfit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.R;

import java.util.ArrayList;

public class FiltroAdapter extends RecyclerView.Adapter<FiltroAdapter.ViewHolder> {

    public interface OnFiltroClickListener {
        void onFiltroClick(String filtro);
    }

    private final ArrayList<String> filtros;
    private final OnFiltroClickListener listener;
    private String filtroSeleccionado = "Todos";

    public FiltroAdapter(ArrayList<String> filtros, OnFiltroClickListener listener) {
        this.filtros = filtros;
        this.listener = listener;
    }

    public void setFiltroSeleccionado(String filtro) {
        filtroSeleccionado = filtro;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filtro, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String filtro = filtros.get(position);

        holder.txtFiltro.setText(filtro);

        if (filtro.equalsIgnoreCase(filtroSeleccionado)) {
            holder.txtFiltro.setBackgroundResource(R.drawable.bg_chip_verde);
        } else {
            holder.txtFiltro.setBackgroundResource(R.drawable.bg_chip_gris);
        }

        holder.txtFiltro.setOnClickListener(v -> {
            listener.onFiltroClick(filtro);
        });
    }

    @Override
    public int getItemCount() {
        return filtros.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtFiltro;
        ViewHolder(View itemView) {
            super(itemView);
            txtFiltro = itemView.findViewById(R.id.txtFiltro);
        }
    }
}