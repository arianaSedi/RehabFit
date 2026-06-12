package com.example.rehabfit.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.R;
import com.example.rehabfit.models.Ejercicio;
import com.example.rehabfit.utils.RutinaManager;

import java.util.List;

public class EjercicioAdapter extends RecyclerView.Adapter<EjercicioAdapter.EjercicioViewHolder> {

    private List<Ejercicio> listaEjercicios;

    public EjercicioAdapter(List<Ejercicio> listaEjercicios) {
        this.listaEjercicios = listaEjercicios;
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ejercicio, parent, false);

        return new EjercicioViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        Ejercicio ejercicio = listaEjercicios.get(position);

        holder.txtNombreEjercicio.setText(ejercicio.getNombre());
        holder.txtDatosEjercicio.setText(ejercicio.getZona() + "   " + ejercicio.getNivel() + "   " + ejercicio.getPosicion());
        holder.txtDuracionEjercicio.setText("⏱ " + ejercicio.getDuracionMinutos() + " min · " + ejercicio.getRepeticiones() + " rep");
        holder.txtGuardar.setText("☆");
        holder.itemView.setOnClickListener(v -> {
            String mensaje = "Zona: " + ejercicio.getZona() + "\n" +
                            "Nivel: " + ejercicio.getNivel() + "\n" +
                            "Posición: " + ejercicio.getPosicion() + "\n" +
                            "Duración: " + ejercicio.getDuracionMinutos() + " minutos\n" +
                            "Repeticiones: " + ejercicio.getRepeticiones() + "\n\n" +
                            "Descripción:\n" + ejercicio.getDescripcion() + "\n\n" +
                            "Advertencia:\n" + ejercicio.getAdvertencia();

            new AlertDialog.Builder(v.getContext())
                    .setTitle(ejercicio.getNombre())
                    .setMessage(mensaje)
                    .setPositiveButton("Entendido", null)
                    .show();
        });

        holder.txtGuardar.setOnClickListener(v -> {
            RutinaManager.agregarEjercicio(ejercicio);
            holder.txtGuardar.setText("★");

            Toast.makeText(v.getContext(), "Ejercicio agregado a tu rutina", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return listaEjercicios.size();
    }

    public void actualizarLista(List<Ejercicio> nuevaLista) {
        this.listaEjercicios = nuevaLista;
        notifyDataSetChanged();
    }

    public static class EjercicioViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombreEjercicio;
        TextView txtDatosEjercicio;
        TextView txtDuracionEjercicio;
        TextView txtGuardar;

        public EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNombreEjercicio = itemView.findViewById(R.id.txtNombreEjercicio);
            txtDatosEjercicio = itemView.findViewById(R.id.txtDatosEjercicio);
            txtDuracionEjercicio = itemView.findViewById(R.id.txtDuracionEjercicio);
            txtGuardar = itemView.findViewById(R.id.txtGuardar);
        }
    }
}