package com.ecommerce.electronicos.model;

import org.springframework.data.annotation.Transient;

/**
 * Copia de un producto tal como estaba al momento de la compra.
 * La factura se arma con esto (y no con el Dispositivo vivo) para que no cambie
 * si el vendedor edita el precio o elimina la publicación después.
 */
public class LineaPedido {

    private String nombre;
    private String marca;
    private String modelo;
    private String estado;
    private String categoria;
    private String vendedor;
    private double precio;

    public LineaPedido() {
    }

    public LineaPedido(Dispositivo d) {
        this.nombre = d.getNombre();
        this.marca = d.getMarca();
        this.modelo = d.getModelo();
        this.estado = d.getEstado();
        this.categoria = d.getCategoria();
        this.vendedor = d.getVendedor() != null ? d.getVendedor().getNombre() : null;
        this.precio = d.getPrecio();
    }

    @Transient
    public String getMarcaModelo() {
        String m = marca != null ? marca : "";
        String mo = modelo != null ? modelo : "";
        return (m + " " + mo).trim();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
}
