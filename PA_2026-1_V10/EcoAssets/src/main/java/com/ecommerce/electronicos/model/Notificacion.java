package com.ecommerce.electronicos.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document(collection = "notificacion")
public class Notificacion {

    @Id
    private String id;

    private String vendedorId;
    private String pedidoId;

    // Producto vendido
    private String productoNombre;
    private String productoMarcaModelo;
    private double precio;

    // Contacto del cliente
    private String compradorNombre;
    private String compradorEmail;
    private String compradorTelefono;

    private LocalDateTime fechaVenta;
    private boolean leida = false;
    private boolean correoEnviado = false;

    public Notificacion() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVendedorId() { return vendedorId; }
    public void setVendedorId(String vendedorId) { this.vendedorId = vendedorId; }
    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getProductoMarcaModelo() { return productoMarcaModelo; }
    public void setProductoMarcaModelo(String productoMarcaModelo) { this.productoMarcaModelo = productoMarcaModelo; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public String getCompradorNombre() { return compradorNombre; }
    public void setCompradorNombre(String compradorNombre) { this.compradorNombre = compradorNombre; }
    public String getCompradorEmail() { return compradorEmail; }
    public void setCompradorEmail(String compradorEmail) { this.compradorEmail = compradorEmail; }
    public String getCompradorTelefono() { return compradorTelefono; }
    public void setCompradorTelefono(String compradorTelefono) { this.compradorTelefono = compradorTelefono; }
    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public boolean isCorreoEnviado() { return correoEnviado; }
    public void setCorreoEnviado(boolean correoEnviado) { this.correoEnviado = correoEnviado; }

    public String getPrecioFormateado() {
        return String.format("%,.2f", precio);
    }
}
