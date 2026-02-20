package com.example.entrevistasapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Entrevista {
    private String id; // doc id en Firestore
    private int idOrden;
    private String descripcion;
    private String periodista;
    private Timestamp fecha;
    private String imageUrl;
    private String audioUrl;

    public Entrevista() {} // necesario para Firestore

    public Entrevista(String id, int idOrden, String descripcion, String periodista, Timestamp fecha, String imageUrl, String audioUrl) {
        this.id = id;
        this.idOrden = idOrden;
        this.descripcion = descripcion;
        this.periodista = periodista;
        this.fecha = fecha;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
    }

    // getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getIdOrden() { return idOrden; }
    public void setIdOrden(int idOrden) { this.idOrden = idOrden; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getPeriodista() { return periodista; }
    public void setPeriodista(String periodista) { this.periodista = periodista; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
}
