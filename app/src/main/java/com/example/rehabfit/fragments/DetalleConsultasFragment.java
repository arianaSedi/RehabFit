package com.example.rehabfit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.R;
import com.example.rehabfit.adapters.EjercicioDetalleAdapter;
import com.example.rehabfit.models.ConsultasIA;
import com.example.rehabfit.models.Ejercicio;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DetalleConsultasFragment extends Fragment {

    private static ConsultasIA consultaSeleccionada;

    private TextView txtTituloDetalleConsultaIA;
    private TextView txtFechaDetalleConsultaIA;
    private TextView txtConsultaDetalleIA;
    private TextView txtRecomendacionDetalleIA;
    private RecyclerView rvDetalleEjerciciosIA;
    private ImageButton btnVolverDetalleIA;

    public DetalleConsultasFragment() {
    }

    public static DetalleConsultasFragment newInstance(ConsultasIA consulta) {
        DetalleConsultasFragment fragment = new DetalleConsultasFragment();
        consultaSeleccionada = consulta;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_detalle_consultas, container, false);

        txtTituloDetalleConsultaIA = vista.findViewById(R.id.txtTituloDetalleConsultaIA);
        txtFechaDetalleConsultaIA = vista.findViewById(R.id.txtFechaDetalleConsultaIA);
        txtConsultaDetalleIA = vista.findViewById(R.id.txtConsultaDetalleIA);
        txtRecomendacionDetalleIA = vista.findViewById(R.id.txtRecomendacionDetalleIA);
        rvDetalleEjerciciosIA = vista.findViewById(R.id.rvDetalleEjerciciosIA);
        txtTituloDetalleConsultaIA = vista.findViewById(R.id.txtTituloDetalleConsultaIA);

        rvDetalleEjerciciosIA.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        cargarDatos();

        return vista;
    }

    private void cargarDatos() {
        if (consultaSeleccionada == null) {
            Toast.makeText(requireContext(), "No se encontró la consulta", Toast.LENGTH_SHORT).show();
            return;
        }

        txtTituloDetalleConsultaIA.setText("Detalle de consulta IA");
        txtFechaDetalleConsultaIA.setText(formatearFecha(consultaSeleccionada.getFechaMillis()));
        txtConsultaDetalleIA.setText("Consulta:\n\n" + consultaSeleccionada.getConsulta());
        txtRecomendacionDetalleIA.setText("Recomendación:\n\n" + limpiarTextoIA(consultaSeleccionada.getRecomendacion()));

        ArrayList<Ejercicio> ejercicios = new ArrayList<>();

        if (consultaSeleccionada.getEjerciciosRecomendados() != null) {
            ejercicios.addAll(consultaSeleccionada.getEjerciciosRecomendados());
        }

        EjercicioDetalleAdapter adapter = new EjercicioDetalleAdapter(ejercicios);
        rvDetalleEjerciciosIA.setAdapter(adapter);
    }

    private String formatearFecha(long fechaMillis) {
        SimpleDateFormat formato = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        return formato.format(new Date(fechaMillis));
    }

    private String limpiarTextoIA(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.replace("**", "").replace("* ", "• ").replace("*", "•").trim();
    }
}