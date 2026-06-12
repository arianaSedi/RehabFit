package com.example.rehabfit.models;

import java.util.List;

public class EjercicioResponse {

    private int total;
    private List<Ejercicio> ejercicios;

    public EjercicioResponse() {
    }

    public int getTotal() {
        return total;
    }

    public List<Ejercicio> getEjercicios() {
        return ejercicios;
    }
}
