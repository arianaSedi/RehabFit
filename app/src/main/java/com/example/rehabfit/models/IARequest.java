package com.example.rehabfit.models;

public class IARequest {

    private String uid;
    private String consulta;
    private String movilidad;
    private String objetivo;
    private String apoyoFisico;
    private int dolorActual;

    public IARequest() {
        // Constructor vacío requerido por Firebase/Retrofit
    }

    public IARequest(String uid, String consulta, String movilidad, String objetivo, String apoyoFisico, int dolorActual) {
        this.uid = uid;
        this.consulta = consulta;
        this.movilidad = movilidad;
        this.objetivo = objetivo;
        this.apoyoFisico = apoyoFisico;
        this.dolorActual = dolorActual;
    }

    public String getUid() {return uid;}
    public String getConsulta() {return consulta;}
    public String getMovilidad() {return movilidad;}
    public String getObjetivo() {return objetivo;}
    public String getApoyoFisico() {return apoyoFisico;}
    public int getDolorActual() {return dolorActual;}
    public void setUid(String uid) {this.uid = uid;}
    public void setConsulta(String consulta) {this.consulta = consulta;}
    public void setMovilidad(String movilidad) {this.movilidad = movilidad;}
    public void setObjetivo(String objetivo) {this.objetivo = objetivo;}
    public void setApoyoFisico(String apoyoFisico) {this.apoyoFisico = apoyoFisico;}
    public void setDolorActual(int dolorActual) {this.dolorActual = dolorActual;}
}
