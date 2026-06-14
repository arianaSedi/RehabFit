package com.example.rehabfit.models;

import java.util.List;

public class IAResponse {

    private boolean ok;
    private String recomendacion;
    private List<Ejercicio> ejerciciosRecomendados;

    public IAResponse() {
    }

    public IAResponse(boolean ok, String recomendacion, List<Ejercicio> ejerciciosRecomendados) {
        this.ok = ok;
        this.recomendacion = recomendacion;
        this.ejerciciosRecomendados = ejerciciosRecomendados;
    }
    public boolean isOk() {return ok;}
    public String getRecomendacion() {return recomendacion;}
    public List<Ejercicio> getEjerciciosRecomendados() {return ejerciciosRecomendados;}
    public void setOk(boolean ok) {this.ok = ok;}
    public void setRecomendacion(String recomendacion) {this.recomendacion = recomendacion;}
    public void setEjerciciosRecomendados(List<Ejercicio> ejerciciosRecomendados) {this.ejerciciosRecomendados = ejerciciosRecomendados;}
}