package com.ecommerce.electronicos.service;

import com.ecommerce.electronicos.model.Dispositivo;
import com.ecommerce.electronicos.model.TipoUsuario;
import com.ecommerce.electronicos.model.Usuario;
import com.ecommerce.electronicos.repository.DispositivoRepository;
import com.ecommerce.electronicos.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    
    private final DispositivoRepository dispositivoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public long contarPorTipo(TipoUsuario tipo) {
    return usuarioRepository.countByTipo(tipo);
    }
    
    public UsuarioService(UsuarioRepository usuarioRepository, DispositivoRepository dispositivoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.dispositivoRepository = dispositivoRepository;
    }
    
    public Usuario crearUsuario(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizarUsuario(Usuario usuario) {
        // guardamos los datos del usuario actualizados
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        // busca dispositivos de este usuario y actualiz info incrustada
        List<Dispositivo> dispositivos = dispositivoRepository.findByVendedorId(usuario.getId());
        for (Dispositivo d : dispositivos) {
            d.setVendedor(usuarioGuardado);
            dispositivoRepository.save(d);
        }
        
        return usuarioGuardado;
    }
    
    public List<Usuario> obtenerTodos() {
        // obtener lista de usuarios desde la base de datos
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        // llenar lista de dispositivos de cada usuario
        for (Usuario usuario : usuarios) {
            List<Dispositivo> susDispositivos = dispositivoRepository.findByVendedorId(usuario.getId());
            usuario.setDispositivosPublicados(susDispositivos);
        }
        
        return usuarios;
    }
    
    public Optional<Usuario> obtenerPorId(String id) {
        return usuarioRepository.findById(id);
    }
    
    // NUEVO MÉTODO
    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    public void eliminarUsuario(String id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            // Actualizado para buscar usando el método por ID
            List<Dispositivo> dispositivos = dispositivoRepository.findByVendedorId(id);
            for (Dispositivo d : dispositivos) {
                dispositivoRepository.deleteById(d.getId());
            }
            usuarioRepository.deleteById(id);
        }
    }
    
    public List<Usuario> obtenerUsuariosNormales() {
        // obtener solo los usuarios normales desde la base de datos
        List<Usuario> usuarios = usuarioRepository.findUsuariosNormales();
        
        // Llenar lista de dispositivos para que el HTML la pueda contar
        for (Usuario usuario : usuarios) {
            List<Dispositivo> susDispositivos = dispositivoRepository.findByVendedorId(usuario.getId());
            usuario.setDispositivosPublicados(susDispositivos);
        }
        
        return usuarios;
    }
    
    public List<Usuario> obtenerAdministradores() {
        // obtener admins desde la base de datos
        List<Usuario> usuarios = usuarioRepository.findAdministradores();
        
        // Llenar lista de dispositivos para que el HTML la pueda contar
        for (Usuario usuario : usuarios) {
            List<Dispositivo> susDispositivos = dispositivoRepository.findByVendedorId(usuario.getId());
            usuario.setDispositivosPublicados(susDispositivos);
        }
        
        return usuarios;
    }
    
    public void agregarDispositivoAUsuario(String usuarioId, Dispositivo dispositivo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            dispositivo.setVendedor(usuario);
            Dispositivo guardado = dispositivoRepository.save(dispositivo);
            usuario.getDispositivosPublicados().add(guardado);
            usuarioRepository.save(usuario);
        }
    }

    public void registrarNuevoUsuario(Usuario usuario) {
        // Encriptar la contraseña antes de guardarla
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        // Asignar rol por defecto
        usuario.setTipo(TipoUsuario.USUARIO);
        
        // Guardar en la base de datos
        usuarioRepository.save(usuario);
    }

    public Page<Usuario> obtenerUsuariosNormalesPagina(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    
    // Traemos de MongoDB únicamente los 30 usuarios correspondientes a la página actual
    Page<Usuario> usuariosPagina = usuarioRepository.findByTipo(TipoUsuario.USUARIO, pageable);
    
    // OPTIMIZACIÓN DE ORO: Solo llenamos la lista de dispositivos de los 30 vendedores de esta página
    for (Usuario usuario : usuariosPagina.getContent()) {
        List<Dispositivo> susDispositivos = dispositivoRepository.findByVendedorId(usuario.getId());
        usuario.setDispositivosPublicados(susDispositivos);
    }
    
    return usuariosPagina;
    }

    public Page<Usuario> obtenerTodosPaginado(Pageable pageable) {
    Page<Usuario> usuariosPagina = usuarioRepository.findAll(pageable);
    for (Usuario usuario : usuariosPagina.getContent()) {
        List<Dispositivo> susDispositivos = dispositivoRepository.findByVendedorId(usuario.getId());
        usuario.setDispositivosPublicados(susDispositivos);
    }
    return usuariosPagina;
}
}
