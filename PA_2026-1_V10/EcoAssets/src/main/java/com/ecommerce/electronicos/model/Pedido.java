package com.ecommerce.electronicos.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "pedido")
public class Pedido {
    @Id
    private String id;
    
    private LocalDateTime fecha;
    
    @DBRef
    private Usuario comprador;
    
    @DBRef
    private List<Dispositivo> productos;

    // Copia de los productos al momento de comprar (base de la factura)
    private List<LineaPedido> lineas;
    
    private double total;

    public Pedido() {
        this.fecha = LocalDateTime.now();
    }

    /** Número que se muestra en la factura, ej: EA-2026-3F9A1C (solo visual, la ruta usa el id completo). */
    @Transient
    public String getNumeroFactura() {
        String sufijo = (id != null && id.length() >= 6)
                ? id.substring(id.length() - 6).toUpperCase()
                : "000000";
        int anio = (fecha != null ? fecha : LocalDateTime.now()).getYear();
        return "EA-" + anio + "-" + sufijo;
    }

    /**
     * Ítems de la factura. Usa la copia guardada al comprar; si es un pedido viejo
     * (anterior a las facturas) la arma desde los productos, ignorando los que ya no existan.
     */
    @Transient
    public List<LineaPedido> getLineasFactura() {
        if (lineas != null && !lineas.isEmpty()) {
            return lineas;
        }
        List<LineaPedido> resultado = new ArrayList<>();
        if (productos != null) {
            for (Dispositivo d : productos) {
                if (d != null) {
                    resultado.add(new LineaPedido(d));
                }
            }
        }
        return resultado;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public Usuario getComprador() { return comprador; }
    public void setComprador(Usuario comprador) { this.comprador = comprador; }
    public List<Dispositivo> getProductos() { return productos; }
    public void setProductos(List<Dispositivo> productos) { this.productos = productos; }
    public List<LineaPedido> getLineas() { return lineas; }
    public void setLineas(List<LineaPedido> lineas) { this.lineas = lineas; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
