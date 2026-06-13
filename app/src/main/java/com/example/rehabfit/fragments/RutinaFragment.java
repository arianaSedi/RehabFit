package com.example.rehabfit.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.example.rehabfit.MainActivity;
import com.example.rehabfit.R;
import com.example.rehabfit.adapters.RutinaAdapter;
import com.example.rehabfit.utils.RutinaManager;

import java.util.Locale;

public class RutinaFragment extends Fragment {

    private TextView txtResumenRutina;
    private RecyclerView rvRutina;
    private AppCompatButton btnIniciarRutina;
    private AppCompatButton btnAgregarEjercicio;

    private RutinaAdapter adapter;
    private CountDownTimer countDownTimer;
    private int dolorAntesRutina = 0;
    private boolean rutinaEnCurso = false;

    public RutinaFragment() {
    }
    public static RutinaFragment newInstance(String param1, String param2) {
        RutinaFragment fragment = new RutinaFragment();
        Bundle args = new Bundle();
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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
            if (rutinaEnCurso) {
                Toast.makeText(requireContext(), "Termina la rutina antes de agregar ejercicios", Toast.LENGTH_SHORT).show();
                return;
            }

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

        if (rutinaEnCurso) {
            Toast.makeText(requireContext(), "La rutina ya está en curso", Toast.LENGTH_SHORT).show();
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
        txtValorDolor.setTextSize(18);
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
                .setMessage("Mueve la barra según el dolor que sientes ahora.")
                .setView(contenedor)
                .setPositiveButton("Iniciar", (dialog, which) -> iniciarCronometroRutina())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void iniciarCronometroRutina() {
        int totalMinutos = RutinaManager.obtenerTotalMinutos();

        if (totalMinutos <= 0) {
            Toast.makeText(requireContext(), "La rutina no tiene tiempo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        long tiempoTotalMillis = totalMinutos * 60L * 1000L;

        rutinaEnCurso = true;
        btnIniciarRutina.setEnabled(false);
        btnAgregarEjercicio.setEnabled(false);

        Toast.makeText(requireContext(), "Rutina iniciada", Toast.LENGTH_SHORT).show();

        countDownTimer = new CountDownTimer(tiempoTotalMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long segundosTotales = millisUntilFinished / 1000;
                long minutos = segundosTotales / 60;
                long segundos = segundosTotales % 60;

                String tiempo = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
                btnIniciarRutina.setText("Tiempo restante: " + tiempo);
            }

            @Override
            public void onFinish() {
                rutinaEnCurso = false;
                btnIniciarRutina.setEnabled(true);
                btnAgregarEjercicio.setEnabled(true);
                btnIniciarRutina.setText("▷ Iniciar rutina");

                pedirDolorDespuesYGuardar();
            }
        };

        countDownTimer.start();
    }

    private void pedirDolorDespuesYGuardar() {
        LinearLayout contenedor = new LinearLayout(requireContext());
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(50, 25, 50, 10);

        final int[] dolorDespues = {dolorAntesRutina};

        TextView txtValorDolor = new TextView(requireContext());
        txtValorDolor.setText("Dolor seleccionado: " + dolorAntesRutina + "/10");
        txtValorDolor.setTextSize(18);
        txtValorDolor.setTextColor(getResources().getColor(R.color.texto_principal));

        SeekBar seekBarDolor = new SeekBar(requireContext());
        seekBarDolor.setMax(10);
        seekBarDolor.setProgress(dolorAntesRutina);

        seekBarDolor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dolorDespues[0] = progress;
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
                .setTitle("Rutina finalizada")
                .setMessage("Mueve la barra según el dolor que sientes ahora.")
                .setView(contenedor)
                .setPositiveButton("Guardar sesión", (dialog, which) -> guardarSesionConDolor(dolorDespues[0]))
                .setCancelable(false)
                .show();
    }
    private void guardarSesionConDolor(int dolorDespues) {
        int minutosTerminados = RutinaManager.obtenerTotalMinutos();
        int cantidadEjercicios = RutinaManager.obtenerRutina().size();
        String zonaPrincipal = obtenerZonaPrincipal();

        RutinaManager.guardarSesionTerminada(
                minutosTerminados,
                cantidadEjercicios,
                dolorAntesRutina,
                dolorDespues,
                zonaPrincipal,
                new RutinaManager.AccionCallback() {
                    @Override
                    public void onExito() {
                        if (!isAdded()) {
                            return;
                        }

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Sesión guardada")
                                .setMessage("¡Muy bien! Terminaste tu rutina de " + minutosTerminados + " minutos.")
                                .setPositiveButton("Aceptar", null)
                                .show();
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(requireContext(), "Rutina terminada, pero no se pudo guardar: " + error, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private String obtenerZonaPrincipal() {
        if (RutinaManager.obtenerRutina().isEmpty()) {
            return "Sin datos";
        }

        if (RutinaManager.obtenerRutina().get(0).getZona() != null) {
            return RutinaManager.obtenerRutina().get(0).getZona();
        }

        return "Sin datos";
    }

    @Override
    public void onResume() {
        super.onResume();

        cargarRutinaGuardada();
    }
    private void actualizarResumen() {
        int cantidad = RutinaManager.obtenerRutina().size();
        int minutos = RutinaManager.obtenerTotalMinutos();

        txtResumenRutina.setText(cantidad + " ejercicios · " + minutos + " minutos");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}