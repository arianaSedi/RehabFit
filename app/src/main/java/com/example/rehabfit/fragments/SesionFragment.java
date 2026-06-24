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
import android.media.MediaPlayer;
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

    // metodo para crear una nueva instancia del fragment de sesion
    public static SesionFragment newInstance(ArrayList<Ejercicio> ejercicios, int dolorAntes) {

        // crea una nueva instancia del fragment
        SesionFragment fragment = new SesionFragment();

        // crea un bundle para enviar datos al fragment
        Bundle args = new Bundle();

        // envia la lista de ejercicios
        args.putSerializable("ejercicios", ejercicios);

        // envia el dolor registrado antes de iniciar la rutina
        args.putInt("dolorAntes", dolorAntes);

        // asigna los argumentos al fragment
        fragment.setArguments(args);

        // devuelve el fragment configurado
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // infla el layout de la sesion
        View vista = inflater.inflate(R.layout.fragment_sesion, container, false);

        // oculta la barra de navegacion inferior mientras se ejecuta la sesion
        ocultarBottomNavigation();

        // vincula los componentes visuales del layout
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

        // recibe la lista de ejercicios y el dolor inicial
        recibirDatos();

        // configura los eventos de los botones
        configurarBotones();

        // valida si la rutina no tiene ejercicios
        if (ejercicios.isEmpty()) {
            Toast.makeText(requireContext(), "No hay ejercicios en la rutina", Toast.LENGTH_SHORT).show();

            // regresa al fragment anterior
            requireActivity().getSupportFragmentManager().popBackStack();

        } else {

            // carga el primer ejercicio de la sesion
            cargarEjercicioActual();
        }

        // devuelve la vista del fragment
        return vista;
    }

    // metodo para recibir los datos enviados al fragment
    private void recibirDatos() {

        // valida que existan argumentos
        if (getArguments() == null) {
            return;
        }

        // obtiene la lista de ejercicios enviada
        ArrayList<Ejercicio> lista =
                (ArrayList<Ejercicio>) getArguments().getSerializable("ejercicios");

        // valida que la lista no sea nula
        if (lista != null) {

            // asigna la lista recibida a la variable global
            ejercicios = lista;
        }

        // obtiene el dolor antes de iniciar la rutina
        dolorAntes = getArguments().getInt("dolorAntes", 0);
    }

    // metodo para configurar los eventos de los botones
    private void configurarBotones() {

        // completa el ejercicio actual
        btnCompletarEjercicio.setOnClickListener(v -> completarEjercicio());

        // salta el ejercicio actual
        btnSaltarEjercicio.setOnClickListener(v -> saltarEjercicio());

        // muestra confirmacion para finalizar sesion
        btnFinalizarSesion.setOnClickListener(v -> confirmarFinalizarSesion());

        // permite pausar o continuar tocando el cronometro
        txtCronometroSesion.setOnClickListener(v -> pausarOContinuar());

        // permite pausar o continuar tocando el estado del cronometro
        txtEstadoCronometro.setOnClickListener(v -> pausarOContinuar());
    }

    // metodo para cargar el ejercicio actual en pantalla
    private void cargarEjercicioActual(boolean iniciarAutomatico) {

        // cancela el timer anterior si existe
        if (timer != null) {
            timer.cancel();
        }

        // obtiene el ejercicio actual segun el indice
        Ejercicio ejercicio = ejercicios.get(indiceActual);

        // muestra el plan de la sesion segun la zona principal
        txtPlanSesion.setText("Plan: Recuperación de " + obtenerZonaPrincipal());

        // muestra el numero de ejercicio actual
        txtProgresoEjercicio.setText("Ejercicio " + (indiceActual + 1) + " de " + ejercicios.size());

        // calcula el porcentaje de avance de la sesion
        int porcentaje = (int) (((indiceActual + 1) * 100.0f) / ejercicios.size());

        // muestra el porcentaje de avance
        txtPorcentajeSesion.setText(porcentaje + "%");

        // actualiza la barra de progreso
        progresoSesion.setProgress(porcentaje);

        // carga la imagen del ejercicio desde la api si existe
        if (ejercicio.getImagen() != null && !ejercicio.getImagen().isEmpty()) {
            Glide.with(requireContext())
                    .load(ejercicio.getImagen())
                    .placeholder(R.drawable.bg_info)
                    .error(R.drawable.ic_ejercicios)
                    .fitCenter()
                    .into(imgIconoSesion);
        } else {

            // si no hay imagen, muestra un icono por defecto
            imgIconoSesion.setImageResource(R.drawable.ic_ejercicios);
        }

        // muestra el nombre del ejercicio
        txtNombreEjercicioSesion.setText(valorSeguro(ejercicio.getNombre(), "Ejercicio"));

        // muestra la descripcion del ejercicio
        txtDescripcionSesion.setText(valorSeguro(ejercicio.getDescripcion(), "Realiza el movimiento lentamente y sin forzar."));

        // obtiene la duracion del ejercicio en minutos
        int minutos = ejercicio.getDuracionMinutos();

        // si la duracion es invalida, asigna 1 minuto por defecto
        if (minutos <= 0) {
            minutos = 1;
        }

        // convierte los minutos a milisegundos
        tiempoRestanteMillis = minutos * 60L * 1000L;

        // marca el cronometro como no pausado
        pausado = false;

        // muestra el tiempo inicial en pantalla
        pintarTiempo(tiempoRestanteMillis);

        // si se debe iniciar automatico, inicia el timer
        if (iniciarAutomatico) {
            iniciarTimer();
        } else {
            // si no inicia automatico, deja mensaje de preparacion
            txtEstadoCronometro.setText("Preparate para iniciar");
        }
    }

    // metodo que carga el ejercicio actual iniciando automaticamente el cronometro
    private void cargarEjercicioActual() {

        // llama al metodo principal indicando que debe iniciar automaticamente
        cargarEjercicioActual(true);
    }

    // metodo encargado de iniciar el cronometro del ejercicio
    private void iniciarTimer() {

        // si ya existe un timer activo, lo cancela para evitar duplicados
        if (timer != null) {
            timer.cancel();
        }

        // cambia el texto de estado del cronometro
        txtEstadoCronometro.setText("Toca para pausar");

        // crea un cronometro que disminuye cada segundo
        timer = new CountDownTimer(tiempoRestanteMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                // actualiza el tiempo restante
                tiempoRestanteMillis = millisUntilFinished;

                // pinta el tiempo actualizado en pantalla
                pintarTiempo(millisUntilFinished);
            }

            @Override
            public void onFinish() {

                // cuando termina el tiempo, deja el contador en cero
                tiempoRestanteMillis = 0;

                // muestra 00:00 en pantalla
                pintarTiempo(0);

                // marca el ejercicio como completado
                completarEjercicio();
            }
        };

        // inicia el cronometro
        timer.start();
    }

    // metodo para pausar o continuar el cronometro
    private void pausarOContinuar() {

        // si estaba pausado, continua el cronometro
        if (pausado) {
            pausado = false;
            iniciarTimer();

        } else {

            // si estaba activo, lo pausa
            pausado = true;

            // cancela el timer actual
            if (timer != null) {
                timer.cancel();
            }

            // muestra estado de pausa
            txtEstadoCronometro.setText("Pausado · toca para continuar");
        }
    }

    // metodo para mostrar el tiempo en formato minutos y segundos
    private void pintarTiempo(long millis) {

        // convierte milisegundos a segundos
        long segundosTotales = millis / 1000;

        // calcula los minutos
        long minutos = segundosTotales / 60;

        // calcula los segundos restantes
        long segundos = segundosTotales % 60;

        // muestra el tiempo con formato 00:00
        txtCronometroSesion.setText(String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos));
    }

    // metodo para completar el ejercicio actual
    private void completarEjercicio() {

        // cancela el cronometro si esta activo
        if (timer != null) {
            timer.cancel();
        }

        // aumenta el contador de ejercicios completados
        ejerciciosCompletados++;

        // valida si aun hay mas ejercicios pendientes
        if (indiceActual < ejercicios.size() - 1) {

            // avanza al siguiente ejercicio
            indiceActual++;

            // carga el siguiente ejercicio sin iniciar automaticamente
            cargarEjercicioActual(false);

            // muestra mensaje de preparacion
            Toast.makeText(requireContext(), "Preparate para el siguiente ejercicio", Toast.LENGTH_SHORT).show();

            // reproduce sonido de cambio y luego inicia el cronometro
            reproducirSonidoCambioEjercicio(() -> {
                iniciarTimer();
            });

        } else {

            // si ya no hay mas ejercicios, pide el dolor despues
            pedirDolorDespues();
        }
    }

    // metodo para saltar el ejercicio actual
    private void saltarEjercicio() {

        // cancela el cronometro si esta activo
        if (timer != null) {
            timer.cancel();
        }

        // aumenta el contador de ejercicios saltados
        ejerciciosSaltados++;

        // valida si aun hay mas ejercicios
        if (indiceActual < ejercicios.size() - 1) {

            // avanza al siguiente ejercicio
            indiceActual++;

            // carga el siguiente ejercicio iniciando automaticamente
            cargarEjercicioActual();

        } else {

            // si ya no hay mas ejercicios, pide dolor despues
            pedirDolorDespues();
        }
    }

    // metodo para confirmar si el usuario desea finalizar la sesion
    private void confirmarFinalizarSesion() {

        // crea un dialogo de confirmacion
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Finalizar sesión")
                .setMessage("¿Deseas finalizar la sesión actual?")
                .setPositiveButton("Finalizar", (d, which) -> finalizarSesionManual())
                .setNegativeButton("Continuar", null)
                .create();

        // cambia el color de los botones del dialogo
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.verde_principal));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.verde_principal));
        });

        // muestra el dialogo
        dialog.show();
    }

    // metodo para que cuando se finaliza la sesion se cuente el ejercicio actual como completado
    private void finalizarSesionManual() {

        // cancela el cronometro si esta activo
        if (timer != null) {
            timer.cancel();
        }

        // calcula cuantos ejercicios ya fueron procesados
        int totalProcesados = ejerciciosCompletados + ejerciciosSaltados;

        // si el ejercicio actual aun no habia sido contado, lo cuenta como completado
        if (indiceActual >= totalProcesados && indiceActual < ejercicios.size()) {
            ejerciciosCompletados++;
        }

        // solicita el dolor despues de la sesion
        pedirDolorDespues();
    }

    // metodo para pedir el nivel de dolor despues de la sesion
    private void pedirDolorDespues() {

        // cancela el timer si existe
        if (timer != null) {
            timer.cancel();
        }

        // crea un contenedor vertical para el texto y la barra
        LinearLayout contenedor = new LinearLayout(requireContext());
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(50, 25, 50, 10);

        // arreglo para guardar el dolor seleccionado dentro del listener
        final int[] dolorDespues = {dolorAntes};

        // texto que muestra el dolor seleccionado
        TextView txtValorDolor = new TextView(requireContext());
        txtValorDolor.setText("Dolor seleccionado: " + dolorAntes + "/10");
        txtValorDolor.setTextSize(16);
        txtValorDolor.setTextColor(getResources().getColor(R.color.texto_principal));

        // barra para seleccionar el dolor del 0 al 10
        SeekBar seekBarDolor = new SeekBar(requireContext());
        seekBarDolor.setMax(10);
        seekBarDolor.setProgress(dolorAntes);

        // escucha los cambios del seekbar
        seekBarDolor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // guarda el valor seleccionado
                dolorDespues[0] = progress;
                // actualiza el texto del valor
                txtValorDolor.setText("Dolor seleccionado: " + progress + "/10");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // agrega los elementos al contenedor
        contenedor.addView(txtValorDolor);
        contenedor.addView(seekBarDolor);
        // cambia el color del seekbar
        seekBarDolor.getProgressDrawable().setTint(getResources().getColor(R.color.verde_principal));
        seekBarDolor.getThumb().setTint(getResources().getColor(R.color.verde_principal));

        // crea el dialogo para registrar dolor despues
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Dolor después de la sesión")
                .setMessage("Selecciona cuánto dolor sientes ahora.")
                .setView(contenedor)
                .setPositiveButton("Continuar", (d, which) -> abrirSesionCompletada(dolorDespues[0]))
                .setNegativeButton("Cancelar", null)
                .create();

        // cambia el color de los botones del dialogo
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.verde_principal));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.verde_principal));
        });
        // muestra el dialogo
        dialog.show();
    }

    // metodo para abrir la pantalla de sesion completada
    private void abrirSesionCompletada(int dolorDespues) {
        // crea el fragment de completados enviando los datos de la sesion
        CompletadosFragment fragment = CompletadosFragment.newInstance(calcularMinutosTotales(), ejerciciosCompletados,
                ejerciciosSaltados, dolorAntes, dolorDespues, obtenerZonaPrincipal());

        // reemplaza la pantalla actual por la pantalla de completados
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, fragment)
                .commit();
    }

    // metodo para calcular los minutos totales de la sesion
    private int calcularMinutosTotales() {
        // variable acumuladora de minutos
        int total = 0;

        // suma la duracion de todos los ejercicios
        for (Ejercicio ejercicio : ejercicios) {
            total += ejercicio.getDuracionMinutos();
        }

        // si no hay duracion valida, usa la cantidad de ejercicios como minutos
        if (total <= 0) {
            total = ejercicios.size();
        }

        // devuelve el total de minutos
        return total;
    }

    // metodo para obtener la zona principal de la rutina
    private String obtenerZonaPrincipal() {
        // valida si no hay ejercicios
        if (ejercicios.isEmpty()) {
            return "Sin datos";
        }

        // obtiene la zona del primer ejercicio
        String zona = ejercicios.get(0).getZona();

        // valida que la zona no este vacia
        if (zona == null || zona.trim().isEmpty()) {
            return "Sin datos";
        }

        return zona;
    }
    // metodo para devolver un texto seguro si viene nulo o vacio
    private String valorSeguro(String texto, String defecto) {
        // valida si el texto es nulo o esta vacio
        if (texto == null || texto.trim().isEmpty()) {
            return defecto;
        }

        // devuelve el texto original
        return texto;
    }

    // metodo para reproducir sonido antes de pasar al siguiente ejercicio
    private void reproducirSonidoCambioEjercicio(Runnable accionAlFinal) {

        // crea el reproductor con el sonido del contador
        MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.contador
        );

        // si no se pudo crear el sonido, ejecuta la accion final directamente
        if (mediaPlayer == null) {
            if (accionAlFinal != null) {
                accionAlFinal.run();
            }
            return;
        }

        // escucha cuando termina el sonido
        mediaPlayer.setOnCompletionListener(mp -> {
            // libera recursos del reproductor
            mp.release();

            // ejecuta la accion final despues del sonido
            if (accionAlFinal != null) {
                accionAlFinal.run();
            }
        });
        // inicia el sonido
        mediaPlayer.start();
    }

    // metodo para ocultar la barra inferior de navegacion
    private void ocultarBottomNavigation() {
        // valida que la actividad exista
        if (getActivity() != null) {
            // obtiene la barra inferior
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigation);
            // valida que la barra exista
            if (bottomNavigationView != null) {

                // oculta la barra
                bottomNavigationView.setVisibility(View.GONE);
            }
        }
    }

    // metodo para mostrar la barra inferior de navegacion
    private void mostrarBottomNavigation() {
        // valida que la actividad exista
        if (getActivity() != null) {
            // obtiene la barra inferior
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigation);
            // valida que la barra exista
            if (bottomNavigationView != null) {
                // muestra la barra
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // cancela el cronometro al salir del fragment
        if (timer != null) {
            timer.cancel();
        }
        // vuelve a mostrar la barra inferior
        mostrarBottomNavigation();
    }
}