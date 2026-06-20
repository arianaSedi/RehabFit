package com.example.rehabfit.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
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

        View vista = inflater.inflate(R.layout.fragment_rutina, container, false);

        txtResumenRutina = vista.findViewById(R.id.txtResumenRutina);
        rvRutina = vista.findViewById(R.id.rvRutina);
        btnIniciarRutina = vista.findViewById(R.id.btnIniciarRutina);
        btnAgregarEjercicio = vista.findViewById(R.id.btnAgregarEjercicio);

        rvRutina.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new RutinaAdapter(RutinaManager.obtenerRutina(), this::actualizarResumen);
        rvRutina.setAdapter(adapter);

        cargarRutinaGuardada();

        btnIniciarRutina.setOnClickListener(v -> iniciarRutina());

        btnAgregarEjercicio.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).cambiarFragmentBoton(R.id.nav_ejercicios);
            }
        });

        return vista;
    }

    private void cargarRutinaGuardada() {
        RutinaManager.cargarRutinaActual(() -> {
            if (!isAdded()) {
                return;
            }

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            actualizarResumen();
        });
    }

    private void iniciarRutina() {
        if (RutinaManager.obtenerRutina().isEmpty()) {
            Toast.makeText(requireContext(), "Primero agrega ejercicios a tu rutina", Toast.LENGTH_SHORT).show();
            return;
        }

        pedirDolorAntes();
    }

    private void pedirDolorAntes() {
        LinearLayout contenedor = new LinearLayout(requireContext());
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(50, 25, 50, 10);

        TextView txtValorDolor = new TextView(requireContext());
        txtValorDolor.setText("Dolor seleccionado: 0/10");
        txtValorDolor.setTextSize(16);
        txtValorDolor.setTextColor(getResources().getColor(R.color.texto_principal));

        SeekBar seekBarDolor = new SeekBar(requireContext());
        seekBarDolor.setMax(10);
        seekBarDolor.setProgress(0);

        seekBarDolor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dolorAntesRutina = progress;
                txtValorDolor.setText("Dolor seleccionado: " + progress + "/10");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        contenedor.addView(txtValorDolor);
        contenedor.addView(seekBarDolor);

        new AlertDialog.Builder(requireContext())
                .setTitle("Dolor antes de iniciar")
                .setMessage("Selecciona cuánto dolor sientes antes de empezar.")
                .setView(contenedor)
                .setPositiveButton("Iniciar sesión", (dialog, which) -> abrirSesionEnCurso())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirSesionEnCurso() {
        ArrayList<Ejercicio> ejercicios = new ArrayList<>(RutinaManager.obtenerRutina());

        SesionFragment fragment = SesionFragment.newInstance(ejercicios, dolorAntesRutina);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void actualizarResumen() {
        int cantidad = RutinaManager.obtenerRutina().size();
        int minutos = RutinaManager.obtenerTotalMinutos();

        txtResumenRutina.setText(cantidad + " ejercicios · " + minutos + " minutos");
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarRutinaGuardada();
    }
}