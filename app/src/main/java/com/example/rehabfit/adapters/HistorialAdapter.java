package com.example.rehabfit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.R;
import com.example.rehabfit.models.SesionRutina;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

    private List<SesionRutina> listaSesiones;

    public HistorialAdapter(List<SesionRutina> listaSesiones) {
        this.listaSesiones = listaSesiones;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial, parent, false);

        return new HistorialViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        SesionRutina sesion = listaSesiones.get(position);

        holder.txtFechaSesion.setText(formatearFecha(sesion.getFechaMillis()));
        holder.txtEstadoSesion.setText("✓ Completado");

        String zona = sesion.getZonaPrincipal();

        if (zona == null || zona.isEmpty()) {
            zona = "Sin zona";
        }

        holder.txtDetalleSesion.setText(
                "🏃 " + zona +
                        " · ⏱ " + sesion.getMinutosTotales() + " min" +
                        " · ✅ " + sesion.getCantidadEjercicios() + " ejercicios"
        );

        if (sesion.getDolorAntes() > 0 || sesion.getDolorDespues() > 0) {
            holder.txtDolorAntes.setText("Antes: " + sesion.getDolorAntes() + "/10");
            holder.txtDolorDespues.setText("Después: " + sesion.getDolorDespues() + "/10");
        } else {
            holder.txtDolorAntes.setText("Antes: sin dato");
            holder.txtDolorDespues.setText("Después: sin dato");
        }
    }

    @Override
    public int getItemCount() {
        return listaSesiones.size();
    }

    public void actualizarLista(List<SesionRutina> nuevaLista) {
        this.listaSesiones = nuevaLista;
        notifyDataSetChanged();
    }

    private String formatearFecha(long fechaMillis) {
        if (fechaMillis <= 0) {
            return "Sin fecha";
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
        return formato.format(new Date(fechaMillis));
    }

    public static class HistorialViewHolder extends RecyclerView.ViewHolder {

        TextView txtFechaSesion;
        TextView txtEstadoSesion;
        TextView txtDetalleSesion;
        TextView txtDolorAntes;
        TextView txtDolorDespues;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);

            txtFechaSesion = itemView.findViewById(R.id.txtFechaSesion);
            txtEstadoSesion = itemView.findViewById(R.id.txtEstadoSesion);
            txtDetalleSesion = itemView.findViewById(R.id.txtDetalleSesion);
            txtDolorAntes = itemView.findViewById(R.id.txtDolorAntes);
            txtDolorDespues = itemView.findViewById(R.id.txtDolorDespues);
        }
    }
}