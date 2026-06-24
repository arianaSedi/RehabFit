package com.example.rehabfit.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rehabfit.MainActivity;
import com.example.rehabfit.R;
import com.example.rehabfit.adapters.RutinaAdapter;
import com.example.rehabfit.models.Ejercicio;
import com.example.rehabfit.utils.RutinaManager;

import java.util.ArrayList;

public class RutinaFragment extends Fragment {

    private TextView txtResumenRutina;
    private RecyclerView rvRutina;
    private AppCompatButton btnIniciarRutina;
    private AppCompatButton btnAgregarEjercicio;

    private RutinaAdapter adapter;
    private int dolorAntesRutina = 0;

    public RutinaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // infla el layout del fragment de rutina
        View vista = inflater.inflate(R.layout.fragment_rutina, container, false);

        // vincula los componentes visuales del layout
        txtResumenRutina = vista.findViewById(R.id.txtResumenRutina);
        rvRutina = vista.findViewById(R.id.rvRutina);
        btnIniciarRutina = vista.findViewById(R.id.btnIniciarRutina);
        btnAgregarEjercicio = vista.findViewById(R.id.btnAgregarEjercicio);

        // configura el recycler view para mostrar la rutina en forma de lista
        rvRutina.setLayoutManager(new LinearLayoutManager(requireContext()));

        // crea el adaptador usando la rutina actual y el metodo para actualizar resumen
        adapter = new RutinaAdapter(RutinaManager.obtenerRutina(), this::actualizarResumen);

        // asigna el adaptador al recycler view
        rvRutina.setAdapter(adapter);

        // carga la rutina guardada del usuario
        cargarRutinaGuardada();

        // inicia la rutina al presionar el boton
        btnIniciarRutina.setOnClickListener(v -> iniciarRutina());

        // permite ir a la seccion de ejercicios para agregar mas ejercicios
        btnAgregarEjercicio.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).cambiarFragmentBoton(R.id.nav_ejercicios);
            }
        });

        // devuelve la vista del fragment
        return vista;
    }

    // metodo encargado de cargar la rutina guardada
    private void cargarRutinaGuardada() {

        // carga la rutina actual desde el manager
        RutinaManager.cargarRutinaActual(() -> {

            // valida que el fragment siga activo
            if (!isAdded()) {
                return;
            }

            // actualiza el adaptador si existe
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            // actualiza el resumen de ejercicios y minutos
            actualizarResumen();
        });
    }

    // metodo para iniciar la rutina
    private void iniciarRutina() {

        // valida que existan ejercicios agregados
        if (RutinaManager.obtenerRutina().isEmpty()) {
            Toast.makeText(requireContext(), "Primero agrega ejercicios a tu rutina", Toast.LENGTH_SHORT).show();
            return;
        }

        // solicita el nivel de dolor antes de iniciar
        pedirDolorAntes();
    }

    // metodo para pedir el nivel de dolor antes de iniciar la sesion
    private void pedirDolorAntes() {

        // crea un contenedor vertical para el texto y el seekbar
        LinearLayout contenedor = new LinearLayout(requireContext());
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(50, 25, 50, 10);

        // texto que muestra el nivel de dolor seleccionado
        TextView txtValorDolor = new TextView(requireContext());
        txtValorDolor.setText("Dolor seleccionado: 0/10");
        txtValorDolor.setTextSize(16);
        txtValorDolor.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_principal));

        // barra para seleccionar el nivel de dolor del 0 al 10
        SeekBar seekBarDolor = new SeekBar(requireContext());
        seekBarDolor.setMax(10);
        seekBarDolor.setProgress(0);

        // cambia el color de la barra y el indicador
        seekBarDolor.getProgressDrawable().setTint(ContextCompat.getColor(requireContext(), R.color.verde_principal));
        seekBarDolor.getThumb().setTint(ContextCompat.getColor(requireContext(), R.color.verde_principal));

        // detecta cambios en el nivel de dolor seleccionado
        seekBarDolor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // guarda el dolor seleccionado
                dolorAntesRutina = progress;

                // actualiza el texto con el valor seleccionado
                txtValorDolor.setText("Dolor seleccionado: " + progress + "/10");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // agrega el texto y el seekbar al contenedor
        contenedor.addView(txtValorDolor);
        contenedor.addView(seekBarDolor);

        // crea el dialogo para seleccionar dolor antes de iniciar
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Dolor antes de iniciar")
                .setMessage("Selecciona cuánto dolor sientes antes de empezar.")
                .setView(contenedor)
                .setPositiveButton("Iniciar sesión", (d, which) -> abrirSesionEnCurso())
                .setNegativeButton("Cancelar", null)
                .create();

        // cambia el color de los botones del dialogo
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.verde_principal));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.verde_principal));
        });

        // muestra el dialogo
        dialog.show();
    }

    // metodo para abrir la pantalla de sesion en curso
    private void abrirSesionEnCurso() {

        // obtiene una copia de los ejercicios actuales de la rutina
        ArrayList<Ejercicio> ejercicios = new ArrayList<>(RutinaManager.obtenerRutina());

        // crea el fragment de sesion enviando ejercicios y dolor inicial
        SesionFragment fragment = SesionFragment.newInstance(ejercicios, dolorAntesRutina);

        // reemplaza el fragment actual por el fragment de sesion
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, fragment)
                .addToBackStack(null)
                .commit();
    }

    // metodo para actualizar el resumen de la rutina
    private void actualizarResumen() {

        // obtiene la cantidad de ejercicios en la rutina
        int cantidad = RutinaManager.obtenerRutina().size();

        // obtiene la duracion total en minutos
        int minutos = RutinaManager.obtenerTotalMinutos();

        // muestra el resumen en pantalla
        txtResumenRutina.setText(cantidad + " ejercicios · " + minutos + " minutos");
    }

    @Override
    public void onResume() {
        super.onResume();

        // vuelve a cargar la rutina al regresar al fragment
        cargarRutinaGuardada();
    }
}