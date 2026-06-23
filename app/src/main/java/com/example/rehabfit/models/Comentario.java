package com.example.rehabfit.models;

public class Comentario {

    private String id;
    private String uid;
    private String nombreUsuario;
    private String texto;
    private String fecha;
    private long timestamp;

    public Comentario() {
    }

    public Comentario(String id, String uid, String nombreUsuario, String texto, String fecha, long timestamp) {
        this.id = id;
        this.uid = uid;
        this.nombreUsuario = nombreUsuario;
        this.texto = texto;
        this.fecha = fecha;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getUid() { return uid; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getTexto() { return texto; }
    public String getFecha() { return fecha; }
    public long getTimestamp() { return timestamp; }

    public void setId(String id) { this.id = id; }
    public void setUid(String uid) { this.uid = uid; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public void setTexto(String texto) { this.texto = texto; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
