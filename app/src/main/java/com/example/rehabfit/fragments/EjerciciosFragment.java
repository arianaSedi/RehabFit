package com.example.rehabfit.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rehabfit.adapters.FiltroAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rehabfit.R;
import com.example.rehabfit.adapters.EjercicioAdapter;
import com.example.rehabfit.models.Ejercicio;
import com.example.rehabfit.models.EjercicioResponse;
import com.example.rehabfit.network.ApiService;
import com.example.rehabfit.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EjerciciosFragment extends Fragment {

    private RecyclerView rvEjercicios;
    private EditText edtBuscarEjercicio;

    private RecyclerView rvFiltros;
    private FiltroAdapter filtroAdapter;
    private ArrayList<String> filtros;

    private EjercicioAdapter adapter;

    private final List<Ejercicio> listaCompleta = new ArrayList<>();
    private final List<Ejercicio> listaFiltrada = new ArrayList<>();

    private Call<EjercicioResponse> callEjercicios;

    private String filtroSeleccionado = "Todos";

    public EjerciciosFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_ejercicios, container, false);

        rvEjercicios = vista.findViewById(R.id.rvEjercicios);
        edtBuscarEjercicio = vista.findViewById(R.id.edtBuscarEjercicio);
        rvFiltros = vista.findViewById(R.id.rvFiltros);

        rvFiltros.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        filtros = new ArrayList<>();
        filtros.add("Todos");
        filtros.add("Brazo");
        filtros.add("Hombro");
        filtros.add("Muñeca");
        filtros.add("Mano");
        filtros.add("Espalda");
        filtros.add("Rodilla");
        filtros.add("Pierna");
        filtros.add("Tobillo");
        filtros.add("Sentado");

        filtroAdapter = new FiltroAdapter(filtros, filtro -> {
            if (filtro.equalsIgnoreCase("Todos")) {
                mostrarTodos();
            } else {
                filtrarPorCategoria(filtro);
            }
        });

        rvFiltros.setAdapter(filtroAdapter);
        rvEjercicios.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new EjercicioAdapter(listaFiltrada);
        rvEjercicios.setAdapter(adapter);

        configurarBuscador();
        actualizarFiltro();
        cargarEjerciciosAPI();
        return vista;
    }

    private void cargarEjerciciosAPI() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        callEjercicios = apiService.obtenerEjercicios();

        callEjercicios.enqueue(new Callback<EjercicioResponse>() {
            @Override
            public void onResponse(Call<EjercicioResponse> call, Response<EjercicioResponse> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    listaCompleta.clear();
                    listaFiltrada.clear();

                    listaCompleta.addAll(response.body().getEjercicios());
                    listaFiltrada.addAll(listaCompleta);

                    adapter.notifyDataSetChanged();
                    aplicarZonaGuardada();

                } else {
                    Toast.makeText(requireContext(), "No se pudieron cargar los ejercicios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EjercicioResponse> call, Throwable t) {
                if (!isAdded() || call.isCanceled()) {
                    return;
                }

                Toast.makeText(requireContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void configurarBuscador() {
        edtBuscarEjercicio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarPorTexto(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void mostrarTodos() {
        filtroSeleccionado = "Todos";
        actualizarFiltro();

        edtBuscarEjercicio.setText("");

        listaFiltrada.clear();
        listaFiltrada.addAll(listaCompleta);
        adapter.notifyDataSetChanged();
    }

    private void filtrarPorCategoria(String categoria) {
        filtroSeleccionado = categoria;
        actualizarFiltro();

        edtBuscarEjercicio.setText("");

        listaFiltrada.clear();

        for (Ejercicio ejercicio : listaCompleta) {
            boolean coincideZona = ejercicio.getZona() != null &&
                    ejercicio.getZona().equalsIgnoreCase(categoria);

            boolean coincidePosicion = ejercicio.getPosicion() != null &&
                    ejercicio.getPosicion().equalsIgnoreCase(categoria);

            if (coincideZona || coincidePosicion) {
                listaFiltrada.add(ejercicio);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void filtrarPorTexto(String texto) {
        String busqueda = texto.toLowerCase().trim();

        if (!busqueda.isEmpty()) {
            actualizarFiltro();
        }

        listaFiltrada.clear();

        if (busqueda.isEmpty()) {
            listaFiltrada.addAll(listaCompleta);
        } else {
            for (Ejercicio ejercicio : listaCompleta) {
                String nombre = ejercicio.getNombre() != null ? ejercicio.getNombre().toLowerCase() : "";
                String zona = ejercicio.getZona() != null ? ejercicio.getZona().toLowerCase() : "";
                String nivel = ejercicio.getNivel() != null ? ejercicio.getNivel().toLowerCase() : "";
                String posicion = ejercicio.getPosicion() != null ? ejercicio.getPosicion().toLowerCase() : "";
                String descripcion = ejercicio.getDescripcion() != null ? ejercicio.getDescripcion().toLowerCase() : "";

                if (nombre.contains(busqueda) || zona.contains(busqueda) || nivel.contains(busqueda) || posicion.contains(busqueda) || descripcion.contains(busqueda)) {
                    listaFiltrada.add(ejercicio);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void actualizarFiltro() {

        if (filtroAdapter != null) {
            filtroAdapter.setFiltroSeleccionado(
                    filtroSeleccionado);
        }
    }
    private void aplicarZonaGuardada() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("perfilAdaptado")
                .child("zonaAfectada")
                .get()
                .addOnSuccessListener(snapshot -> {

                    String zona = snapshot.getValue(String.class);

                    if (zona == null || zona.isEmpty()) {
                        return;
                    }

                    filtrarPorCategoria(zona);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (callEjercicios != null) {
            callEjercicios.cancel();
        }

        if (adapter != null) {
            adapter.liberarListenerFavoritos();
        }
    }
}