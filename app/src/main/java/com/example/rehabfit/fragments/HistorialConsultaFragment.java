package com.example.rehabfit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.R;
import com.example.rehabfit.adapters.HistorialConsultaAdapter;
import com.example.rehabfit.models.ConsultasIA;
import com.example.rehabfit.models.Ejercicio;
import com.example.rehabfit.utils.RutinaManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HistorialConsultaFragment extends Fragment {

    private RecyclerView rvHistorialConsultasIA;
    private AppCompatButton btnNuevaConsultaIA;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private ImageButton btnVolverHistorialIA;
    private final List<ConsultasIA> listaConsultas = new ArrayList<>();
    private HistorialConsultaAdapter adapter;

    public HistorialConsultaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // infla el layout del fragment de historial de consultas
        View vista = inflater.inflate(R.layout.fragment_historial_consulta, container, false);

        // inicializa firebase auth y firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // vincula los componentes visuales del layout
        rvHistorialConsultasIA = vista.findViewById(R.id.rvHistorialConsultasIA);
        btnNuevaConsultaIA = vista.findViewById(R.id.btnNuevaConsultaIA);
        btnVolverHistorialIA = vista.findViewById(R.id.btnVolverHistorialIA);

        // configura el recycler view en forma de lista vertical
        rvHistorialConsultasIA.setLayoutManager(new LinearLayoutManager(requireContext()));

        // crea el adaptador del historial con las acciones de ver detalle y agregar a rutina
        adapter = new HistorialConsultaAdapter(listaConsultas, consulta -> mostrarDetalleConsulta(consulta), consulta -> agregarConsultaARutina(consulta));

        // asigna el adaptador al recycler view
        rvHistorialConsultasIA.setAdapter(adapter);

        // abre la pantalla para hacer una nueva consulta
        btnNuevaConsultaIA.setOnClickListener(v -> abrirNuevaConsulta());

        // vuelve al fragment anterior
        btnVolverHistorialIA.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // carga el historial de consultas desde firestore
        cargarHistorial();

        // devuelve la vista del fragment
        return vista;
    }

    // metodo encargado de cargar el historial de consultas del usuario
    private void cargarHistorial() {

        // obtiene el usuario autenticado
        FirebaseUser usuario = auth.getCurrentUser();

        // valida que exista una sesion iniciada
        if (usuario == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // consulta en firestore las consultas ia del usuario ordenadas por fecha
        firestore.collection("users")
                .document(usuario.getUid())
                .collection("consultasIA")
                .orderBy("fechaMillis", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {

                    // valida que el fragment siga activo
                    if (!isAdded()) {
                        return;
                    }

                    // limpia la lista antes de cargar nuevos datos
                    listaConsultas.clear();

                    // recorre los documentos encontrados en firestore
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {

                        // convierte cada documento en un objeto consultas ia
                        ConsultasIA consulta = doc.toObject(ConsultasIA.class);

                        // valida que la consulta no sea nula
                        if (consulta != null) {

                            // agrega la consulta a la lista
                            listaConsultas.add(consulta);
                        }
                    }

                    // actualiza el recycler view
                    adapter.notifyDataSetChanged();

                    // muestra mensaje si no hay consultas guardadas
                    if (listaConsultas.isEmpty()) {
                        Toast.makeText(requireContext(), "Aún no tienes consultas guardadas", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {

                    // valida que el fragment siga activo
                    if (!isAdded()) {
                        return;
                    }

                    // muestra mensaje si ocurre un error al cargar el historial
                    Toast.makeText(requireContext(), "Error al cargar historial: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // metodo para agregar a la rutina los ejercicios recomendados de una consulta
    private void agregarConsultaARutina(ConsultasIA consulta) {

        // valida que la consulta tenga ejercicios recomendados
        if (consulta.getEjerciciosRecomendados() == null || consulta.getEjerciciosRecomendados().isEmpty()) {

            Toast.makeText(requireContext(), "Esta consulta no tiene ejercicios recomendados", Toast.LENGTH_SHORT).show();
            return;
        }

        // guarda la cantidad total de ejercicios a agregar
        final int total = consulta.getEjerciciosRecomendados().size();

        // contador de ejercicios agregados correctamente
        final int[] agregados = {0};

        // bandera para saber si ocurrio algun error
        final boolean[] huboError = {false};

        // recorre cada ejercicio recomendado
        for (Ejercicio ejercicio : consulta.getEjerciciosRecomendados()) {

            // agrega el ejercicio a la rutina usando el rutina manager
            RutinaManager.agregarEjercicio(ejercicio, new RutinaManager.AccionCallback() {
                @Override
                public void onExito() {

                    // aumenta el contador de ejercicios agregados
                    agregados[0]++;

                    // valida que el fragment siga activo
                    if (!isAdded()) {
                        return;
                    }

                    // muestra mensaje cuando todos los ejercicios se agregaron sin error
                    if (agregados[0] == total && !huboError[0]) {
                        Toast.makeText(requireContext(), "Ejercicios agregados a tu rutina", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {

                    // marca que ocurrio un error
                    huboError[0] = true;

                    // valida que el fragment siga activo
                    if (!isAdded()) {
                        return;
                    }

                    // muestra mensaje con el error ocurrido
                    Toast.makeText(requireContext(), "Error al agregar: " + error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // metodo para mostrar el detalle de una consulta seleccionada
    private void mostrarDetalleConsulta(ConsultasIA consulta) {

        // crea el fragment de detalle enviando la consulta seleccionada
        DetalleConsultasFragment fragment = DetalleConsultasFragment.newInstance(consulta);

        // reemplaza el fragment actual por el detalle de la consulta
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, fragment)
                .addToBackStack(null)
                .commit();
    }

    // metodo para abrir una nueva consulta ia
    private void abrirNuevaConsulta() {

        // reemplaza el fragment actual por el fragment de consultas ia
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, new ConsultasIAFragment())
                .addToBackStack(null)
                .commit();
    }
}