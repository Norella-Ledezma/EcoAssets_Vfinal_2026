package com.ecommerce.electronicos.service;

import com.ecommerce.electronicos.model.Dispositivo;
import com.ecommerce.electronicos.model.Usuario;
import com.ecommerce.electronicos.repository.DispositivoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import org.springframework.data.domain.Pageable;

@Service
public class DispositivoService {
    
    private final DispositivoRepository dispositivoRepository;
    private UsuarioService usuarioService;
    
    public DispositivoService(DispositivoRepository dispositivoRepository, UsuarioService usuarioService) {
        this.dispositivoRepository = dispositivoRepository;
        this.usuarioService = usuarioService;
    }
    
    public Dispositivo crearDispositivo(Dispositivo dispositivo) {
    // 1. Guardamos el dispositivo primero
    Dispositivo guardado = dispositivoRepository.save(dispositivo);
    
    // 2. Sincronizar con la lista del usuario
    if (guardado.getVendedor() != null) {
        Usuario vendedor = guardado.getVendedor();
        
        // Inicializar la lista si es nula para evitar NullPointerException
        if (vendedor.getDispositivosPublicados() == null) {
            vendedor.setDispositivosPublicados(new ArrayList<>());
        }

        if (!vendedor.getDispositivosPublicados().contains(guardado)) {
            vendedor.getDispositivosPublicados().add(guardado);
            // IMPORTANTE: Debes guardar al usuario para que MongoDB registre el cambio en el array
            usuarioService.actualizarUsuario(vendedor); 
        }
    }
    return guardado;
}
    public Dispositivo actualizarDispositivo(Dispositivo dispositivo) {
        return dispositivoRepository.save(dispositivo);
    }
    
    public List<Dispositivo> obtenerTodos() {
        return dispositivoRepository.findAll();
    }
    
    public Optional<Dispositivo> obtenerPorId(String id) {
        return dispositivoRepository.findById(id);
    }
    
    public void eliminarDispositivo(String id) {
        dispositivoRepository.deleteById(id);
    }
    
    public List<Dispositivo> obtenerPorVendedor(String vendedorId) {
        return dispositivoRepository.findByVendedorId(vendedorId);
    }
    
    public List<Dispositivo> buscarPorCategoria(String categoria) {
        return dispositivoRepository.findByCategoriaIgnoreCase(categoria);
    }
    
    public List<Dispositivo> buscarPorNombre(String query) {
        return dispositivoRepository.searchByNombre(query);
    }
    
    public List<String> obtenerCategorias() {
        return obtenerTodos().stream()
                .map(Dispositivo::getCategoria)
                .distinct()
                .toList();
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    // Súper método para búsqueda facetada (filtros dinámicos)

public Page<Dispositivo> buscarDispositivosConFiltros(
        String busqueda,
        String categoria, Double precioMin, Double precioMax, 
        String departamento, String municipio, String marca, 
        String ordenPrecio, int page, int size) {

    Query query = new Query();
    query.addCriteria(Criteria.where("disponible").is(true));

    if (busqueda != null && !busqueda.isEmpty()) {
        query.addCriteria(Criteria.where("nombre").regex(busqueda, "i"));
    }
    
    // filtros
    if (categoria != null && !categoria.isEmpty()) query.addCriteria(Criteria.where("categoria").is(categoria));
    if (marca != null && !marca.isEmpty()) query.addCriteria(Criteria.where("marca").regex(marca, "i"));
    if (departamento != null && !departamento.isEmpty()) query.addCriteria(Criteria.where("departamento").is(departamento));
    if (municipio != null && !municipio.isEmpty()) query.addCriteria(Criteria.where("municipio").regex(municipio, "i"));

    if (precioMin != null || precioMax != null) {
        Criteria criteriaPrecio = Criteria.where("precio");
        if (precioMin != null) criteriaPrecio.gte(precioMin);
        if (precioMax != null) criteriaPrecio.lte(precioMax);
        query.addCriteria(criteriaPrecio);
    }

    if (ordenPrecio != null && !ordenPrecio.isEmpty()) {
        if (ordenPrecio.equalsIgnoreCase("asc")) query.with(Sort.by(Sort.Direction.ASC, "precio"));
        else if (ordenPrecio.equalsIgnoreCase("desc")) query.with(Sort.by(Sort.Direction.DESC, "precio"));
    }

    // 1. Contar el total REAL de resultados antes de cortar
    long total = mongoTemplate.count(query, Dispositivo.class);

    // 2. Decirle a MongoDB exactamente qué página queremos
    Pageable pageable = PageRequest.of(page, size);
    query.with(pageable);

    // 3. MongoDB nos trae SOLO los 20 dispositivos de esa página (¡Ultra rápido!)
    List<Dispositivo> dispositivos = mongoTemplate.find(query, Dispositivo.class);

    return new org.springframework.data.domain.PageImpl<>(dispositivos, pageable, total);
}

    //funcion para quitar el stock cuando el comprador haya comprado un dispositivo
    public synchronized boolean quitarstock(String id, int cantidad){
        Optional<Dispositivo> opt = dispositivoRepository.findById(id);
        if (opt.isEmpty()) return false;
        //devuelve a false si no se alcanzan las cantidades
        Dispositivo d = opt.get();
        if(!d.isDisponible()|| d.getStock() < cantidad) return false;
        //si el stock llega a 0 el producto queda como no disponible
        d.setStock(d.getStock()-cantidad);
        if(d.getStock() <= 0){
            d.setDisponible (false);
        }
        dispositivoRepository.save(d);
        return true;
    }

    public List<Dispositivo> obtenerTodosDisponibles() {
        return dispositivoRepository.findByDisponibleTrue();
    }

    public List<Dispositivo> obtenerPorCategoria(String categoria) {
    
        return dispositivoRepository.findByCategoriaAndDisponibleTrue(categoria);
    }

    public Page<Dispositivo> obtenerPaginaDeDispositivos(int page, int size) {
    // Esto crea las instrucciones: "Quiero la página X, con Y cantidad de elementos"
    Pageable pageable = PageRequest.of(page, size);
    
    // Tu repositorio de Mongo ya trae el método findAll(pageable) oculto de fábrica
    return dispositivoRepository.findAll(pageable); 
    }

    
}
