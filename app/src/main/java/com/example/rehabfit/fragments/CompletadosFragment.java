package com.example.rehabfit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.rehabfit.MainActivity;
import com.example.rehabfit.R;
import com.example.rehabfit.utils.RutinaManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CompletadosFragment extends Fragment {

    private TextView txtResumenDuracion;
    private TextView txtResumenCompletados;
    private TextView txtResumenSaltados;
    private TextView txtResumenZona;
    private TextView txtResumenFecha;
    private TextView txtDolorSesionCompletada;
    private TextView btnGuardarHistorial;
    private TextView btnVolverInicioSesion;

    private int minutosTotales;
    private int ejerciciosCompletados;
    private int ejerciciosSaltados;
    private int dolorAntes;
    private int dolorDespues;
    private String zonaPrincipal;

    private boolean historialGuardado = false;

    public CompletadosFragment() {
    }

    public static CompletadosFragment newInstance(
            int minutosTotales,
            int ejerciciosCompletados,
            int ejerciciosSaltados,
            int dolorAntes,
            int dolorDespues,
            String zonaPrincipal
    ) {
        CompletadosFragment fragment = new CompletadosFragment();

        Bundle args = new Bundle();
        args.putInt("minutosTotales", minutosTotales);
        args.putInt("ejerciciosCompletados", ejerciciosCompletados);
        args.putInt("ejerciciosSaltados", ejerciciosSaltados);
        args.putInt("dolorAntes", dolorAntes);
        args.putInt("dolorDespues", dolorDespues);
        args.putString("zonaPrincipal", zonaPrincipal);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_completados, container, false);
        ocultarBottomNavigation();

        txtResumenDuracion = vista.findViewById(R.id.txtResumenDuracion);
        txtResumenCompletados = vista.findViewById(R.id.txtResumenCompletados);
        txtResumenSaltados = vista.findViewById(R.id.txtResumenSaltados);
        txtResumenZona = vista.findViewById(R.id.txtResumenZona);
        txtResumenFecha = vista.findViewById(R.id.txtResumenFecha);
        txtDolorSesionCompletada = vista.findViewById(R.id.txtDolorSesionCompletada);
        btnGuardarHistorial = vista.findViewById(R.id.btnGuardarHistorial);
        btnVolverInicioSesion = vista.findViewById(R.id.btnVolverInicioSesion);

        recibirDatos();
        pintarDatos();
        configurarBotones();
        return vista;
    }

    private void recibirDatos() {
        if (getArguments() == null) {
            return;
        }

        minutosTotales = getArguments().getInt("minutosTotales", 0);
        ejerciciosCompletados = getArguments().getInt("ejerciciosCompletados", 0);
        ejerciciosSaltados = getArguments().getInt("ejerciciosSaltados", 0);
        dolorAntes = getArguments().getInt("dolorAntes", 0);
        dolorDespues = getArguments().getInt("dolorDespues", 0);
        zonaPrincipal = getArguments().getString("zonaPrincipal", "Sin datos");
    }

    private void pintarDatos() {
        txtResumenDuracion.setText("⏱  Duración                                      " + minutosTotales + " minutos");
        txtResumenCompletados.setText("✅  Ejercicios completados                 " + ejerciciosCompletados);
        txtResumenSaltados.setText("⏭  Ejercicios saltados                       " + ejerciciosSaltados);
        txtResumenZona.setText("🦵  Zona trabajada                         " + zonaPrincipal);
        txtResumenFecha.setText("🗓  Fecha                                      " + obtenerFechaActual());

        String textoMejora;

        if (dolorDespues < dolorAntes) {
            textoMejora = " · Mejora";
        } else if (dolorDespues == dolorAntes) {
            textoMejora = " · Igual";
        } else {
            textoMejora = " · Aumentó";
        }

        txtDolorSesionCompletada.setText("Antes de la sesión: " + dolorAntes + "/10\n" + "Después de la sesión: " + dolorDespues + "/10" + textoMejora
        );
    }

    private void configurarBotones() {
        btnGuardarHistorial.setOnClickListener(v -> guardarHistorial());

        btnVolverInicioSesion.setOnClickListener(v -> {
            mostrarBottomNavigation();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).cambiarFragmentBoton(R.id.nav_inicio);
            }
        });
    }

    private void guardarHistorial() {
        if (historialGuardado) {
            abrirHistorial();
            return;
        }

        RutinaManager.guardarSesionTerminada(minutosTotales, ejerciciosCompletados, dolorAntes, dolorDespues, zonaPrincipal,
                new RutinaManager.AccionCallback() {
                    @Override
                    public void onExito() {
                        if (!isAdded()) {
                            return;
                        }

                        historialGuardado = true;
                        Toast.makeText(requireContext(), "Sesión guardada en historial", Toast.LENGTH_SHORT).show();
                        abrirHistorial();
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(requireContext(), "Error al guardar historial: " + error, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void abrirHistorial() {
        mostrarBottomNavigation();

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, new HistorialFragment())
                .addToBackStack(null)
                .commit();
    }

    private String obtenerFechaActual() {
        SimpleDateFormat formato = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
        return formato.format(new Date());
    }

    private void ocultarBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.GONE);
            }
        }
    }

    private void mostrarBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mostrarBottomNavigation();
    }
}