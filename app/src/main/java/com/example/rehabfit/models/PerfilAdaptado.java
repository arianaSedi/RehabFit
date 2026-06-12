package com.example.rehabfit.models;

public class PerfilAdaptado {
    private int edad;
    private String nivelMovilidad;
    private String apoyoFisico;
    private String objetivoPrincipal;
    private int nivelDolor;
    private boolean perfilCompletado;

    public PerfilAdaptado() {
        // Constructor
    }

    public PerfilAdaptado(int edad, String nivelMovilidad, String apoyoFisico,
                          String objetivoPrincipal, int nivelDolor, boolean perfilCompletado) {
        this.edad = edad;
        this.nivelMovilidad = nivelMovilidad;
        this.apoyoFisico = apoyoFisico;
        this.objetivoPrincipal = objetivoPrincipal;
        this.nivelDolor = nivelDolor;
        this.perfilCompletado = perfilCompletado;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getNivelMovilidad() {
        return nivelMovilidad;
    }

    public void setNivelMovilidad(String nivelMovilidad) {
        this.nivelMovilidad = nivelMovilidad;
    }

    public String getApoyoFisico() {
        return apoyoFisico;
    }

    public void setApoyoFisico(String apoyoFisico) {
        this.apoyoFisico = apoyoFisico;
    }

    public String getObjetivoPrincipal() {
        return objetivoPrincipal;
    }

    public void setObjetivoPrincipal(String objetivoPrincipal) {
        this.objetivoPrincipal = objetivoPrincipal;
    }

    public int getNivelDolor() {
        return nivelDolor;
    }

    public void setNivelDolor(int nivelDolor) {
        this.nivelDolor = nivelDolor;
    }

    public boolean isPerfilCompletado() {
        return perfilCompletado;
    }

    public void setPerfilCompletado(boolean perfilCompletado) {
        this.perfilCompletado = perfilCompletado;
    }
}
