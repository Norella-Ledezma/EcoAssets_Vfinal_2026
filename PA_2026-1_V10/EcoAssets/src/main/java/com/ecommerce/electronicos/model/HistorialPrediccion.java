package com.ecommerce.electronicos.model;

import com.ecommerce.electronicos.dto.PrediccionRequest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/** Registro de cada predicción hecha desde el panel de administración (como el historial del video). */
@Document(collection = "historial_prediccion")
public class HistorialPrediccion {

    @Id
    private String id;
    private LocalDateTime fecha = LocalDateTime.now();
    private String usuario;                    // email del administrador que la hizo
    private PrediccionRequest entrada;         // datos que se ingresaron
    private double prediccionCop;
    private double rangoMinCop;
    private double rangoMaxCop;
    private String modelo;

    public HistorialPrediccion() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public PrediccionRequest getEntrada() { return entrada; }
    public void setEntrada(PrediccionRequest entrada) { this.entrada = entrada; }
    public double getPrediccionCop() { return prediccionCop; }
    public void setPrediccionCop(double prediccionCop) { this.prediccionCop = prediccionCop; }
    public double getRangoMinCop() { return rangoMinCop; }
    public void setRangoMinCop(double rangoMinCop) { this.rangoMinCop = rangoMinCop; }
    public double getRangoMaxCop() { return rangoMaxCop; }
    public void setRangoMaxCop(double rangoMaxCop) { this.rangoMaxCop = rangoMaxCop; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
}
