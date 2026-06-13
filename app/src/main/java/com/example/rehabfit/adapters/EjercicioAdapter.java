package com.example.rehabfit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.R;
import com.example.rehabfit.fragments.DetalleEjerciciosFragment;
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
            DetalleEjerciciosFragment detalleFragment = DetalleEjerciciosFragment.newInstance(ejercicio);

            FragmentActivity activity = (FragmentActivity) v.getContext();

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, detalleFragment)
                    .addToBackStack(null)
                    .commit();
        });

        holder.txtGuardar.setOnClickListener(v -> {
            RutinaManager.agregarEjercicio(ejercicio, new RutinaManager.AccionCallback() {
                @Override
                public void onExito() {
                    holder.txtGuardar.setText("★");
                    Toast.makeText(v.getContext(), "Ejercicio agregado a tu rutina", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onError(String error) {
                    Toast.makeText(v.getContext(), "Error al guardar: " + error, Toast.LENGTH_LONG).show();
                }
            });
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