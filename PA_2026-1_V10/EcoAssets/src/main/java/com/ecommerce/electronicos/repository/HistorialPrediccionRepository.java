package com.ecommerce.electronicos.repository;

import com.ecommerce.electronicos.model.HistorialPrediccion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HistorialPrediccionRepository extends MongoRepository<HistorialPrediccion, String> {
    List<HistorialPrediccion> findTop20ByOrderByFechaDesc();
}
