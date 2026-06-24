package com.example.rehabfit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.R;

import java.util.ArrayList;

// adaptador encargado de mostrar los filtros disponibles en un recycler view
public class FiltroAdapter extends RecyclerView.Adapter<FiltroAdapter.ViewHolder> {

    // interfaz para detectar cuando un filtro es seleccionado
    public interface OnFiltroClickListener {
        // metodo ejecutado al seleccionar un filtro
        void onFiltroClick(String filtro);
    }
    // lista que almacena los filtros disponibles
    private final ArrayList<String> filtros;
    // listener utilizado para notificar la seleccion de un filtro
    private final OnFiltroClickListener listener;
    // filtro seleccionado por defecto
    private String filtroSeleccionado = "Todos";

    // constructor que recibe la lista de filtros y el listener
    public FiltroAdapter(ArrayList<String> filtros, OnFiltroClickListener listener) {
        this.filtros = filtros;
        this.listener = listener;
    }
    // actualiza el filtro seleccionado y refresca la lista
    public void setFiltroSeleccionado(String filtro) {
        filtroSeleccionado = filtro;
        // actualiza todos los elementos del recycler view
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // infla el layout correspondiente a cada filtro
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filtro, parent, false);
        // retorna un nuevo view holder
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // obtiene el filtro correspondiente a la posicion actual
        String filtro = filtros.get(position);
        // muestra el nombre del filtro
        holder.txtFiltro.setText(filtro);
        // verifica si el filtro actual es el seleccionado
        if (filtro.equalsIgnoreCase(filtroSeleccionado)) {
            // aplica el estilo visual para filtro seleccionado
            holder.txtFiltro.setBackgroundResource(R.drawable.bg_chip_verde);

        } else {
            // aplica el estilo visual para filtros no seleccionados
            holder.txtFiltro.setBackgroundResource(R.drawable.bg_chip_gris);
        }
        // detecta cuando el usuario presiona un filtro
        holder.txtFiltro.setOnClickListener(v -> {
            // notifica el filtro seleccionado al listener
            listener.onFiltroClick(filtro);
        });
    }
    @Override
    public int getItemCount() {
        // devuelve la cantidad total de filtros
        return filtros.size();
    }

    // clase encargada de almacenar las referencias de las vistas
    static class ViewHolder extends RecyclerView.ViewHolder {
        // componente visual donde se muestra el nombre del filtro
        TextView txtFiltro;
        ViewHolder(View itemView) {
            super(itemView);
            // vincula la variable con el textview del layout
            txtFiltro = itemView.findViewById(R.id.txtFiltro);
        }
    }
}