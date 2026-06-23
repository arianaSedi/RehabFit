package com.example.rehabfit.models;

import java.io.Serializable;

public class Ejercicio implements Serializable {

    private int id;
    private String nombre;
    private String zona;
    private String nivel;
    private String posicion;
    private int duracionMinutos;
    private int repeticiones;
    private String descripcion;
    private String advertencia;
    private String imagen;

    public Ejercicio() {
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public String getZona() {return zona;}
    public void setZona(String zona) {this.zona = zona;}
    public String getNivel() {return nivel;}
    public void setNivel(String nivel) {this.nivel = nivel;}
    public String getPosicion() {return posicion;}
    public void setPosicion(String posicion) {this.posicion = posicion;}
    public int getDuracionMinutos() {return duracionMinutos;}
    public void setDuracionMinutos(int duracionMinutos) {this.duracionMinutos = duracionMinutos;}
    public int getRepeticiones() {return repeticiones;}
    public void setRepeticiones(int repeticiones) {this.repeticiones = repeticiones;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public String getAdvertencia() {return advertencia;}
    public void setAdvertencia(String advertencia) {this.advertencia = advertencia;}
    public String getImagen() {
        return imagen;
    }
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}