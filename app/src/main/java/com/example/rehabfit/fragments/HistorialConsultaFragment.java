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

        View vista = inflater.inflate(R.layout.fragment_historial_consulta, container, false);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        rvHistorialConsultasIA = vista.findViewById(R.id.rvHistorialConsultasIA);
        btnNuevaConsultaIA = vista.findViewById(R.id.btnNuevaConsultaIA);
        btnVolverHistorialIA = vista.findViewById(R.id.btnVolverHistorialIA);
        rvHistorialConsultasIA.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new HistorialConsultaAdapter(listaConsultas, consulta -> mostrarDetalleConsulta(consulta), consulta -> agregarConsultaARutina(consulta));

        rvHistorialConsultasIA.setAdapter(adapter);

        btnNuevaConsultaIA.setOnClickListener(v -> abrirNuevaConsulta());

        btnVolverHistorialIA.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
        cargarHistorial();

        return vista;
    }

    private void cargarHistorial() {
        FirebaseUser usuario = auth.getCurrentUser();

        if (usuario == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users")
                .document(usuario.getUid())
                .collection("consultasIA")
                .orderBy("fechaMillis", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }

                    listaConsultas.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        ConsultasIA consulta = doc.toObject(ConsultasIA.class);

                        if (consulta != null) {
                            listaConsultas.add(consulta);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (listaConsultas.isEmpty()) {
                        Toast.makeText(requireContext(), "Aún no tienes consultas guardadas", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(requireContext(), "Error al cargar historial: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void agregarConsultaARutina(ConsultasIA consulta) {
        if (consulta.getEjerciciosRecomendados() == null || consulta.getEjerciciosRecomendados().isEmpty()) {

            Toast.makeText(requireContext(), "Esta consulta no tiene ejercicios recomendados", Toast.LENGTH_SHORT).show();
            return;
        }

        final int total = consulta.getEjerciciosRecomendados().size();
        final int[] agregados = {0};
        final boolean[] huboError = {false};

        for (Ejercicio ejercicio : consulta.getEjerciciosRecomendados()) {
            RutinaManager.agregarEjercicio(ejercicio, new RutinaManager.AccionCallback() {
                @Override
                public void onExito() {
                    agregados[0]++;

                    if (!isAdded()) {
                        return;
                    }

                    if (agregados[0] == total && !huboError[0]) {
                        Toast.makeText(requireContext(), "Ejercicios agregados a tu rutina", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    huboError[0] = true;

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(requireContext(), "Error al agregar: " + error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void mostrarDetalleConsulta(ConsultasIA consulta) {
        DetalleConsultasFragment fragment = DetalleConsultasFragment.newInstance(consulta);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void abrirNuevaConsulta() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, new ConsultasIAFragment())
                .addToBackStack(null)
                .commit();
    }
}