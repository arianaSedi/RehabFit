package com.example.rehabfit.models;

public class PublicacionComunidad {

    private String id;
    private String uid;
    private String nombreUsuario;
    private String fecha;
    private String ejercicio;
    private String zona;
    private String duracion;
    private String dificultad;
    private String experiencia;
    private int likes;
    private long timestamp;

    public PublicacionComunidad() {
    }

    public PublicacionComunidad(String id, String uid, String nombreUsuario, String fecha,
                                String ejercicio, String zona, String duracion,
                                String dificultad, String experiencia, int likes, long timestamp) {
        this.id = id;
        this.uid = uid;
        this.nombreUsuario = nombreUsuario;
        this.fecha = fecha;
        this.ejercicio = ejercicio;
        this.zona = zona;
        this.duracion = duracion;
        this.dificultad = dificultad;
        this.experiencia = experiencia;
        this.likes = likes;
        this.timestamp = timestamp;
    }

    public String getId() {return id;}
    public String getUid() {return uid;}
    public String getNombreUsuario() {return nombreUsuario;}
    public String getFecha() {return fecha;}
    public String getEjercicio() {return ejercicio;}
    public String getZona() {return zona;}
    public String getDuracion() {return duracion;}
    public String getDificultad() {return dificultad;}
    public String getExperiencia() {return experiencia;}
    public int getLikes() {return likes;}
    public long getTimestamp() {return timestamp;}
    public void setId(String id) {this.id = id;}
    public void setUid(String uid) {this.uid = uid;}
    public void setNombreUsuario(String nombreUsuario) {this.nombreUsuario = nombreUsuario;}
    public void setFecha(String fecha) {this.fecha = fecha;}
    public void setEjercicio(String ejercicio) {this.ejercicio = ejercicio;}
    public void setZona(String zona) {this.zona = zona;}
    public void setDuracion(String duracion) {this.duracion = duracion;}
    public void setDificultad(String dificultad) {this.dificultad = dificultad;}
    public void setExperiencia(String experiencia) {this.experiencia = experiencia;}
    public void setLikes(int likes) {this.likes = likes;}
    public void setTimestamp(long timestamp) {this.timestamp = timestamp;}
}