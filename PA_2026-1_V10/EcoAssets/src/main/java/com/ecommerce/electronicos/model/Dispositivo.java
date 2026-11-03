package com.ecommerce.electronicos.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document (collection = "dispositivo")
public class Dispositivo {

    @Id
    private String id;

    @Field("nombre")
    private String nombre;

    @Field("marca")
    private String marca;

    @Field("modelo")
    private String modelo;

    @Field("descripcion")
    private String descripcion;

    @Field("precio")
    private double precio;

    @Field("categoria")
    private String categoria;

    @Field("estado")
    private String estado; // Usado, Reacondicionado

    @Field("imagenUrl")
    private String imagenUrl;

    @Field("fechaPublicacion")
    private LocalDateTime fechaPublicacion;

    @DBRef
    @Field("vendedor")
    private Usuario vendedor;

    @Field("departamento")
    private String departamento; // Ej: Cundinamarca, Antioquia, Bolívar

    @Field("municipio")
    private String municipio;

    @Field("stock")
    private int stock = 1;
    
    public Dispositivo() {
        this.fechaPublicacion = LocalDateTime.now();
    }

    @Field("disponible")
    private boolean disponible = true; // true = en venta, false = vendido
    
    public Dispositivo(String id, String nombre, String marca, String modelo, 
                       String descripcion, double precio, String categoria, 
                       String estado, String imagenUrl, Usuario vendedor,
                       String departamento, String municipio,int stock, boolean disponible) {
        this();
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.modelo = modelo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.estado = estado;
        this.imagenUrl = imagenUrl;
        this.vendedor = vendedor;
        this.departamento = departamento;
        this.municipio = municipio;
        this.disponible = disponible;
        this.stock = stock;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    
    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    
    public Usuario getVendedor() { return vendedor; }
    public void setVendedor(Usuario vendedor) { this.vendedor = vendedor; }
    
    public String getPrecioFormateado() {
        return String.format("%,.2f", precio);
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public boolean isDisponible() {
        return disponible;
    }
    
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public int getStock() {
        return stock;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
    }

    
}
