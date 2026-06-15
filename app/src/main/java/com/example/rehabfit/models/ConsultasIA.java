package com.example.rehabfit.models;

import java.util.List;

public class ConsultasIA {

    private String id;
    private String consulta;
    private String recomendacion;
    private String movilidad;
    private String objetivo;
    private String apoyoFisico;
    private int dolorActual;
    private long fechaMillis;
    private List<Ejercicio> ejerciciosRecomendados;

    public ConsultasIA() {
    }

    public ConsultasIA(String id, String consulta, String recomendacion,
                       String movilidad, String objetivo, String apoyoFisico,
                       int dolorActual, long fechaMillis,
                       List<Ejercicio> ejerciciosRecomendados) {
        this.id = id;
        this.consulta = consulta;
        this.recomendacion = recomendacion;
        this.movilidad = movilidad;
        this.objetivo = objetivo;
        this.apoyoFisico = apoyoFisico;
        this.dolorActual = dolorActual;
        this.fechaMillis = fechaMillis;
        this.ejerciciosRecomendados = ejerciciosRecomendados;
    }

    public String getId() {
        return id;
    }

    public String getConsulta() {
        return consulta;
    }

    public String getRecomendacion() {
        return recomendacion;
    }

    public String getMovilidad() {
        return movilidad;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public String getApoyoFisico() {
        return apoyoFisico;
    }

    public int getDolorActual() {
        return dolorActual;
    }

    public long getFechaMillis() {
        return fechaMillis;
    }

    public List<Ejercicio> getEjerciciosRecomendados() {
        return ejerciciosRecomendados;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setConsulta(String consulta) {
        this.consulta = consulta;
    }

    public void setRecomendacion(String recomendacion) {
        this.recomendacion = recomendacion;
    }

    public void setMovilidad(String movilidad) {
        this.movilidad = movilidad;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public void setApoyoFisico(String apoyoFisico) {
        this.apoyoFisico = apoyoFisico;
    }

    public void setDolorActual(int dolorActual) {
        this.dolorActual = dolorActual;
    }

    public void setFechaMillis(long fechaMillis) {
        this.fechaMillis = fechaMillis;
    }

    public void setEjerciciosRecomendados(List<Ejercicio> ejerciciosRecomendados) {
        this.ejerciciosRecomendados = ejerciciosRecomendados;
    }
}