package com.ecommerce.electronicos.repository;

import com.ecommerce.electronicos.model.Notificacion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificacionRepository extends MongoRepository<Notificacion, String> {

    List<Notificacion> findByVendedorIdOrderByFechaVentaDesc(String vendedorId);

    List<Notificacion> findByVendedorIdAndLeidaFalse(String vendedorId);

    long countByVendedorIdAndLeidaFalse(String vendedorId);
}
