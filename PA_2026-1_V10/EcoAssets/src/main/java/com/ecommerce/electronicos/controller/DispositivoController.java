package com.ecommerce.electronicos.controller;

import com.ecommerce.electronicos.model.Dispositivo;
import com.ecommerce.electronicos.model.Usuario;
import com.ecommerce.electronicos.service.DispositivoService;
import com.ecommerce.electronicos.service.UsuarioService;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dispositivos")
public class DispositivoController {
    
    private final DispositivoService dispositivoService;
    private final UsuarioService usuarioService;
    
    public DispositivoController(DispositivoService dispositivoService, UsuarioService usuarioService) {
        this.dispositivoService = dispositivoService;
        this.usuarioService = usuarioService;
    }
    
    @GetMapping({"","/"})
public String listarDispositivos(
        @RequestParam(required = false) String busqueda,
        @RequestParam(required = false) String categoria,
        @RequestParam(required = false) Double precioMin,
        @RequestParam(required = false) Double precioMax,
        @RequestParam(required = false) String departamento,
        @RequestParam(required = false) String municipio,
        @RequestParam(required = false) String marca,
        @RequestParam(required = false) String ordenPrecio,
        @RequestParam(defaultValue = "0") int page,
        Model model) {

    int cantidadPorPagina = 20;

    // Llamamos al nuevo servicio que hace todo el trabajo mágico
    Page<Dispositivo> pagina = dispositivoService.buscarDispositivosConFiltros(
            busqueda, categoria, precioMin, precioMax, departamento, municipio, marca, ordenPrecio, page, cantidadPorPagina);

    // Mandamos los datos a la vista
    model.addAttribute("dispositivos", pagina.getContent());
    model.addAttribute("totalDispositivos", pagina.getTotalElements());
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", pagina.getTotalPages());

    // Mantenemos los filtros para que los botones de HTML tengan memoria
    model.addAttribute("categorias", List.of("Smartphones", "Laptops", "Tablets", "Consolas", "Audio", "Televisores", "Accesorios", "Otros"));
    model.addAttribute("categoriaSeleccionada", categoria);
    model.addAttribute("precioMinSeleccionado", precioMin);
    model.addAttribute("precioMaxSeleccionado", precioMax);
    model.addAttribute("departamentoSeleccionado", departamento);
    model.addAttribute("municipioSeleccionado", municipio);
    model.addAttribute("marcaSeleccionada", marca);
    model.addAttribute("ordenSeleccionado", ordenPrecio);
    model.addAttribute("busqueda", busqueda);

    // Cuántos filtros del panel lateral están activos (para el contador del botón "Filtros")
    int filtrosActivos = 0;
    if (marca != null && !marca.isBlank()) filtrosActivos++;
    if (precioMin != null) filtrosActivos++;
    if (precioMax != null) filtrosActivos++;
    if (departamento != null && !departamento.isBlank()) filtrosActivos++;
    if (municipio != null && !municipio.isBlank()) filtrosActivos++;
    if (ordenPrecio != null && !ordenPrecio.isBlank()) filtrosActivos++;
    model.addAttribute("filtrosActivos", filtrosActivos);

    return "dispositivos/lista";
}


    @GetMapping("/{id}")
public String verDetalleDispositivo(@PathVariable String id, Model model) {
    // Usamos el servicio para buscar el dispositivo por ID
    Optional<Dispositivo> dispositivoOpt = dispositivoService.obtenerPorId(id);
    
    if (dispositivoOpt.isPresent()) {
        model.addAttribute("dispositivo", dispositivoOpt.get());
        return "dispositivos/detalle"; // Esta es la nueva plantilla que crearemos
    } else {
        return "redirect:/dispositivos?error=Dispositivo+no+encontrado";
    }
}
    
    @GetMapping("/nuevo")
public String mostrarFormulario(Model model) {
    model.addAttribute("dispositivo", new Dispositivo());
    
    return "dispositivos/formulario";
}
    
    @PostMapping("/guardar")
    public String guardarDispositivo(@ModelAttribute Dispositivo dispositivo, 
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
    
        if (authentication == null) {
            return "redirect:/login";
        }
    
        // Obtener usuario autenticado automáticamente
        String email = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioService.obtenerPorEmail(email);
        
        if (usuarioOpt.isPresent()) {
            dispositivo.setVendedor(usuarioOpt.get());
            dispositivoService.crearDispositivo(dispositivo);
            redirectAttributes.addFlashAttribute("mensaje", "Producto publicado exitosamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al identificar usuario");
        }
        return "redirect:/dispositivos";
    }
    //editar un dispositivo
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable String id,
                                          Authentication authentication,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        if (authentication == null) return "redirect:/login";
 
        Optional<Dispositivo> dispositivoOpt = dispositivoService.obtenerPorId(id);
        if (dispositivoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Dispositivo no encontrado");
            return "redirect:/dispositivos";
        }
 
        Dispositivo dispositivo = dispositivoOpt.get();
        String email = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = dispositivo.getVendedor() != null &&
                          dispositivo.getVendedor().getEmail().equals(email);
 
        if (!isAdmin && !isOwner) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar este producto");
            return "redirect:/dispositivos";
        }
 
        model.addAttribute("dispositivo", dispositivo);
        return "dispositivos/editar";     // ← va a templates/dispositivos/editar.html
    }

    //guardar la ediccion
    @PostMapping("/editar/{id}")
    public String guardarEdicion(@PathVariable String id,
                                 @ModelAttribute Dispositivo dispositivoEditado,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null) return "redirect:/login";
 
        Optional<Dispositivo> dispositivoOpt = dispositivoService.obtenerPorId(id);
        if (dispositivoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Dispositivo no encontrado");
            return "redirect:/dispositivos";
        }
 
        Dispositivo original = dispositivoOpt.get();
        String email = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = original.getVendedor() != null &&
                          original.getVendedor().getEmail().equals(email);
 
        if (!isAdmin && !isOwner) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar este producto");
            return "redirect:/dispositivos";
        }
 
        // Actualizar campos pero se  conservan los id, vendedor y fecha original
        dispositivoEditado.setId(id);
        dispositivoEditado.setVendedor(original.getVendedor());
        dispositivoEditado.setFechaPublicacion(original.getFechaPublicacion());
 
        dispositivoService.actualizarDispositivo(dispositivoEditado);
        redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado correctamente");
        return "redirect:/dispositivos";
    }
    
    @GetMapping("/eliminar/{id}")
    public String eliminarDispositivo(@PathVariable String id, 
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        Optional<Dispositivo> dispositivoOpt = dispositivoService.obtenerPorId(id);
        if (dispositivoOpt.isPresent()) {
            Dispositivo dispositivo = dispositivoOpt.get();
            String email = authentication.getName();
            
            // Verificar si es admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            // Verificar si es el dueño del producto
            boolean isOwner = false;
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorEmail(email);
            if (usuarioOpt.isPresent() && dispositivo.getVendedor() != null) {
                isOwner = dispositivo.getVendedor().getId().equals(usuarioOpt.get().getId());
            }
            
            if (isAdmin || isOwner) {
                dispositivoService.eliminarDispositivo(id);
                redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado correctamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar este producto");
            }
        }
        return "redirect:/dispositivos";
    }
    
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable String id, Model model) {
        Optional<Dispositivo> dispositivoOpt = dispositivoService.obtenerPorId(id);
        if (dispositivoOpt.isPresent()) {
            model.addAttribute("dispositivo", dispositivoOpt.get());
            return "dispositivos/detalle";
        }
        return "redirect:/dispositivos";
    }
}
