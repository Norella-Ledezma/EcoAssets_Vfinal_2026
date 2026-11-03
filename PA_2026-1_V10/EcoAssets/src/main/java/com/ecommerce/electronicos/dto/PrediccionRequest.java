package com.ecommerce.electronicos.dto;

/**
 * Datos que se le piden al administrador para estimar las ventas del mes de una categoría.
 * Los montos van en pesos (COP) y los valores "mes anterior" son los del mes inmediatamente anterior.
 */
public class PrediccionRequest {

    private String categoria;
    private int mesDelAnio;                          // 1 a 12 (mes que se quiere predecir)
    private Integer festivosMes;                     // opcional: si viene vacío se usa un valor típico del mes
    private double ventasMesAnterior;
    private double publicacionesActivasInicio;       // inventario disponible el día 1 del mes
    private double publicacionesNuevasMesAnterior;
    private double vendedoresActivosMesAnterior;
    private double precioPromedioMesAnterior;
    private double proporcionReacondicionadosMesAnterior;   // entre 0 y 1

    public PrediccionRequest() {
    }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public int getMesDelAnio() { return mesDelAnio; }
    public void setMesDelAnio(int mesDelAnio) { this.mesDelAnio = mesDelAnio; }
    public Integer getFestivosMes() { return festivosMes; }
    public void setFestivosMes(Integer festivosMes) { this.festivosMes = festivosMes; }
    public double getVentasMesAnterior() { return ventasMesAnterior; }
    public void setVentasMesAnterior(double ventasMesAnterior) { this.ventasMesAnterior = ventasMesAnterior; }
    public double getPublicacionesActivasInicio() { return publicacionesActivasInicio; }
    public void setPublicacionesActivasInicio(double v) { this.publicacionesActivasInicio = v; }
    public double getPublicacionesNuevasMesAnterior() { return publicacionesNuevasMesAnterior; }
    public void setPublicacionesNuevasMesAnterior(double v) { this.publicacionesNuevasMesAnterior = v; }
    public double getVendedoresActivosMesAnterior() { return vendedoresActivosMesAnterior; }
    public void setVendedoresActivosMesAnterior(double v) { this.vendedoresActivosMesAnterior = v; }
    public double getPrecioPromedioMesAnterior() { return precioPromedioMesAnterior; }
    public void setPrecioPromedioMesAnterior(double v) { this.precioPromedioMesAnterior = v; }
    public double getProporcionReacondicionadosMesAnterior() { return proporcionReacondicionadosMesAnterior; }
    public void setProporcionReacondicionadosMesAnterior(double v) { this.proporcionReacondicionadosMesAnterior = v; }
}
