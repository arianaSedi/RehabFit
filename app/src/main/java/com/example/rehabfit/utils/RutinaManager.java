package com.example.rehabfit.utils;

import com.example.rehabfit.models.Ejercicio;

import java.util.ArrayList;
import java.util.List;

public class RutinaManager {

    private static final List<Ejercicio> ejerciciosRutina = new ArrayList<>();

    public static void agregarEjercicio(Ejercicio ejercicio) {
        for (Ejercicio item : ejerciciosRutina) {
            if (item.getId() == ejercicio.getId()) {
                return;
            }
        }

        ejerciciosRutina.add(ejercicio);
    }

    public static void eliminarEjercicio(Ejercicio ejercicio) {
        ejerciciosRutina.remove(ejercicio);
    }

    public static List<Ejercicio> obtenerRutina() {
        return ejerciciosRutina;
    }

    public static int obtenerTotalMinutos() {
        int total = 0;

        for (Ejercicio ejercicio : ejerciciosRutina) {
            total += ejercicio.getDuracionMinutos();
        }

        return total;
    }

    public static void limpiarRutina() {
        ejerciciosRutina.clear();
    }
}
