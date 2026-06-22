package com.example.rehabfit.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rehabfit.R;
import com.example.rehabfit.models.SesionRutina;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.github.mikephil.charting.charts.BarChart;

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
    private BarChart barChartSemana;
    private BarChart barChartDolor;
    private TextView txtMensajeProgreso;

    public ProgresoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_progreso, container, false);

        txtSesionesCompletadas = vista.findViewById(R.id.txtSesionesCompletadas);
        txtTiempoTerapia = vista.findViewById(R.id.txtTiempoTerapia);
        txtZonaMasTrabajada = vista.findViewById(R.id.txtZonaMasTrabajada);
        txtDolorPromedioProgreso = vista.findViewById(R.id.txtDolorPromedioProgreso);
        barChartSemana = vista.findViewById(R.id.barChartSemana);
        barChartDolor = vista.findViewById(R.id.barChartDolor);
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

        txtSesionesCompletadas.setText(totalSesiones + "\nSesiones completas");
        txtTiempoTerapia.setText(totalMinutos + " min\nTiempo de terapia");
        txtZonaMasTrabajada.setText(zonaMasTrabajada + "\nZona más trabajada");
        txtDolorPromedioProgreso.setText(String.format(Locale.getDefault(), "%.1f\nDolor promedio", promedioDolor));

        mostrarGraficaSemana(sesionesSemana);
        double[] promedios = calcularPromedioDolor(sesiones);
        mostrarGraficaDolor(promedios);
        mostrarMensajeProgreso(promedios);
    }
    private void mostrarMensajeProgreso(double[] promedios) {

        double dolorInicial = promedios[0];
        double dolorActual = promedios[3];

        if (dolorInicial <= 0 || dolorActual <= 0) {

            txtMensajeProgreso.setText("Aún no hay suficientes datos de dolor para evaluar tu progreso.");
            return;
        }

        double disminucion = dolorInicial - dolorActual;

        if (disminucion > 0) {
            txtMensajeProgreso.setText("¡Excelente progreso!\nReduciste tu dolor promedio de " + redondear(dolorInicial) + " a " + redondear(dolorActual) + ".");

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
    private double[] calcularPromedioDolor(List<SesionRutina> sesiones) {

        double[] suma = new double[4];
        int[] conteo = new int[4];

        Calendar hoy = Calendar.getInstance();

        for (SesionRutina sesion : sesiones) {

            if (sesion.getDolorDespues() <= 0) {
                continue;
            }

            long diferenciaMillis =
                    hoy.getTimeInMillis() - sesion.getFechaMillis();

            long dias =
                    diferenciaMillis / (1000L * 60L * 60L * 24L);

            int indiceSemana;

            if (dias <= 6) {
                indiceSemana = 3; // semana actual
            }
            else if (dias <= 13) {
                indiceSemana = 2;
            }
            else if (dias <= 20) {
                indiceSemana = 1;
            }
            else if (dias <= 27) {
                indiceSemana = 0;
            }
            else {
                continue;
            }

            suma[indiceSemana] += sesion.getDolorDespues();
            conteo[indiceSemana]++;
        }

        double[] promedios = new double[4];

        for (int i = 0; i < 4; i++) {

            if (conteo[i] > 0) {
                promedios[i] = suma[i] / conteo[i];
            } else {
                promedios[i] = 0;
            }
        }

        return promedios;
    }
    private String redondear(double valor) {
        return String.format(Locale.getDefault(), "%.1f", valor);
    }

    private void mostrarGraficaSemana(int[] datos) {

        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < datos.length; i++) {
            entries.add(new BarEntry(i, datos[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.verde_principal));
        dataSet.setDrawValues(false);

        BarData data = new BarData(dataSet);
        barChartSemana.setData(data);

        String[] dias = {"L","M","X","J","V","S","D"};

        XAxis xAxis = barChartSemana.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dias));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        barChartSemana.getDescription().setEnabled(false);
        barChartSemana.getLegend().setEnabled(false);
        barChartSemana.getAxisRight().setEnabled(false);
        barChartSemana.invalidate();
    }

    private void mostrarGraficaDolor(double[] promedios) {

        ArrayList<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(0, (float) promedios[0]));
        entries.add(new BarEntry(1, (float) promedios[1]));
        entries.add(new BarEntry(2, (float) promedios[2]));
        entries.add(new BarEntry(3, (float) promedios[3]));

        BarDataSet dataSet = new BarDataSet(entries, "");

        dataSet.setColors(
                Color.parseColor("#FF8A65"),
                Color.parseColor("#FFB74D"),
                Color.parseColor("#81C784"),
                Color.parseColor("#00C897")
        );

        dataSet.setValueTextSize(12f);
        BarData data = new BarData(dataSet);
        barChartDolor.setData(data);

        String[] semanas = {"Sem 1", "Sem 2", "Sem 3", "Sem 4"};

        XAxis xAxis = barChartDolor.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(semanas));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        barChartDolor.getDescription().setEnabled(false);
        barChartDolor.getLegend().setEnabled(false);
        barChartDolor.getAxisRight().setEnabled(false);
        barChartDolor.animateY(1000);
        barChartDolor.invalidate();
    }
}