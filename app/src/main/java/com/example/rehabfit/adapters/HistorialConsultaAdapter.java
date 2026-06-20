package com.example.rehabfit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.R;
import com.example.rehabfit.models.ConsultasIA;
import com.example.rehabfit.models.Ejercicio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialConsultaAdapter extends RecyclerView.Adapter<HistorialConsultaAdapter.ViewHolder> {

    public interface OnConsultaClick {
        void onClick(ConsultasIA consulta);
    }

    public interface OnAgregarRutinaClick {
        void onAgregar(ConsultasIA consulta);
    }

    private final List<ConsultasIA> consultas;
    private final OnConsultaClick listenerDetalle;
    private final OnAgregarRutinaClick listenerAgregar;

    public HistorialConsultaAdapter(List<ConsultasIA> consultas, OnConsultaClick listenerDetalle, OnAgregarRutinaClick listenerAgregar) {
        this.consultas = consultas;
        this.listenerDetalle = listenerDetalle;
        this.listenerAgregar = listenerAgregar;
    }

    @NonNull
    @Override
    public HistorialConsultaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial_consulta, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialConsultaAdapter.ViewHolder holder, int position) {
        ConsultasIA consulta = consultas.get(position);

        holder.txtFecha.setText(formatearFecha(consulta.getFechaMillis()));
        holder.txtConsulta.setText(recortar(consulta.getConsulta(), 65));
        holder.txtZona.setText(obtenerZona(consulta));

        holder.itemView.setOnClickListener(v -> {
            if (listenerDetalle != null) {
                listenerDetalle.onClick(consulta);
            }
        });

        holder.btnAgregarRutina.setOnClickListener(v -> {
            if (consulta.getEjerciciosRecomendados() == null || consulta.getEjerciciosRecomendados().isEmpty()) {

                Toast.makeText(v.getContext(), "Esta consulta no tiene ejercicios recomendados", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listenerAgregar != null) {
                listenerAgregar.onAgregar(consulta);
            }
        });
    }

    @Override
    public int getItemCount() {
        return consultas == null ? 0 : consultas.size();
    }

    private String formatearFecha(long fechaMillis) {
        SimpleDateFormat formato = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return formato.format(new Date(fechaMillis));
    }

    private String recortar(String texto, int maximo) {
        if (texto == null || texto.trim().isEmpty()) {
            return "Consulta IA";
        }

        if (texto.length() <= maximo) {
            return texto;
        }

        return texto.substring(0, maximo) + "...";
    }

    private String obtenerZona(ConsultasIA consulta) {
        if (consulta.getEjerciciosRecomendados() != null
                && !consulta.getEjerciciosRecomendados().isEmpty()) {

            Ejercicio ejercicio = consulta.getEjerciciosRecomendados().get(0);

            if (ejercicio.getZona() != null && !ejercicio.getZona().trim().isEmpty()) {
                return ejercicio.getZona();
            }
        }

        String texto = consulta.getConsulta() == null ? "" : consulta.getConsulta().toLowerCase();

        if (texto.contains("rodilla")) return "Rodilla";
        if (texto.contains("espalda")) return "Espalda";
        if (texto.contains("tobillo") || texto.contains("pie")) return "Tobillo";
        if (texto.contains("hombro")) return "Hombro";
        if (texto.contains("cuello")) return "Cuello";
        if (texto.contains("mano") || texto.contains("muñeca")) return "Mano";

        return "General";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtFecha;
        TextView txtConsulta;
        TextView txtZona;
        AppCompatButton btnAgregarRutina;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtFecha = itemView.findViewById(R.id.txtFechaHistorialIA);
            txtConsulta = itemView.findViewById(R.id.txtConsultaHistorialIA);
            txtZona = itemView.findViewById(R.id.txtZonaHistorialIA);
            btnAgregarRutina = itemView.findViewById(R.id.btnAgregarRutinaHistorialIA);
        }
    }
}
