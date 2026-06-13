package com.example.rehabfit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.example.rehabfit.R;
import com.example.rehabfit.models.Ejercicio;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DetalleEjerciciosFragment extends Fragment {

    private TextView btnVolverDetalle;
    private TextView txtIconoDetalle;
    private TextView txtNombreDetalle;
    private TextView txtZonaDetalle;
    private TextView txtDificultadDetalle;
    private TextView txtPosicionDetalle;
    private TextView txtDuracionDetalle;
    private TextView txtRepeticionesDetalle;
    private TextView txtDescripcionDetalle;
    private TextView txtInstruccionesDetalle;
    private TextView txtPrecaucionDetalle;

    public DetalleEjerciciosFragment() {

    }

    public static DetalleEjerciciosFragment newInstance(Ejercicio ejercicio) {
        DetalleEjerciciosFragment fragment = new DetalleEjerciciosFragment();

        Bundle args = new Bundle();
        args.putSerializable("ejercicio", ejercicio);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_detalle_ejercicios, container, false);

        ocultarBottomNavigation();

        btnVolverDetalle = vista.findViewById(R.id.btnVolverDetalle);
        txtIconoDetalle = vista.findViewById(R.id.txtIconoDetalle);
        txtNombreDetalle = vista.findViewById(R.id.txtNombreDetalle);
        txtZonaDetalle = vista.findViewById(R.id.txtZonaDetalle);
        txtDificultadDetalle = vista.findViewById(R.id.txtDificultadDetalle);
        txtPosicionDetalle = vista.findViewById(R.id.txtPosicionDetalle);
        txtDuracionDetalle = vista.findViewById(R.id.txtDuracionDetalle);
        txtRepeticionesDetalle = vista.findViewById(R.id.txtRepeticionesDetalle);
        txtDescripcionDetalle = vista.findViewById(R.id.txtDescripcionDetalle);
        txtInstruccionesDetalle = vista.findViewById(R.id.txtInstruccionesDetalle);
        txtPrecaucionDetalle = vista.findViewById(R.id.txtPrecaucionDetalle);

        cargarDatosDelEjercicio();

        btnVolverDetalle.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return vista;
    }

    private void cargarDatosDelEjercicio() {
        if (getArguments() == null) {
            return;
        }

        Ejercicio ejercicio = (Ejercicio) getArguments().getSerializable("ejercicio");

        if (ejercicio == null) {
            return;
        }

        txtNombreDetalle.setText(valorSeguro(ejercicio.getNombre(), "Ejercicio"));
        txtZonaDetalle.setText("Zona\n" + valorSeguro(ejercicio.getZona(), "No especificado"));
        txtDificultadDetalle.setText("Nivel\n" + valorSeguro(ejercicio.getNivel(), "No especificado"));
        txtPosicionDetalle.setText("Posición\n" + valorSeguro(ejercicio.getPosicion(), "No especificado"));
        txtDuracionDetalle.setText("Duración\n" + ejercicio.getDuracionMinutos() + " min");
        txtRepeticionesDetalle.setText("Repeticiones\n" + ejercicio.getRepeticiones() + " rep");

        txtDescripcionDetalle.setText(valorSeguro(ejercicio.getDescripcion(), "Sin descripción disponible"));
        txtInstruccionesDetalle.setText(crearInstrucciones(ejercicio));
        txtPrecaucionDetalle.setText("⚠ Advertencia\n" + valorSeguro(ejercicio.getAdvertencia(), "Realizar con cuidado."));

        cambiarIconoSegunZona(ejercicio.getZona());
    }

    private String valorSeguro(String texto, String valorPorDefecto) {
        if (texto == null || texto.trim().isEmpty()) {
            return valorPorDefecto;
        }
        return texto;
    }

    private String crearInstrucciones(Ejercicio ejercicio) {
        String nombre = ejercicio.getNombre() != null ? ejercicio.getNombre().toLowerCase() : "";
        String zona = ejercicio.getZona() != null ? ejercicio.getZona().toLowerCase() : "";
        String posicion = ejercicio.getPosicion() != null ? ejercicio.getPosicion().toLowerCase() : "";

        if (posicion.contains("sentado")) {
            return "1. Siéntate en una silla estable.\n\n" +
                    "2. Mantén la espalda recta y los pies apoyados.\n\n" +
                    "3. Realiza el movimiento de forma lenta y controlada.\n\n" +
                    "4. Evita forzar la zona trabajada.\n\n" +
                    "5. Repite según las indicaciones del ejercicio.";
        }

        if (zona.contains("rodilla") || nombre.contains("rodilla")) {
            return "1. Colócate en una posición cómoda y estable.\n\n" +
                    "2. Realiza el movimiento de rodilla lentamente.\n\n" +
                    "3. Mantén el movimiento suave y controlado.\n\n" +
                    "4. Regresa poco a poco a la posición inicial.\n\n" +
                    "5. Detente si sientes dolor intenso.";
        }

        if (zona.contains("tobillo") || nombre.contains("tobillo")) {
            return "1. Apoya el pie de forma segura.\n\n" +
                    "2. Realiza movimientos suaves con el tobillo.\n\n" +
                    "3. Evita movimientos bruscos.\n\n" +
                    "4. Regresa lentamente a la posición inicial.\n\n" +
                    "5. Repite con cuidado.";
        }

        if (zona.contains("hombro") || nombre.contains("hombro")) {
            return "1. Mantén la espalda recta.\n\n" +
                    "2. Relaja los hombros antes de iniciar.\n\n" +
                    "3. Mueve el brazo lentamente.\n\n" +
                    "4. No fuerces más allá de tu capacidad.\n\n" +
                    "5. Regresa lentamente a la posición inicial.";
        }

        if (zona.contains("espalda") || nombre.contains("espalda")) {
            return "1. Mantén una postura cómoda y segura.\n\n" +
                    "2. Realiza el movimiento lentamente.\n\n" +
                    "3. Evita arquear demasiado la espalda.\n\n" +
                    "4. Respira de forma controlada.\n\n" +
                    "5. Detente si sientes dolor.";
        }

        return "1. Colócate en una posición cómoda.\n\n" +
                "2. Realiza el movimiento lentamente.\n\n" +
                "3. No fuerces tu cuerpo.\n\n" +
                "4. Descansa si sientes molestia.\n\n" +
                "5. Repite según las indicaciones.";
    }

    private void cambiarIconoSegunZona(String zona) {
        if (zona == null) {
            txtIconoDetalle.setText("🏃");
            return;
        }

        String zonaMinuscula = zona.toLowerCase();

        if (zonaMinuscula.contains("rodilla")) {
            txtIconoDetalle.setText("🦵");
        } else if (zonaMinuscula.contains("tobillo")) {
            txtIconoDetalle.setText("🦶");
        } else if (zonaMinuscula.contains("hombro")) {
            txtIconoDetalle.setText("💪");
        } else if (zonaMinuscula.contains("espalda")) {
            txtIconoDetalle.setText("🧍");
        } else {
            txtIconoDetalle.setText("🏃");
        }
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