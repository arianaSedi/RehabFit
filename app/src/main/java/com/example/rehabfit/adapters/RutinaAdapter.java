package com.example.rehabfit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rehabfit.R;
import com.example.rehabfit.models.Ejercicio;
import com.example.rehabfit.utils.RutinaManager;

import java.util.List;

// adaptador encargado de mostrar los ejercicios agregados a la rutina
public class RutinaAdapter extends RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder> {
    // lista que contiene los ejercicios de la rutina
    private List<Ejercicio> listaRutina;
    // listener para avisar cuando la rutina cambia
    private OnRutinaChangeListener listener;

    // interfaz que permite comunicar cambios al fragment o activity
    public interface OnRutinaChangeListener {
        // metodo que se ejecuta cuando la rutina fue modificada
        void onRutinaCambiada();
    }

    // constructor que recibe la lista de ejercicios y el listener
    public RutinaAdapter(List<Ejercicio> listaRutina, OnRutinaChangeListener listener) {
        this.listaRutina = listaRutina;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RutinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // infla el layout de cada ejercicio dentro de la rutina
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rutina, parent, false);
        // retorna un nuevo view holder
        return new RutinaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull RutinaViewHolder holder, int position) {

        // obtiene el ejercicio correspondiente a la posicion actual
        Ejercicio ejercicio = listaRutina.get(position);

        // se muestra el numero del ejercicio dentro de la rutina
        // se suma 1 porque las posiciones empiezan en 0
        holder.txtNumeroRutina.setText(String.valueOf(position + 1));
        // muestra el nombre del ejercicio
        holder.txtNombreRutina.setText(ejercicio.getNombre());
        // muestra los detalles principales del ejercicio
        holder.txtDetalleRutina.setText(ejercicio.getZona() + " · " + ejercicio.getDuracionMinutos() + " min · " + ejercicio.getRepeticiones() + " rep · " + ejercicio.getNivel());

        // evento para eliminar un ejercicio de la rutina
        holder.EliminarRutina.setOnClickListener(v -> {

            // crea un dialogo de confirmacion antes de eliminar
            AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar ejercicio")
                    .setMessage("¿Estás seguro de que deseas eliminar este ejercicio de tu rutina?")
                    .setPositiveButton("Eliminar", (d, which) -> {

                        // elimina el ejercicio usando el rutina manager
                        RutinaManager.eliminarEjercicio(ejercicio, new RutinaManager.AccionCallback() {
                            @Override
                            public void onExito() {
                                // actualiza la lista visual del recycler view
                                notifyDataSetChanged();

                                // avisa al fragment o activity que la rutina cambio
                                if (listener != null) {
                                    listener.onRutinaCambiada();
                                }

                                // muestra mensaje de confirmacion
                                Toast.makeText(v.getContext(), "Ejercicio eliminado", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String error) {
                                // muestra mensaje si ocurre un error al eliminar
                                Toast.makeText(v.getContext(), "Error al eliminar: " + error, Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .create();

            // cambia el color de los botones del dialogo cuando se muestra
            dialog.setOnShowListener(d -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(v.getContext(), R.color.verde_principal));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(v.getContext(), R.color.verde_principal));
            });

            // muestra el dialogo en pantalla
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        // devuelve la cantidad de ejercicios en la rutina
        return listaRutina.size();
    }

    // clase que almacena las vistas de cada ejercicio en la rutina
    public static class RutinaViewHolder extends RecyclerView.ViewHolder {

        // texto para mostrar el numero del ejercicio
        TextView txtNumeroRutina;
        // texto para mostrar el nombre del ejercicio
        TextView txtNombreRutina;
        // texto para mostrar zona, duracion, repeticiones y nivel
        TextView txtDetalleRutina;
        // icono para eliminar el ejercicio de la rutina
        ImageView EliminarRutina;

        public RutinaViewHolder(@NonNull View itemView) {
            super(itemView);
            // vincula cada variable con su componente visual del layout
            txtNumeroRutina = itemView.findViewById(R.id.txtNumeroRutina);
            txtNombreRutina = itemView.findViewById(R.id.txtNombreRutina);
            txtDetalleRutina = itemView.findViewById(R.id.txtDetalleRutina);
            EliminarRutina = itemView.findViewById(R.id.EliminarRutina);
        }
    }
}