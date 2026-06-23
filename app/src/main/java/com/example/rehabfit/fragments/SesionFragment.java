package com.example.rehabfit.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.rehabfit.R;
import com.example.rehabfit.models.Ejercicio;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Locale;

public class SesionFragment extends Fragment {

    private TextView txtPlanSesion;
    private TextView txtProgresoEjercicio;
    private TextView txtPorcentajeSesion;
    private ProgressBar progresoSesion;
    private ImageView imgIconoSesion;
    private TextView txtNombreEjercicioSesion;
    private TextView txtDescripcionSesion;
    private TextView txtCronometroSesion;
    private TextView txtEstadoCronometro;
    private TextView btnSaltarEjercicio;
    private TextView btnCompletarEjercicio;
    private TextView btnFinalizarSesion;

    private ArrayList<Ejercicio> ejercicios = new ArrayList<>();
    private int dolorAntes = 0;
    private int indiceActual = 0;
    private int ejerciciosCompletados = 0;
    private int ejerciciosSaltados = 0;

    private CountDownTimer timer;
    private long tiempoRestanteMillis = 0;
    private boolean pausado = false;

    public SesionFragment() {
    }

    public static SesionFragment newInstance(ArrayList<Ejercicio> ejercicios, int dolorAntes) {
        SesionFragment fragment = new SesionFragment();

        Bundle args = new Bundle();
        args.putSerializable("ejercicios", ejercicios);
        args.putInt("dolorAntes", dolorAntes);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_sesion, container, false);
        ocultarBottomNavigation();

        txtPlanSesion = vista.findViewById(R.id.txtPlanSesion);
        txtProgresoEjercicio = vista.findViewById(R.id.txtProgresoEjercicio);
        txtPorcentajeSesion = vista.findViewById(R.id.txtPorcentajeSesion);
        progresoSesion = vista.findViewById(R.id.progresoSesion);
        imgIconoSesion = vista.findViewById(R.id.imgIconoSesion);
        txtNombreEjercicioSesion = vista.findViewById(R.id.txtNombreEjercicioSesion);
        txtDescripcionSesion = vista.findViewById(R.id.txtDescripcionSesion);
        txtCronometroSesion = vista.findViewById(R.id.txtCronometroSesion);
        txtEstadoCronometro = vista.findViewById(R.id.txtEstadoCronometro);
        btnSaltarEjercicio = vista.findViewById(R.id.btnSaltarEjercicio);
        btnCompletarEjercicio = vista.findViewById(R.id.btnCompletarEjercicio);
        btnFinalizarSesion = vista.findViewById(R.id.btnFinalizarSesion);

        recibirDatos();
        configurarBotones();

        if (ejercicios.isEmpty()) {
            Toast.makeText(requireContext(), "No hay ejercicios en la rutina", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            cargarEjercicioActual();
        }

        return vista;
    }

    private void recibirDatos() {
        if (getArguments() == null) {
            return;
        }

        ArrayList<Ejercicio> lista =
                (ArrayList<Ejercicio>) getArguments().getSerializable("ejercicios");

        if (lista != null) {
            ejercicios = lista;
        }

        dolorAntes = getArguments().getInt("dolorAntes", 0);
    }

    private void configurarBotones() {
        btnCompletarEjercicio.setOnClickListener(v -> completarEjercicio());
        btnSaltarEjercicio.setOnClickListener(v -> saltarEjercicio());
        btnFinalizarSesion.setOnClickListener(v -> confirmarFinalizarSesion());
        txtCronometroSesion.setOnClickListener(v -> pausarOContinuar());
        txtEstadoCronometro.setOnClickListener(v -> pausarOContinuar());
    }

    private void cargarEjercicioActual() {
        if (timer != null) {
            timer.cancel();
        }

        Ejercicio ejercicio = ejercicios.get(indiceActual);

        txtPlanSesion.setText("Plan: Recuperación de " + obtenerZonaPrincipal());
        txtProgresoEjercicio.setText("Ejercicio " + (indiceActual + 1) + " de " + ejercicios.size());

        int porcentaje = (int) (((indiceActual + 1) * 100.0f) / ejercicios.size());
        txtPorcentajeSesion.setText(porcentaje + "%");
        progresoSesion.setProgress(porcentaje);

        if (ejercicio.getImagen() != null && !ejercicio.getImagen().isEmpty()) {

            Glide.with(requireContext())
                    .load(ejercicio.getImagen())
                    .placeholder(R.drawable.bg_info)
                    .error(R.drawable.ic_ejercicios)
                    .fitCenter()
                    .into(imgIconoSesion);
        } else {

            imgIconoSesion.setImageResource(R.drawable.ic_ejercicios);
        }
        txtNombreEjercicioSesion.setText(valorSeguro(ejercicio.getNombre(), "Ejercicio"));
        txtDescripcionSesion.setText(valorSeguro(ejercicio.getDescripcion(),
                "Realiza el movimiento lentamente y sin forzar."));

        int minutos = ejercicio.getDuracionMinutos();

        if (minutos <= 0) {
            minutos = 1;
        }

        tiempoRestanteMillis = minutos * 60L * 1000L;
        pausado = false;

        iniciarTimer();
    }

