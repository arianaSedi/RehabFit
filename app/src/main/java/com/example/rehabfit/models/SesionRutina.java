package com.example.rehabfit.models;

public class SesionRutina {

    private long fechaMillis;
    private int minutosTotales;
    private int cantidadEjercicios;
    private int dolorAntes;
    private int dolorDespues;
    private String zonaPrincipal;

    public SesionRutina() {
    }
    public long getFechaMillis() {return fechaMillis;}
    public void setFechaMillis(long fechaMillis) {this.fechaMillis = fechaMillis;}
    public int getMinutosTotales() {return minutosTotales;}
    public void setMinutosTotales(int minutosTotales) {this.minutosTotales = minutosTotales;}
    public int getCantidadEjercicios() {return cantidadEjercicios;}
    public void setCantidadEjercicios(int cantidadEjercicios) {this.cantidadEjercicios = cantidadEjercicios;}
    public int getDolorAntes() {return dolorAntes;}
    public void setDolorAntes(int dolorAntes) {this.dolorAntes = dolorAntes;}
    public int getDolorDespues() {return dolorDespues;}
    public void setDolorDespues(int dolorDespues) {this.dolorDespues = dolorDespues;}
    public String getZonaPrincipal() {return zonaPrincipal;}
    public void setZonaPrincipal(String zonaPrincipal) {this.zonaPrincipal = zonaPrincipal;}
}
