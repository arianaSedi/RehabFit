package com.example.rehabfit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rehabfit.R;
import com.example.rehabfit.models.Ejercicio;

import java.util.List;

public class EjercicioDetalleAdapter extends RecyclerView.Adapter<EjercicioDetalleAdapter.ViewHolder> {

    private final List<Ejercicio> ejercicios;
    public EjercicioDetalleAdapter(List<Ejercicio> ejercicios) {
        this.ejercicios = ejercicios;
    }

    @NonNull
    @Override
    public EjercicioDetalleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ejercicio_detalle, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioDetalleAdapter.ViewHolder holder, int position) {
        Ejercicio ejercicio = ejercicios.get(position);

        holder.txtNombre.setText(ejercicio.getNombre());
        holder.txtDetalle.setText(ejercicio.getZona() + " · " + ejercicio.getNivel() + " · " + ejercicio.getPosicion());
        holder.txtTiempo.setText("⏱ "+ ejercicio.getDuracionMinutos() + " min · " + ejercicio.getRepeticiones() + " rep");
        holder.txtDescripcion.setText(ejercicio.getDescripcion());
    }

    @Override
    public int getItemCount() {
        return ejercicios == null ? 0 : ejercicios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre;
        TextView txtDetalle;
        TextView txtTiempo;
        TextView txtDescripcion;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreEjercicioDetalleIA);
            txtDetalle = itemView.findViewById(R.id.txtDetalleEjercicioDetalleIA);
            txtTiempo = itemView.findViewById(R.id.txtTiempoEjercicioDetalleIA);
            txtDescripcion = itemView.findViewById(R.id.txtDescripcionEjercicioDetalleIA);
        }
    }
}
