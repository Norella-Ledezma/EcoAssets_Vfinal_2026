package com.ecommerce.electronicos.dto;

/** Resultado de una predicción: valor estimado y un rango aproximado basado en el error de prueba del modelo. */
public class PrediccionResponse {

    private String categoria;
    private int mesDelAnio;
    private double prediccionCop;
    private double rangoMinCop;
    private double rangoMaxCop;
    private String modelo;

    public PrediccionResponse() {
    }

    public PrediccionResponse(String categoria, int mesDelAnio, double prediccionCop,
                              double rangoMinCop, double rangoMaxCop, String modelo) {
        this.categoria = categoria;
        this.mesDelAnio = mesDelAnio;
        this.prediccionCop = prediccionCop;
        this.rangoMinCop = rangoMinCop;
        this.rangoMaxCop = rangoMaxCop;
        this.modelo = modelo;
    }

    public String getCategoria() { return categoria; }
    public int getMesDelAnio() { return mesDelAnio; }
    public double getPrediccionCop() { return prediccionCop; }
    public double getRangoMinCop() { return rangoMinCop; }
    public double getRangoMaxCop() { return rangoMaxCop; }
    public String getModelo() { return modelo; }
}
