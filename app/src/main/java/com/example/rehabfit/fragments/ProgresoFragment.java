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

        // infla el layout del fragment de progreso
        View vista = inflater.inflate(R.layout.fragment_progreso, container, false);

        // vincula los componentes visuales del layout
        txtSesionesCompletadas = vista.findViewById(R.id.txtSesionesCompletadas);
        txtTiempoTerapia = vista.findViewById(R.id.txtTiempoTerapia);
        txtZonaMasTrabajada = vista.findViewById(R.id.txtZonaMasTrabajada);
        txtDolorPromedioProgreso = vista.findViewById(R.id.txtDolorPromedioProgreso);
        barChartSemana = vista.findViewById(R.id.barChartSemana);
        barChartDolor = vista.findViewById(R.id.barChartDolor);
        txtMensajeProgreso = vista.findViewById(R.id.txtMensajeProgreso);

        // carga toda la informacion de progreso del usuario
        cargarProgreso();

        // devuelve la vista del fragment
        return vista;
    }

    // metodo encargado de cargar las sesiones del usuario desde firebase
    private void cargarProgreso() {

        // obtiene el usuario autenticado
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // valida que exista una sesion iniciada
        if (user == null) {
            Toast.makeText(requireContext(), "No hay usuario activo", Toast.LENGTH_SHORT).show();
            return;
        }

        // referencia al historial de sesiones del usuario
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(user.getUid())
                .child("sesiones");

        // obtiene todas las sesiones guardadas
        ref.get().addOnSuccessListener(snapshot -> {

            // lista donde se almacenaran las sesiones recuperadas
            List<SesionRutina> sesiones = new ArrayList<>();

            // recorre cada sesion encontrada
            for (DataSnapshot item : snapshot.getChildren()) {

                // convierte el registro en un objeto sesion rutina
                SesionRutina sesion = item.getValue(SesionRutina.class);

                // valida que la sesion exista
                if (sesion != null) {
                    sesiones.add(sesion);
                }
            }

            // muestra el resumen de progreso usando las sesiones obtenidas
            mostrarResumen(sesiones);

        }).addOnFailureListener(e -> {

            // valida que el fragment siga activo
            if (!isAdded()) {
                return;
            }

            // muestra mensaje si ocurre un error
            Toast.makeText(requireContext(), "Error al cargar progreso: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    // metodo encargado de calcular y mostrar el resumen general
    private void mostrarResumen(List<SesionRutina> sesiones) {

        // total de sesiones completadas
        int totalSesiones = sesiones.size();

        // total de minutos acumulados
        int totalMinutos = 0;

        // acumulador del dolor registrado
        double sumaDolor = 0;

        // contador de registros de dolor validos
        int contadorDolor = 0;

        // almacena cuantas veces se ha trabajado cada zona
        HashMap<String, Integer> zonas = new HashMap<>();

        // almacena las sesiones realizadas por dia de la semana
        int[] sesionesSemana = new int[7];

        // recorre todas las sesiones registradas
        for (SesionRutina sesion : sesiones) {

            // acumula los minutos de terapia
            totalMinutos += sesion.getMinutosTotales();

            // toma en cuenta solo dolores mayores a cero
            if (sesion.getDolorDespues() > 0) {
                sumaDolor += sesion.getDolorDespues();
                contadorDolor++;
            }

            // obtiene la zona principal trabajada
            String zona = sesion.getZonaPrincipal();

            // cuenta cuantas veces aparece cada zona
            if (zona != null && !zona.isEmpty()) {
                zonas.put(zona, zonas.getOrDefault(zona, 0) + 1);
            }

            // verifica si la sesion pertenece a la semana actual
            if (estaEnSemanaActual(sesion.getFechaMillis())) {

                int dia = obtenerIndiceDia(sesion.getFechaMillis());

                // aumenta el contador del dia correspondiente
                if (dia >= 0 && dia < 7) {
                    sesionesSemana[dia]++;
                }
            }
        }

        // calcula el promedio de dolor
        double promedioDolor = contadorDolor > 0 ? sumaDolor / contadorDolor : 0.0;

        // obtiene la zona mas trabajada
        String zonaMasTrabajada = obtenerZonaMasTrabajada(zonas);

        // muestra los datos en pantalla
        txtSesionesCompletadas.setText(totalSesiones + "\nSesiones completas");
        txtTiempoTerapia.setText(totalMinutos + " min\nTiempo de terapia");
        txtZonaMasTrabajada.setText(zonaMasTrabajada + "\nZona más trabajada");
        txtDolorPromedioProgreso.setText(String.format(Locale.getDefault(), "%.1f\nDolor promedio", promedioDolor));

        // genera la grafica semanal
        mostrarGraficaSemana(sesionesSemana);

        // calcula los promedios de dolor por semana
        double[] promedios = calcularPromedioDolor(sesiones);

        // genera la grafica de evolucion del dolor
        mostrarGraficaDolor(promedios);

        // muestra mensaje personalizado de progreso
        mostrarMensajeProgreso(promedios);
    }

    // metodo encargado de generar un mensaje segun la evolucion del dolor
    private void mostrarMensajeProgreso(double[] promedios) {

        // obtiene el dolor mas antiguo registrado
        double dolorInicial = promedios[0];

        // obtiene el dolor de la semana actual
        double dolorActual = promedios[3];

        // valida que existan suficientes datos
        if (dolorInicial <= 0 || dolorActual <= 0) {

            txtMensajeProgreso.setText("Aún no hay suficientes datos de dolor para evaluar tu progreso.");
            return;
        }

        // calcula la diferencia entre ambos valores
        double disminucion = dolorInicial - dolorActual;

        // si el dolor disminuyo
        if (disminucion > 0) {

            txtMensajeProgreso.setText("¡Excelente progreso!\nReduciste tu dolor promedio de " + redondear(dolorInicial) + " a " + redondear(dolorActual) + ".");

            // si el dolor se mantiene igual
        } else if (disminucion == 0) {

            txtMensajeProgreso.setText("Tu dolor promedio se mantiene estable. Sigue registrando tus sesiones.");

            // si el dolor aumento
        } else {

            txtMensajeProgreso.setText("Tu dolor promedio aumentó. Si sientes molestias fuertes, consulta con un profesional.");
        }
    }

    // metodo que verifica si una fecha pertenece a la semana actual
    private boolean estaEnSemanaActual(long fechaMillis) {

        Calendar fecha = Calendar.getInstance();
        fecha.setTimeInMillis(fechaMillis);

        Calendar inicioSemana = Calendar.getInstance();

        int diaActual = inicioSemana.get(Calendar.DAY_OF_WEEK);

        int diferencia = (diaActual == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - diaActual;

        // posiciona el calendario en el inicio de la semana
        inicioSemana.add(Calendar.DAY_OF_MONTH, diferencia);
        inicioSemana.set(Calendar.HOUR_OF_DAY, 0);
        inicioSemana.set(Calendar.MINUTE, 0);
        inicioSemana.set(Calendar.SECOND, 0);
        inicioSemana.set(Calendar.MILLISECOND, 0);

        // valida si la fecha es posterior al inicio de semana
        return fecha.getTimeInMillis() >= inicioSemana.getTimeInMillis();
    }

    // metodo que convierte un dia de la semana en un indice del arreglo
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
    // metodo para obtener la zona corporal mas trabajada
    private String obtenerZonaMasTrabajada(HashMap<String, Integer> zonas) {

        String mejorZona = "Sin datos";
        int max = 0;

        // recorre todas las zonas registradas
        for (String zona : zonas.keySet()) {

            int cantidad = zonas.get(zona);

            // guarda la zona con mayor cantidad de apariciones
            if (cantidad > max) {
                max = cantidad;
                mejorZona = zona;
            }
        }

        return mejorZona;
    }
    // metodo para calcular el promedio de dolor de las ultimas cuatro semanas
    private double[] calcularPromedioDolor(List<SesionRutina> sesiones) {

        // arreglo para acumular la suma de dolor por semana
        double[] suma = new double[4];

        // arreglo para contar cuantas sesiones hay por semana
        int[] conteo = new int[4];

        // obtiene la fecha actual
        Calendar hoy = Calendar.getInstance();

        // recorre todas las sesiones
        for (SesionRutina sesion : sesiones) {

            // ignora sesiones sin dolor registrado
            if (sesion.getDolorDespues() <= 0) {
                continue;
            }

            // calcula la diferencia entre hoy y la fecha de la sesion
            long diferenciaMillis =
                    hoy.getTimeInMillis() - sesion.getFechaMillis();

            // convierte la diferencia de milisegundos a dias
            long dias =
                    diferenciaMillis / (1000L * 60L * 60L * 24L);

            // variable para ubicar la sesion en una de las cuatro semanas
            int indiceSemana;

            // sesiones de la semana actual
            if (dias <= 6) {
                indiceSemana = 3; // semana actual
            }

            // sesiones de la semana anterior
            else if (dias <= 13) {
                indiceSemana = 2;
            }

            // sesiones de hace dos semanas
            else if (dias <= 20) {
                indiceSemana = 1;
            }

            // sesiones de hace tres semanas
            else if (dias <= 27) {
                indiceSemana = 0;
            }

            // ignora sesiones mayores a cuatro semanas
            else {
                continue;
            }

            // suma el dolor en la semana correspondiente
            suma[indiceSemana] += sesion.getDolorDespues();

            // aumenta el conteo de sesiones de esa semana
            conteo[indiceSemana]++;
        }

        // arreglo donde se guardan los promedios finales
        double[] promedios = new double[4];

        // recorre las cuatro semanas
        for (int i = 0; i < 4; i++) {

            // calcula promedio solo si hay datos
            if (conteo[i] > 0) {
                promedios[i] = suma[i] / conteo[i];
            } else {

                // si no hay datos, el promedio queda en cero
                promedios[i] = 0;
            }
        }

        // devuelve los promedios de dolor
        return promedios;
    }

    // metodo para redondear un numero decimal a un decimal
    private String redondear(double valor) {

        // devuelve el valor con formato de un decimal
        return String.format(Locale.getDefault(), "%.1f", valor);
    }

    // metodo para mostrar la grafica de sesiones realizadas en la semana
    private void mostrarGraficaSemana(int[] datos) {

        // lista de entradas para la grafica
        ArrayList<BarEntry> entries = new ArrayList<>();

        // recorre los datos de cada dia
        for (int i = 0; i < datos.length; i++) {

            // agrega cada valor a la grafica
            entries.add(new BarEntry(i, datos[i]));
        }

        // crea el conjunto de datos para la grafica
        BarDataSet dataSet = new BarDataSet(entries, "");

        // asigna el color principal a las barras
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.verde_principal));

        // oculta los valores encima de las barras
        dataSet.setDrawValues(false);

        // crea los datos finales de la grafica
        BarData data = new BarData(dataSet);

        // asigna los datos a la grafica semanal
        barChartSemana.setData(data);

        // nombres cortos de los dias de la semana
        String[] dias = {"L","M","X","J","V","S","D"};

        // configura el eje x de la grafica
        XAxis xAxis = barChartSemana.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dias));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // oculta descripcion, leyenda y eje derecho
        barChartSemana.getDescription().setEnabled(false);
        barChartSemana.getLegend().setEnabled(false);
        barChartSemana.getAxisRight().setEnabled(false);

        // refresca la grafica
        barChartSemana.invalidate();
    }

    // metodo para mostrar la grafica del promedio de dolor por semanas
    private void mostrarGraficaDolor(double[] promedios) {

        // lista de entradas para la grafica
        ArrayList<BarEntry> entries = new ArrayList<>();

        // agrega el promedio de dolor de cada semana
        entries.add(new BarEntry(0, (float) promedios[0]));
        entries.add(new BarEntry(1, (float) promedios[1]));
        entries.add(new BarEntry(2, (float) promedios[2]));
        entries.add(new BarEntry(3, (float) promedios[3]));

        // crea el conjunto de datos para la grafica
        BarDataSet dataSet = new BarDataSet(entries, "");

        // define los colores de las barras por semana
        dataSet.setColors(
                Color.parseColor("#FF8A65"),
                Color.parseColor("#FFB74D"),
                Color.parseColor("#81C784"),
                Color.parseColor("#00C897")
        );

        // define el tamaño del texto de los valores
        dataSet.setValueTextSize(12f);

        // crea los datos finales de la grafica
        BarData data = new BarData(dataSet);

        // asigna los datos a la grafica de dolor
        barChartDolor.setData(data);

        // etiquetas de las ultimas cuatro semanas
        String[] semanas = {"Sem 1", "Sem 2", "Sem 3", "Sem 4"};

        // configura el eje x de la grafica
        XAxis xAxis = barChartDolor.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(semanas));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // oculta descripcion, leyenda y eje derecho
        barChartDolor.getDescription().setEnabled(false);
        barChartDolor.getLegend().setEnabled(false);
        barChartDolor.getAxisRight().setEnabled(false);

        // anima la grafica verticalmente
        barChartDolor.animateY(1000);

        // refresca la grafica
        barChartDolor.invalidate();
    }
}