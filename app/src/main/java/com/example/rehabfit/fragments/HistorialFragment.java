package com.example.rehabfit.fragments;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_historial, container, false);

        // vincula los botones de filtro y componentes visuales
        btnHoy = vista.findViewById(R.id.btnHoy);
        btnSemana = vista.findViewById(R.id.btnSemana);
        btnMes = vista.findViewById(R.id.btnMes);
        rvHistorial = vista.findViewById(R.id.rvHistorial);
        txtHistorialVacio = vista.findViewById(R.id.txtHistorialVacio);

        // configura el recycler view en forma de lista vertical
        rvHistorial.setLayoutManager(new LinearLayoutManager(requireContext()));

        // crea el adaptador con la lista de sesiones filtradas
        adapter = new HistorialAdapter(sesionesFiltradas);

        // asigna el adaptador al recycler view
        rvHistorial.setAdapter(adapter);

        // configura los eventos de los filtros
        configurarFiltros();

        // carga las sesiones guardadas del usuario
        cargarSesiones();

        // devuelve la vista del fragment
        return vista;
    }

    // metodo encargado de configurar los botones de filtro
    private void configurarFiltros() {

        // filtra las sesiones del dia actual
        btnHoy.setOnClickListener(v -> {
            filtroActual = "HOY";
            filtrarSesiones();
        });

        // filtra las sesiones de la semana actual
        btnSemana.setOnClickListener(v -> {
            filtroActual = "SEMANA";
            filtrarSesiones();
        });

        // filtra las sesiones del mes actual
        btnMes.setOnClickListener(v -> {
            filtroActual = "MES";
            filtrarSesiones();
        });
    }

    // metodo encargado de cargar las sesiones desde firebase
    private void cargarSesiones() {

        // obtiene el usuario autenticado
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        // valida que exista un usuario activo
        if (usuario == null) {
            Toast.makeText(requireContext(), "No hay usuario activo", Toast.LENGTH_SHORT).show();
            return;
        }

        // referencia al historial de sesiones del usuario
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getUid())
                .child("sesiones");

        // obtiene las sesiones guardadas una sola vez
        ref.get()
                .addOnSuccessListener(snapshot -> {

                    // limpia la lista antes de cargar datos actualizados
                    todasLasSesiones.clear();

                    // recorre cada sesion encontrada
                    for (DataSnapshot item : snapshot.getChildren()) {

                        // convierte cada registro en un objeto sesion rutina
                        SesionRutina sesion = item.getValue(SesionRutina.class);

                        // valida que la sesion no sea nula
                        if (sesion != null) {

                            // agrega la sesion a la lista general
                            todasLasSesiones.add(sesion);
                        }
                    }

                    // ordena las sesiones desde la mas reciente hasta la mas antigua
                    Collections.sort(todasLasSesiones, (s1, s2) ->
                            Long.compare(s2.getFechaMillis(), s1.getFechaMillis())
                    );

                    // aplica el filtro actual a las sesiones cargadas
                    filtrarSesiones();
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

    // metodo encargado de filtrar las sesiones segun el filtro seleccionado
    private void filtrarSesiones() {

        // limpia la lista filtrada antes de agregar nuevos resultados
        sesionesFiltradas.clear();

        // recorre todas las sesiones cargadas
        for (SesionRutina sesion : todasLasSesiones) {

            // agrega solo las sesiones que pertenecen al dia actual
            if (filtroActual.equals("HOY") && esDeHoy(sesion.getFechaMillis())) {
                sesionesFiltradas.add(sesion);

                // agrega solo las sesiones que pertenecen a la semana actual
            } else if (filtroActual.equals("SEMANA") && esDeEstaSemana(sesion.getFechaMillis())) {
                sesionesFiltradas.add(sesion);

                // agrega solo las sesiones que pertenecen al mes actual
            } else if (filtroActual.equals("MES") && esDeEsteMes(sesion.getFechaMillis())) {
                sesionesFiltradas.add(sesion);
            }
        }

        // actualiza la lista del adaptador
        adapter.actualizarLista(sesionesFiltradas);

        // muestra mensaje de historial vacio si no hay sesiones
        if (sesionesFiltradas.isEmpty()) {
            txtHistorialVacio.setVisibility(View.VISIBLE);
            rvHistorial.setVisibility(View.GONE);
        } else {

            // muestra la lista si existen sesiones
            txtHistorialVacio.setVisibility(View.GONE);
            rvHistorial.setVisibility(View.VISIBLE);
        }

        // actualiza el estilo de los botones
        actualizarBotones();
    }

    // metodo para actualizar visualmente los botones de filtro
    private void actualizarBotones() {

        // marca el boton hoy si esta seleccionado
        pintarBoton(btnHoy, filtroActual.equals("HOY"));

        // marca el boton semana si esta seleccionado
        pintarBoton(btnSemana, filtroActual.equals("SEMANA"));

        // marca el boton mes si esta seleccionado
        pintarBoton(btnMes, filtroActual.equals("MES"));
    }

    // metodo para cambiar el color de un boton segun su estado
    private void pintarBoton(AppCompatButton boton, boolean seleccionado) {

        // selecciona el color de fondo dependiendo si el boton esta activo
        int colorFondo = seleccionado ? R.color.verde_principal : R.color.gris_chip;

        // selecciona el color de texto dependiendo si el boton esta activo
        int colorTexto = seleccionado ? R.color.blanco : R.color.texto_principal;

        // aplica el color de fondo al boton
        boton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorFondo)));

        // aplica el color del texto al boton
        boton.setTextColor(getResources().getColor(colorTexto));
    }

    // metodo para verificar si una fecha pertenece al dia actual
    private boolean esDeHoy(long fechaMillis) {

        // crea un calendario con la fecha recibida
        Calendar fecha = Calendar.getInstance();
        fecha.setTimeInMillis(fechaMillis);

        // crea un calendario con la fecha actual
        Calendar hoy = Calendar.getInstance();

        // compara el año y el dia del año
        return fecha.get(Calendar.YEAR) == hoy.get(Calendar.YEAR)
                && fecha.get(Calendar.DAY_OF_YEAR) == hoy.get(Calendar.DAY_OF_YEAR);
    }

    // metodo para verificar si una fecha pertenece a la semana actual
    private boolean esDeEstaSemana(long fechaMillis) {

        // crea un calendario con la fecha recibida
        Calendar fecha = Calendar.getInstance();
        fecha.setTimeInMillis(fechaMillis);

        // obtiene la fecha actual para calcular el inicio de la semana
        Calendar inicioSemana = Calendar.getInstance();

        // obtiene el dia actual de la semana
        int diaActual = inicioSemana.get(Calendar.DAY_OF_WEEK);

        // calcula cuantos dias debe retroceder para llegar al lunes
        int diferencia = (diaActual == Calendar.SUNDAY)
                ? -6 : Calendar.MONDAY - diaActual;

        // mueve el calendario al lunes de la semana actual
        inicioSemana.add(Calendar.DAY_OF_MONTH, diferencia);

        // ajusta la hora al inicio del dia
        inicioSemana.set(Calendar.HOUR_OF_DAY, 0);
        inicioSemana.set(Calendar.MINUTE, 0);
        inicioSemana.set(Calendar.SECOND, 0);
        inicioSemana.set(Calendar.MILLISECOND, 0);

        // crea una copia para calcular el fin de la semana
        Calendar finSemana = (Calendar) inicioSemana.clone();

        // suma 7 dias para marcar el final de la semana
        finSemana.add(Calendar.DAY_OF_MONTH, 7);

        // valida si la fecha esta entre el inicio y fin de la semana
        return fecha.getTimeInMillis() >= inicioSemana.getTimeInMillis() && fecha.getTimeInMillis() < finSemana.getTimeInMillis();
    }

    // metodo para verificar si una fecha pertenece al mes actual
    private boolean esDeEsteMes(long fechaMillis) {

        // crea un calendario con la fecha recibida
        Calendar fecha = Calendar.getInstance();
        fecha.setTimeInMillis(fechaMillis);

        // crea un calendario con la fecha actual
        Calendar hoy = Calendar.getInstance();

        // compara año y mes
        return fecha.get(Calendar.YEAR) == hoy.get(Calendar.YEAR) && fecha.get(Calendar.MONTH) == hoy.get(Calendar.MONTH);
    }
}