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

    public DetalleConsultasFragment() {
    }

    // metodo para crear una nueva instancia del fragment con la consulta seleccionada
    public static DetalleConsultasFragment newInstance(ConsultasIA consulta) {

        // crea una nueva instancia del fragment
        DetalleConsultasFragment fragment = new DetalleConsultasFragment();

        // guarda la consulta seleccionada para mostrarla en el detalle
        consultaSeleccionada = consulta;

        // devuelve el fragment creado
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // infla el layout del detalle de consultas
        View vista = inflater.inflate(R.layout.fragment_detalle_consultas, container, false);

        // vincula los componentes visuales del layout
        txtTituloDetalleConsultaIA = vista.findViewById(R.id.txtTituloDetalleConsultaIA);
        txtFechaDetalleConsultaIA = vista.findViewById(R.id.txtFechaDetalleConsultaIA);
        txtConsultaDetalleIA = vista.findViewById(R.id.txtConsultaDetalleIA);
        txtRecomendacionDetalleIA = vista.findViewById(R.id.txtRecomendacionDetalleIA);
        rvDetalleEjerciciosIA = vista.findViewById(R.id.rvDetalleEjerciciosIA);

        // configura el recycler view para mostrar los ejercicios en forma de lista
        rvDetalleEjerciciosIA.setLayoutManager(new LinearLayoutManager(requireContext()));

        // carga la informacion de la consulta seleccionada
        cargarDatos();

        // devuelve la vista del fragment
        return vista;
    }

    // metodo encargado de cargar los datos de la consulta en pantalla
    private void cargarDatos() {

        // valida que exista una consulta seleccionada
        if (consultaSeleccionada == null) {

            // muestra mensaje si no se encontro la consulta
            Toast.makeText(requireContext(), "No se encontró la consulta", Toast.LENGTH_SHORT).show();
            return;
        }

        // muestra el titulo de la pantalla
        txtTituloDetalleConsultaIA.setText("Detalle de consulta IA");

        // muestra la fecha formateada de la consulta
        txtFechaDetalleConsultaIA.setText(formatearFecha(consultaSeleccionada.getFechaMillis()));

        // muestra el texto de la consulta realizada
        txtConsultaDetalleIA.setText("Consulta:\n\n" + consultaSeleccionada.getConsulta());

        // muestra la recomendacion generada por la ia
        txtRecomendacionDetalleIA.setText("Recomendación:\n\n" + limpiarTextoIA(consultaSeleccionada.getRecomendacion()));

        // crea una lista para almacenar los ejercicios recomendados
        ArrayList<Ejercicio> ejercicios = new ArrayList<>();

        // valida si la consulta tiene ejercicios recomendados
        if (consultaSeleccionada.getEjerciciosRecomendados() != null) {

            // agrega los ejercicios recomendados a la lista
            ejercicios.addAll(consultaSeleccionada.getEjerciciosRecomendados());
        }

        // crea el adaptador para mostrar los ejercicios recomendados
        EjercicioDetalleAdapter adapter = new EjercicioDetalleAdapter(ejercicios);

        // asigna el adaptador al recycler view
        rvDetalleEjerciciosIA.setAdapter(adapter);
    }

    // metodo para convertir la fecha en milisegundos a un formato legible
    private String formatearFecha(long fechaMillis) {

        // define el formato de fecha que se mostrara en pantalla
        SimpleDateFormat formato = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());

        // convierte los milisegundos en fecha y la devuelve como texto
        return formato.format(new Date(fechaMillis));
    }

    // metodo para limpiar simbolos innecesarios del texto generado por la ia
    private String limpiarTextoIA(String texto) {
        // si el texto es nulo devuelve una cadena vacia
        if (texto == null) {
            return "";
        }

        // reemplaza simbolos de formato por viñetas y elimina espacios extras
        return texto.replace("**", "").replace("* ", "• ").replace("*", "•").trim();
    }
}