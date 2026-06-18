package com.example.rehabfit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.R;
import com.example.rehabfit.models.Ejercicio;
import com.example.rehabfit.utils.RutinaManager;

import java.util.List;

public class RutinaAdapter extends RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder> {

    private List<Ejercicio> listaRutina;
    private OnRutinaChangeListener listener;

    public interface OnRutinaChangeListener {
        void onRutinaCambiada();
    }

    public RutinaAdapter(List<Ejercicio> listaRutina, OnRutinaChangeListener listener) {
        this.listaRutina = listaRutina;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RutinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rutina, parent, false);
        return new RutinaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull RutinaViewHolder holder, int position) {
        Ejercicio ejercicio = listaRutina.get(position);

        holder.txtNumeroRutina.setText(String.valueOf(position + 1));
        holder.txtNombreRutina.setText(ejercicio.getNombre());

        holder.txtDetalleRutina.setText(ejercicio.getZona() + " · " + ejercicio.getDuracionMinutos() + " min · " + ejercicio.getRepeticiones() + " rep · " + ejercicio.getNivel());

        holder.EliminarRutina.setOnClickListener(v -> {
            RutinaManager.eliminarEjercicio(ejercicio, new RutinaManager.AccionCallback() {
                @Override
                public void onExito() {
                    notifyDataSetChanged();

                    if (listener != null) {
                        listener.onRutinaCambiada();
                    }

                    Toast.makeText(v.getContext(), "Ejercicio eliminado", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(v.getContext(), "Error al eliminar: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return listaRutina.size();
    }

    public static class RutinaViewHolder extends RecyclerView.ViewHolder {

        TextView txtNumeroRutina;
        TextView txtNombreRutina;
        TextView txtDetalleRutina;
        ImageView EliminarRutina;

        public RutinaViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNumeroRutina = itemView.findViewById(R.id.txtNumeroRutina);
            txtNombreRutina = itemView.findViewById(R.id.txtNombreRutina);
            txtDetalleRutina = itemView.findViewById(R.id.txtDetalleRutina);
            EliminarRutina = itemView.findViewById(R.id.EliminarRutina);
        }
    }
}