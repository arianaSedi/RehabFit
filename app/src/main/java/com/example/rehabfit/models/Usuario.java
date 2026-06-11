package com.example.rehabfit.models;

public class Usuario {

    private String uid;
    private String nombre;
    private String correo;

    public Usuario() {
        // Constructor vacío requerido por Firebase
    }

    public Usuario(String uid, String nombre, String correo) {
        this.uid = uid;
        this.nombre = nombre;
        this.correo = correo;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
