package com.example.rehabfit.models;

public class Ejercicio {

    private int id;
    private String nombre;
    private String zona;
    private String nivel;
    private String posicion;
    private int duracionMinutos;
    private int repeticiones;
    private String descripcion;
    private String advertencia;

    public Ejercicio() {
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getZona() {
        return zona;
    }

    public String getNivel() {
        return nivel;
    }

    public String getPosicion() {
        return posicion;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public int getRepeticiones() {
        return repeticiones;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getAdvertencia() {
        return advertencia;
    }
}
