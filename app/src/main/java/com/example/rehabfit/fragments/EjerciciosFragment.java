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

    private TextView btnTodos;
    private TextView btnRodilla;
    private TextView btnTobillo;
    private TextView btnHombro;
    private TextView btnEspalda;
    private TextView btnSentado;

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
        btnTodos = vista.findViewById(R.id.btnTodos);
        btnRodilla = vista.findViewById(R.id.btnRodilla);
        btnTobillo = vista.findViewById(R.id.btnTobillo);
        btnHombro = vista.findViewById(R.id.btnHombro);
        btnEspalda = vista.findViewById(R.id.btnEspalda);
        btnSentado = vista.findViewById(R.id.btnSentado);

        rvEjercicios.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new EjercicioAdapter(listaFiltrada);
        rvEjercicios.setAdapter(adapter);

        configurarFiltros();
        configurarBuscador();
        actualizarChips();
        cargarEjerciciosDesdeApi();
        return vista;
    }

    private void cargarEjerciciosDesdeApi() {
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

                    Toast.makeText(requireContext(), "Ejercicios cargados: " + listaCompleta.size(), Toast.LENGTH_SHORT).show();

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

    private void configurarFiltros() {
        btnTodos.setOnClickListener(v -> mostrarTodos());
        btnRodilla.setOnClickListener(v -> filtrarPorCategoria("Rodilla"));
        btnTobillo.setOnClickListener(v -> filtrarPorCategoria("Tobillo"));
        btnHombro.setOnClickListener(v -> filtrarPorCategoria("Hombro"));
        btnEspalda.setOnClickListener(v -> filtrarPorCategoria("Espalda"));
        btnSentado.setOnClickListener(v -> filtrarPorCategoria("Sentado"));
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
        actualizarChips();

        edtBuscarEjercicio.setText("");

        listaFiltrada.clear();
        listaFiltrada.addAll(listaCompleta);
        adapter.notifyDataSetChanged();
    }

    private void filtrarPorCategoria(String categoria) {
        filtroSeleccionado = categoria;
        actualizarChips();

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
            filtroSeleccionado = "Todos";
            actualizarChips();
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

                if (nombre.contains(busqueda)
                        || zona.contains(busqueda)
                        || nivel.contains(busqueda)
                        || posicion.contains(busqueda)
                        || descripcion.contains(busqueda)) {

                    listaFiltrada.add(ejercicio);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void actualizarChips() {
        pintarChip(btnTodos, filtroSeleccionado.equalsIgnoreCase("Todos"));
        pintarChip(btnRodilla, filtroSeleccionado.equalsIgnoreCase("Rodilla"));
        pintarChip(btnTobillo, filtroSeleccionado.equalsIgnoreCase("Tobillo"));
        pintarChip(btnHombro, filtroSeleccionado.equalsIgnoreCase("Hombro"));
        pintarChip(btnEspalda, filtroSeleccionado.equalsIgnoreCase("Espalda"));
        pintarChip(btnSentado, filtroSeleccionado.equalsIgnoreCase("Sentado"));
    }

    private void pintarChip(TextView chip, boolean seleccionado) {
        if (seleccionado) {
            chip.setBackgroundResource(R.drawable.bg_chip_verde);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.verde_oscuro));
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_gris);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_principal));
            chip.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
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