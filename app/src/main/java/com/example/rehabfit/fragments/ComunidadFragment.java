package com.example.rehabfit.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.rehabfit.DetallePublicacionActivity;
import com.example.rehabfit.PublicarComunidadActivity;
import com.example.rehabfit.R;
import com.example.rehabfit.adapters.PublicacionAdapter;
import com.example.rehabfit.models.PublicacionComunidad;
import com.google.firebase.database.*;

        import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// fragment encargado de mostrar la seccion de comunidad
public class ComunidadFragment extends Fragment {

    // recycler view donde se muestran las publicaciones
    private RecyclerView rvPublicaciones;

    // boton para crear una nueva publicacion
    private Button btnPublicar;

    // adaptador encargado de mostrar las publicaciones
    private PublicacionAdapter adapter;

    // lista que almacena las publicaciones obtenidas de firebase
    private List<PublicacionComunidad> listaPublicaciones;

    // referencia al nodo de publicaciones en firebase
    private DatabaseReference refPublicaciones;

    // constructor vacio requerido por fragment
    public ComunidadFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // infla el layout del fragment
        View vista = inflater.inflate(R.layout.fragment_comunidad, container, false);

        // vincula los componentes visuales del layout
        rvPublicaciones = vista.findViewById(R.id.rvPublicaciones);
        btnPublicar = vista.findViewById(R.id.btnPublicar);

        // inicializa la lista de publicaciones
        listaPublicaciones = new ArrayList<>();

        // crea el adaptador y define la accion al tocar una publicacion
        adapter = new PublicacionAdapter(listaPublicaciones, publicacion -> {
            // abre la pantalla de detalle de la publicacion seleccionada
            Intent intent = new Intent(requireContext(), DetallePublicacionActivity.class);
            // envia el id de la publicacion a la siguiente pantalla
            intent.putExtra("idPublicacion", publicacion.getId());
            // inicia la actividad de detalle
            startActivity(intent);
        });

        // configura el recycler view con un layout vertical
        rvPublicaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        // asigna el adaptador al recycler view
        rvPublicaciones.setAdapter(adapter);
        // obtiene la referencia al nodo publicaciones comunidad en firebase
        refPublicaciones = FirebaseDatabase.getInstance().getReference("publicacionesComunidad");

        // evento para abrir la pantalla de publicar
        btnPublicar.setOnClickListener(v -> {
            // crea el intent hacia la actividad de publicar
            Intent intent = new Intent(requireContext(), PublicarComunidadActivity.class);
            // inicia la pantalla para crear publicacion
            startActivity(intent);
        });

        // carga las publicaciones desde firebase
        cargarPublicaciones();
        // retorna la vista del fragment
        return vista;
    }

    // metodo encargado de cargar las publicaciones desde firebase
    private void cargarPublicaciones() {

        // ordena las publicaciones por timestamp
        refPublicaciones.orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // limpia la lista antes de agregar los datos actualizados
                        listaPublicaciones.clear();

                        // recorre cada publicacion encontrada en firebase
                        for (DataSnapshot data : snapshot.getChildren()) {

                            // convierte los datos en un objeto publicacion comunidad
                            PublicacionComunidad publicacion = data.getValue(PublicacionComunidad.class);

                            // valida que la publicacion no sea nula
                            if (publicacion != null) {

                                // agrega la publicacion a la lista
                                listaPublicaciones.add(publicacion);
                            }
                        }

                        // invierte la lista para mostrar primero las publicaciones mas recientes
                        Collections.reverse(listaPublicaciones);
                        // actualiza el recycler view
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}