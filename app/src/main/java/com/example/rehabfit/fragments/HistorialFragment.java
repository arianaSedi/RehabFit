package com.example.rehabfit.fragments;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rehabfit.R;
import com.example.rehabfit.adapters.HistorialAdapter;
import com.example.rehabfit.models.SesionRutina;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class HistorialFragment extends Fragment {

    private AppCompatButton btnHoy;
    private AppCompatButton btnSemana;
    private AppCompatButton btnMes;

    private RecyclerView rvHistorial;
    private TextView txtHistorialVacio;

    private HistorialAdapter adapter;

    private final List<SesionRutina> todasLasSesiones = new ArrayList<>();
    private final List<SesionRutina> sesionesFiltradas = new ArrayList<>();

    private String filtroActual = "SEMANA";

    public HistorialFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_historial, container, false);

        btnHoy = vista.findViewById(R.id.btnHoy);
        btnSemana = vista.findViewById(R.id.btnSemana);
        btnMes = vista.findViewById(R.id.btnMes);

        rvHistorial = vista.findViewById(R.id.rvHistorial);
        txtHistorialVacio = vista.findViewById(R.id.txtHistorialVacio);

        rvHistorial.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new HistorialAdapter(sesionesFiltradas);
        rvHistorial.setAdapter(adapter);

        configurarFiltros();
        cargarSesiones();

        return vista;
    }

    private void configurarFiltros() {
        btnHoy.setOnClickListener(v -> {
            filtroActual = "HOY";
            filtrarSesiones();
        });

        btnSemana.setOnClickListener(v -> {
            filtroActual = "SEMANA";
            filtrarSesiones();
        });

        btnMes.setOnClickListener(v -> {
            filtroActual = "MES";
            filtrarSesiones();
        });
    }

    private void cargarSesiones() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Toast.makeText(requireContext(), "No hay usuario activo", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getUid())
                .child("sesiones");

        ref.get()
                .addOnSuccessListener(snapshot -> {
                    todasLasSesiones.clear();

                    for (DataSnapshot item : snapshot.getChildren()) {
                        SesionRutina sesion = item.getValue(SesionRutina.class);

                        if (sesion != null) {
                            todasLasSesiones.add(sesion);
                        }
                    }

                    Collections.sort(todasLasSesiones, (s1, s2) ->
                            Long.compare(s2.getFechaMillis(), s1.getFechaMillis())
                    );

                    filtrarSesiones();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(requireContext(), "Error al cargar historial: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void filtrarSesiones() {
        sesionesFiltradas.clear();

        for (SesionRutina sesion : todasLasSesiones) {
            if (filtroActual.equals("HOY") && esDeHoy(sesion.getFechaMillis())) {
                sesionesFiltradas.add(sesion);
            } else if (filtroActual.equals("SEMANA") && esDeEstaSemana(sesion.getFechaMillis())) {
                sesionesFiltradas.add(sesion);
            } else if (filtroActual.equals("MES") && esDeEsteMes(sesion.getFechaMillis())) {
                sesionesFiltradas.add(sesion);
            }
        }

        adapter.actualizarLista(sesionesFiltradas);

        if (sesionesFiltradas.isEmpty()) {
            txtHistorialVacio.setVisibility(View.VISIBLE);
            rvHistorial.setVisibility(View.GONE);
        } else {
            txtHistorialVacio.setVisibility(View.GONE);
            rvHistorial.setVisibility(View.VISIBLE);
        }

        actualizarBotones();
    }

    private void actualizarBotones() {
        btnHoy.setBackgroundTintList(getResources().getColorStateList(
                filtroActual.equals("HOY") ? R.color.verde_principal : R.color.gris_chip
        ));

        btnSemana.setBackgroundTintList(getResources().getColorStateList(
                filtroActual.equals("SEMANA") ? R.color.verde_principal : R.color.gris_chip
        ));

        btnMes.setBackgroundTintList(getResources().getColorStateList(
                filtroActual.equals("MES") ? R.color.verde_principal : R.color.gris_chip
        ));

        btnHoy.setTextColor(getResources().getColor(
                filtroActual.equals("HOY") ? R.color.blanco : R.color.texto_principal
        ));

        btnSemana.setTextColor(getResources().getColor(
                filtroActual.equals("SEMANA") ? R.color.blanco : R.color.texto_principal
        ));

        btnMes.setTextColor(getResources().getColor(
                filtroActual.equals("MES") ? R.color.blanco : R.color.texto_principal
        ));
    }

    private boolean esDeHoy(long fechaMillis) {
        Calendar fecha = Calendar.getInstance();
        fecha.setTimeInMillis(fechaMillis);

        Calendar hoy = Calendar.getInstance();

        return fecha.get(Calendar.YEAR) == hoy.get(Calendar.YEAR)
                && fecha.get(Calendar.DAY_OF_YEAR) == hoy.get(Calendar.DAY_OF_YEAR);
    }

    private boolean esDeEstaSemana(long fechaMillis) {
        Calendar fecha = Calendar.getInstance();
        fecha.setTimeInMillis(fechaMillis);

        Calendar inicioSemana = Calendar.getInstance();

        int diaActual = inicioSemana.get(Calendar.DAY_OF_WEEK);
        int diferencia = (diaActual == Calendar.SUNDAY)
                ? -6
                : Calendar.MONDAY - diaActual;

        inicioSemana.add(Calendar.DAY_OF_MONTH, diferencia);
        inicioSemana.set(Calendar.HOUR_OF_DAY, 0);
        inicioSemana.set(Calendar.MINUTE, 0);
        inicioSemana.set(Calendar.SECOND, 0);
        inicioSemana.set(Calendar.MILLISECOND, 0);

        Calendar finSemana = (Calendar) inicioSemana.clone();
        finSemana.add(Calendar.DAY_OF_MONTH, 7);

        return fecha.getTimeInMillis() >= inicioSemana.getTimeInMillis()
                && fecha.getTimeInMillis() < finSemana.getTimeInMillis();
    }

    private boolean esDeEsteMes(long fechaMillis) {
        Calendar fecha = Calendar.getInstance();
        fecha.setTimeInMillis(fechaMillis);

        Calendar hoy = Calendar.getInstance();

        return fecha.get(Calendar.YEAR) == hoy.get(Calendar.YEAR)
                && fecha.get(Calendar.MONTH) == hoy.get(Calendar.MONTH);
    }
}