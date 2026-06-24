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

// adaptador encargado de mostrar el detalle de los ejercicios en un recycler view
public class EjercicioDetalleAdapter extends RecyclerView.Adapter<EjercicioDetalleAdapter.ViewHolder> {

    // lista que contiene los ejercicios a mostrar
    private final List<Ejercicio> ejercicios;
    // constructor que recibe la lista de ejercicios
    public EjercicioDetalleAdapter(List<Ejercicio> ejercicios) {
        this.ejercicios = ejercicios;
    }

    @NonNull
    @Override
    public EjercicioDetalleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // infla el layout de cada elemento de la lista
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ejercicio_detalle, parent, false);

        // retorna un nuevo view holder
        return new ViewHolder(vista);
    }
    @Override
    public void onBindViewHolder(@NonNull EjercicioDetalleAdapter.ViewHolder holder, int position) {

        // obtiene el ejercicio correspondiente a la posicion actual
        Ejercicio ejercicio = ejercicios.get(position);

        // muestra el nombre del ejercicio
        holder.txtNombre.setText(ejercicio.getNombre());

        // muestra zona del cuerpo, nivel y posicion del ejercicio
        holder.txtDetalle.setText(
                ejercicio.getZona() + " · " +
                        ejercicio.getNivel() + " · " +
                        ejercicio.getPosicion()
        );
        // muestra duracion y cantidad de repeticiones
        holder.txtTiempo.setText(
                "⏱ " + ejercicio.getDuracionMinutos() +
                        " min · " +
                        ejercicio.getRepeticiones() +
                        " rep"
        );
        // muestra la descripcion del ejercicio
        holder.txtDescripcion.setText(ejercicio.getDescripcion());
    }

    @Override
    public int getItemCount() {
        // devuelve la cantidad de ejercicios disponibles
        // si la lista es nula retorna cero
        return ejercicios == null ? 0 : ejercicios.size();
    }

    // clase que almacena las referencias de las vistas de cada elemento
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // campos de texto donde se muestran los datos del ejercicio
        TextView txtNombre;
        TextView txtDetalle;
        TextView txtTiempo;
        TextView txtDescripcion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // vincula cada variable con su componente visual correspondiente
            txtNombre = itemView.findViewById(R.id.txtNombreEjercicioDetalleIA);
            txtDetalle = itemView.findViewById(R.id.txtDetalleEjercicioDetalleIA);
            txtTiempo = itemView.findViewById(R.id.txtTiempoEjercicioDetalleIA);
            txtDescripcion = itemView.findViewById(R.id.txtDescripcionEjercicioDetalleIA);
        }
    }
}