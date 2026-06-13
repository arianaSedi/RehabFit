package com.example.rehabfit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.rehabfit.R;
import com.example.rehabfit.models.SesionRutina;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProgresoFragment extends Fragment {

    private TextView txtSesionesCompletadas;
    private TextView txtTiempoTerapia;
    private TextView txtZonaMasTrabajada;
    private TextView txtDolorPromedioProgreso;
    private TextView txtResumenSemana;
    private TextView txtSemanasDolor;
    private TextView txtMensajeProgreso;

    public ProgresoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_progreso, container, false);

        txtSesionesCompletadas = vista.findViewById(R.id.txtSesionesCompletadas);
        txtTiempoTerapia = vista.findViewById(R.id.txtTiempoTerapia);
        txtZonaMasTrabajada = vista.findViewById(R.id.txtZonaMasTrabajada);
        txtDolorPromedioProgreso = vista.findViewById(R.id.txtDolorPromedioProgreso);
        txtResumenSemana = vista.findViewById(R.id.txtResumenSemana);
        txtSemanasDolor = vista.findViewById(R.id.txtSemanasDolor);
        txtMensajeProgreso = vista.findViewById(R.id.txtMensajeProgreso);

        cargarProgreso();

        return vista;
    }

    private void cargarProgreso() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(requireContext(), "No hay usuario activo", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(user.getUid())
                .child("sesiones");

        ref.get().addOnSuccessListener(snapshot -> {
            List<SesionRutina> sesiones = new ArrayList<>();

            for (DataSnapshot item : snapshot.getChildren()) {
                SesionRutina sesion = item.getValue(SesionRutina.class);

                if (sesion != null) {
                    sesiones.add(sesion);
                }
            }
            mostrarResumen(sesiones);
        }).addOnFailureListener(e -> {
            if (!isAdded()) {
                return;
            }
            Toast.makeText(requireContext(), "Error al cargar progreso: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
    private void mostrarResumen(List<SesionRutina> sesiones) {
        int totalSesiones = sesiones.size();
        int totalMinutos = 0;

        double sumaDolor = 0;
        int contadorDolor = 0;

        HashMap<String, Integer> zonas = new HashMap<>();
        int[] sesionesSemana = new int[7];

        for (SesionRutina sesion : sesiones) {
            totalMinutos += sesion.getMinutosTotales();

            if (sesion.getDolorDespues() > 0) {
                sumaDolor += sesion.getDolorDespues();
                contadorDolor++;
            }

            String zona = sesion.getZonaPrincipal();

            if (zona != null && !zona.isEmpty()) {
                zonas.put(zona, zonas.getOrDefault(zona, 0) + 1);
            }
            if (estaEnSemanaActual(sesion.getFechaMillis())) {
                int dia = obtenerIndiceDia(sesion.getFechaMillis());

                if (dia >= 0 && dia < 7) {
                    sesionesSemana[dia]++;
                }
            }
        }

        double promedioDolor = contadorDolor > 0 ? sumaDolor / contadorDolor : 0.0;
        String zonaMasTrabajada = obtenerZonaMasTrabajada(zonas);

        txtSesionesCompletadas.setText(totalSesiones + "\nSesiones completadas");
        txtTiempoTerapia.setText(totalMinutos + " min\nTiempo de terapia");
        txtZonaMasTrabajada.setText(zonaMasTrabajada + "\nZona más trabajada");
        txtDolorPromedioProgreso.setText(String.format(Locale.getDefault(), "%.1f\nDolor promedio", promedioDolor));

        txtResumenSemana.setText(
                "L:" + sesionesSemana[0] + "   " +
                        "M:" + sesionesSemana[1] + "   " +
                        "X:" + sesionesSemana[2] + "   " +
                        "J:" + sesionesSemana[3] + "   " +
                        "V:" + sesionesSemana[4] + "   " +
                        "S:" + sesionesSemana[5] + "   " +
                        "D:" + sesionesSemana[6]
        );
        double[] promedios = calcularPromedioDolorUltimas3Semanas(sesiones);

        txtSemanasDolor.setText(
                "Sem 1: " + redondear(promedios[0]) + "/10   " +
                        "Sem 2: " + redondear(promedios[1]) + "/10   " +
                        "Sem 3: " + redondear(promedios[2]) + "/10"
        );
        mostrarMensajeProgreso(promedios);
    }
    private void mostrarMensajeProgreso(double[] promedios) {
        double dolorInicial = promedios[0];
        double dolorActual = promedios[2];

        if (dolorInicial <= 0 || dolorActual <= 0) {
            txtMensajeProgreso.setText("Aún no hay suficientes datos de dolor para evaluar tu progreso.");
            return;
        }
        double disminucion = dolorInicial - dolorActual;

        if (disminucion > 0) {
            txtMensajeProgreso.setText(
                    "💡 ¡Excelente progreso!\nReduciste tu dolor promedio de "
                            + redondear(dolorInicial)
                            + " a "
                            + redondear(dolorActual)
                            + "."
            );
        } else if (disminucion == 0) {
            txtMensajeProgreso.setText("Tu dolor promedio se mantiene estable. Sigue registrando tus sesiones.");
        } else {
            txtMensajeProgreso.setText("Tu dolor promedio aumentó. Si sientes molestias fuertes, consulta con un profesional.");
        }
    }
    private boolean estaEnSemanaActual(long fechaMillis) {
        Calendar fecha = Calendar.getInstance();
        fecha.setTimeInMillis(fechaMillis);

        Calendar inicioSemana = Calendar.getInstance();

        int diaActual = inicioSemana.get(Calendar.DAY_OF_WEEK);
        int diferencia = (diaActual == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - diaActual;

        inicioSemana.add(Calendar.DAY_OF_MONTH, diferencia);
        inicioSemana.set(Calendar.HOUR_OF_DAY, 0);
        inicioSemana.set(Calendar.MINUTE, 0);
        inicioSemana.set(Calendar.SECOND, 0);
        inicioSemana.set(Calendar.MILLISECOND, 0);

        return fecha.getTimeInMillis() >= inicioSemana.getTimeInMillis();
    }
    private int obtenerIndiceDia(long fechaMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(fechaMillis);

        int day = c.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.MONDAY:
                return 0;
            case Calendar.TUESDAY:
                return 1;
            case Calendar.WEDNESDAY:
                return 2;
            case Calendar.THURSDAY:
                return 3;
            case Calendar.FRIDAY:
                return 4;
            case Calendar.SATURDAY:
                return 5;
            case Calendar.SUNDAY:
                return 6;
            default:
                return -1;
        }
    }
    private String obtenerZonaMasTrabajada(HashMap<String, Integer> zonas) {
        String mejorZona = "Sin datos";
        int max = 0;

        for (String zona : zonas.keySet()) {
            int cantidad = zonas.get(zona);

            if (cantidad > max) {
                max = cantidad;
                mejorZona = zona;
            }
        }

        return mejorZona;
    }
    private double[] calcularPromedioDolorUltimas3Semanas(List<SesionRutina> sesiones) {
        double[] suma = new double[3];
        int[] conteo = new int[3];

        Calendar hoy = Calendar.getInstance();

        for (SesionRutina sesion : sesiones) {
            if (sesion.getDolorDespues() <= 0) {
                continue;
            }

            long diferenciaMillis = hoy.getTimeInMillis() - sesion.getFechaMillis();
            long dias = diferenciaMillis / (1000L * 60L * 60L * 24L);

            int indiceSemana;

            if (dias <= 6) {
                indiceSemana = 2;
            } else if (dias <= 13) {
                indiceSemana = 1;
            } else if (dias <= 20) {
                indiceSemana = 0;
            } else {
                continue;
            }

            suma[indiceSemana] += sesion.getDolorDespues();
            conteo[indiceSemana]++;
        }

        double[] promedios = new double[3];

        for (int i = 0; i < 3; i++) {
            promedios[i] = conteo[i] > 0 ? suma[i] / conteo[i] : 0;
        }

        return promedios;
    }
    private String redondear(double valor) {
        return String.format(Locale.getDefault(), "%.1f", valor);
    }
}