    private void iniciarTimer() {
        if (timer != null) {
            timer.cancel();
        }

        txtEstadoCronometro.setText("Toca para pausar");

        timer = new CountDownTimer(tiempoRestanteMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestanteMillis = millisUntilFinished;
                pintarTiempo(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                tiempoRestanteMillis = 0;
                pintarTiempo(0);
                completarEjercicio();
            }
        };

        timer.start();
    }

    private void pausarOContinuar() {
        if (pausado) {
            pausado = false;
            iniciarTimer();
        } else {
            pausado = true;

            if (timer != null) {
                timer.cancel();
            }

            txtEstadoCronometro.setText("Pausado · toca para continuar");
        }
    }

    private void pintarTiempo(long millis) {
        long segundosTotales = millis / 1000;
        long minutos = segundosTotales / 60;
        long segundos = segundosTotales % 60;

        txtCronometroSesion.setText(String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos));
    }

    private void completarEjercicio() {
        if (timer != null) {
            timer.cancel();
        }

        ejerciciosCompletados++;

        if (indiceActual < ejercicios.size() - 1) {
            indiceActual++;
            cargarEjercicioActual();
        } else {
            pedirDolorDespues();
        }
    }

    private void saltarEjercicio() {
        if (timer != null) {
            timer.cancel();
        }

        ejerciciosSaltados++;

        if (indiceActual < ejercicios.size() - 1) {
            indiceActual++;
            cargarEjercicioActual();
        } else {
            pedirDolorDespues();
        }
    }

    private void confirmarFinalizarSesion() {

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Finalizar sesión")
                .setMessage("¿Deseas finalizar la sesión actual?")
                .setPositiveButton("Finalizar", (d, which) -> finalizarSesionManual())
                .setNegativeButton("Continuar", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.verde_principal));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.verde_principal));
        });
        dialog.show();
    }

    //metodo para que cuando le damos a finalizar la app cuenta el ejercicio actual como completo
    private void finalizarSesionManual() {
        if (timer != null) {
            timer.cancel();
        }

        int totalProcesados = ejerciciosCompletados + ejerciciosSaltados;

        if (indiceActual >= totalProcesados && indiceActual < ejercicios.size()) {
            ejerciciosCompletados++;
        }
        pedirDolorDespues();
    }

    private void pedirDolorDespues() {
        if (timer != null) {
            timer.cancel();
        }

        LinearLayout contenedor = new LinearLayout(requireContext());
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(50, 25, 50, 10);

        final int[] dolorDespues = {dolorAntes};

        TextView txtValorDolor = new TextView(requireContext());
        txtValorDolor.setText("Dolor seleccionado: " + dolorAntes + "/10");
        txtValorDolor.setTextSize(16);
        txtValorDolor.setTextColor(getResources().getColor(R.color.texto_principal));

        SeekBar seekBarDolor = new SeekBar(requireContext());
        seekBarDolor.setMax(10);
        seekBarDolor.setProgress(dolorAntes);

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
        seekBarDolor.getProgressDrawable().setTint(getResources().getColor(R.color.verde_principal));
        seekBarDolor.getThumb().setTint(getResources().getColor(R.color.verde_principal));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Dolor después de la sesión")
                .setMessage("Selecciona cuánto dolor sientes ahora.")
                .setView(contenedor)
                .setPositiveButton("Continuar", (d, which) -> abrirSesionCompletada(dolorDespues[0]))
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.verde_principal));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.verde_principal));
        });
        dialog.show();
    }

    private void abrirSesionCompletada(int dolorDespues) {
        CompletadosFragment fragment = CompletadosFragment.newInstance(calcularMinutosTotales(), ejerciciosCompletados,
                ejerciciosSaltados, dolorAntes, dolorDespues, obtenerZonaPrincipal());

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, fragment)
                .commit();
    }

    private int calcularMinutosTotales() {
        int total = 0;

        for (Ejercicio ejercicio : ejercicios) {
            total += ejercicio.getDuracionMinutos();
        }

        if (total <= 0) {
            total = ejercicios.size();
        }

        return total;
    }

    private String obtenerZonaPrincipal() {
        if (ejercicios.isEmpty()) {
            return "Sin datos";
        }

        String zona = ejercicios.get(0).getZona();

        if (zona == null || zona.trim().isEmpty()) {
            return "Sin datos";
        }
        return zona;
    }
    private String valorSeguro(String texto, String defecto) {
        if (texto == null || texto.trim().isEmpty()) {
            return defecto;
        }

        return texto;
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

        if (timer != null) {
            timer.cancel();
        }
        mostrarBottomNavigation();
    }
}