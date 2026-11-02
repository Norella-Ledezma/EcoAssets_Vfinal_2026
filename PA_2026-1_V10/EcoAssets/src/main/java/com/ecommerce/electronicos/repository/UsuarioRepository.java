package com.ecommerce.electronicos.repository;

import com.ecommerce.electronicos.model.TipoUsuario;
import com.ecommerce.electronicos.model.Usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    Page<Usuario> findByTipo(TipoUsuario tipo, Pageable pageable);
 
    Optional<Usuario> findByEmail(String email);
 
    boolean existsByEmail(String email);
 
    long countByTipo(TipoUsuario tipo);

    List<Usuario> findByTipo(TipoUsuario tipo);
 
    default List<Usuario> findUsuariosNormales() {
        return findByTipo(TipoUsuario.USUARIO);
    }
 
    default List<Usuario> findAdministradores() {
        return findByTipo(TipoUsuario.ADMINISTRADOR);
    }
}
