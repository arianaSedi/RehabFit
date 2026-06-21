package com.example.rehabfit.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rehabfit.R;
import com.example.rehabfit.fragments.DetalleEjerciciosFragment;
import com.example.rehabfit.models.Ejercicio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EjercicioAdapter extends RecyclerView.Adapter<EjercicioAdapter.EjercicioViewHolder> {

    private List<Ejercicio> listaEjercicios;
    private DatabaseReference favoritosRef;
    private ValueEventListener favoritosListener;
    private final Set<String> idsFavoritos = new HashSet<>();

    public EjercicioAdapter(List<Ejercicio> listaEjercicios) {
        this.listaEjercicios = listaEjercicios;
        configurarFavoritosFirebase();
    }

    //metodo que conecta con firebase para obtener los ejercicios favoritos
    private void configurarFavoritosFirebase() {

        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioActual == null) {
            return;
        }

        favoritosRef = FirebaseDatabase.getInstance().getReference("usuarios").child(usuarioActual.getUid()).child("favoritos");
        favoritosListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                idsFavoritos.clear();

                for (DataSnapshot item : snapshot.getChildren()) {

                    //se guarda el id de cada favorito
                    idsFavoritos.add(item.getKey());
                }

                //actualiza el recyclerview cuando hay cambios
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
            }
        };

        favoritosRef.addValueEventListener(favoritosListener);
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ejercicio, parent, false);
        return new EjercicioViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {

        Ejercicio ejercicio = listaEjercicios.get(position);
        String idEjercicio = obtenerIdEjercicio(ejercicio);

        //se verifica si el ejercicio esta guardado como favorito
        boolean favorito = idsFavoritos.contains(idEjercicio);

        //se muestran los datos del ejercicio
        holder.txtNombreEjercicio.setText(ejercicio.getNombre());
        holder.txtDatosEjercicio.setText(ejercicio.getZona() + "   " + ejercicio.getNivel() + "   " + ejercicio.getPosicion());
        holder.txtDuracionEjercicio.setText("⏱ " + ejercicio.getDuracionMinutos() + " min · " + ejercicio.getRepeticiones() + " rep");

        //coloca un icono segun la zona corporal
        holder.txtIconoEjercicio.setText(obtenerIconoEjercicio(ejercicio));

        //actualiza la apariencia de la estrella
        Estrella(holder, favorito);

        holder.itemView.setOnClickListener(v -> {

            DetalleEjerciciosFragment detalleFragment = DetalleEjerciciosFragment.newInstance(ejercicio);
            FragmentActivity activity = (FragmentActivity) v.getContext();

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, detalleFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // agrega o elimina favoritos
        holder.txtGuardar.setOnClickListener(v -> {

            if (favoritosRef == null) {
                Toast.makeText(v.getContext(), "Debes iniciar sesion para guardar favoritos", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean actualmenteFavorito = idsFavoritos.contains(idEjercicio);

            // si ya es favorito lo elimina
            if (actualmenteFavorito) {

                favoritosRef.child(idEjercicio).removeValue().addOnSuccessListener(unused -> {

                    idsFavoritos.remove(idEjercicio);
                    Estrella(holder, false);
                    Toast.makeText(v.getContext(), "Quitado de favoritos", Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e ->
                        Toast.makeText(v.getContext(), "Error al quitar favorito: " + e.getMessage(), Toast.LENGTH_LONG).show());

            } else {

                // si no es favorito lo guarda en firebase
                favoritosRef.child(idEjercicio).setValue(ejercicio).addOnSuccessListener(unused -> {

                    idsFavoritos.add(idEjercicio);
                    Estrella(holder, true);

                    Toast.makeText(v.getContext(), "Guardado en favoritos", Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e ->
                        Toast.makeText(v.getContext(), "Error al guardar favorito: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    //se genera un id unico para cada ejercicio
    //eesto ayuda a guardar favoritos sin repetir datos
    private String obtenerIdEjercicio(Ejercicio ejercicio) {

        if (ejercicio.getId() != 0) {
            return String.valueOf(ejercicio.getId());
        }

        String nombre = ejercicio.getNombre() != null ? ejercicio.getNombre() : "sin_nombre";
        String zona = ejercicio.getZona() != null ? ejercicio.getZona() : "sin_zona";
        return limpiarTextoParaFirebase(nombre + "_" + zona);
    }

    //firebase no permite algunos caracteres en las claves
    //por eso se reemplazan por guiones bajos
    private String limpiarTextoParaFirebase(String texto) {

        return texto.replace(".", "_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_")
                .replace("/", "_");
    }

    //devuelve un icono dependiendo de la zona corporal
    private String obtenerIconoEjercicio(Ejercicio ejercicio) {

        String zona = ejercicio.getZona() != null ? ejercicio.getZona().toLowerCase() : "";
        String posicion =ejercicio.getPosicion() != null ? ejercicio.getPosicion().toLowerCase() : "";
        String nombre = ejercicio.getNombre() != null ? ejercicio.getNombre().toLowerCase() : "";

        if (zona.contains("rodilla") || nombre.contains("rodilla")) {
            return "🦵";
        }

        if (zona.contains("tobillo") || nombre.contains("tobillo")) {
            return "🦶";
        }

        if (zona.contains("hombro") || nombre.contains("hombro")) {
            return "💪";
        }

        if (zona.contains("espalda") || nombre.contains("espalda")) {
            return "🧍";
        }

        if (posicion.contains("sentado") || nombre.contains("sentado")) {
            return "🪑";
        }

        return "🏃";
    }

    //cambia la estrella de FAV dependiendo si esta guardado o no
    private void Estrella(EjercicioViewHolder holder, boolean favorito) {

        if (favorito) {

            holder.txtGuardar.setText("★");
            holder.txtGuardar.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.amarillo_estrella));
            holder.txtGuardar.setTypeface(null, Typeface.BOLD);

        } else {
            holder.txtGuardar.setText("☆");
            holder.txtGuardar.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.borde));
            holder.txtGuardar.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public int getItemCount() {
        return listaEjercicios.size();
    }

    //actualiza la lista cuando se aplican filtros o busquedas
    public void actualizarLista(List<Ejercicio> nuevaLista) {

        this.listaEjercicios = nuevaLista;
        notifyDataSetChanged();
    }

    //elimina el listener cuando ya no se necesita esto evita consumo innecesario de recursos
    public void liberarListenerFavoritos() {

        if (favoritosRef != null && favoritosListener != null) {
            favoritosRef.removeEventListener(favoritosListener);
        }
    }

    public static class EjercicioViewHolder extends RecyclerView.ViewHolder {
        TextView txtIconoEjercicio;
        TextView txtNombreEjercicio;
        TextView txtDatosEjercicio;
        TextView txtDuracionEjercicio;
        TextView txtGuardar;

        public EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);

            txtIconoEjercicio = itemView.findViewById(R.id.txtIconoEjercicio);
            txtNombreEjercicio = itemView.findViewById(R.id.txtNombreEjercicio);
            txtDatosEjercicio = itemView.findViewById(R.id.txtDatosEjercicio);
            txtDuracionEjercicio = itemView.findViewById(R.id.txtDuracionEjercicio);
            txtGuardar = itemView.findViewById(R.id.txtGuardar);
        }
    }
}