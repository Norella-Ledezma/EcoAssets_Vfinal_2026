package com.ecommerce.electronicos.controller;

import com.ecommerce.electronicos.model.Dispositivo;
import com.ecommerce.electronicos.service.CarritoService;
import com.ecommerce.electronicos.service.DispositivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private DispositivoService dispositivoService;

    @GetMapping
    public String verCarrito(Model model) {
        model.addAttribute("items", carritoService.getItems());
        model.addAttribute("total", carritoService.obtenerTotal());
        return "carrito/verCarrito";
    }

    @PostMapping("/agregar")
    public String agregarAlCarrito(@RequestParam String dispositivoId, 
                                Authentication authentication, 
                                RedirectAttributes redirectAttributes) {
        if (authentication == null) return "redirect:/login";

        Optional<Dispositivo> dispositivoOpt = dispositivoService.obtenerPorId(dispositivoId);
        
        if (dispositivoOpt.isPresent()) {
            Dispositivo disp = dispositivoOpt.get();
            String emailUsuarioLogueado = authentication.getName();

            // VALIDACIÓN: ¿Es el dueño del producto?
            if (disp.getVendedor() != null && disp.getVendedor().getEmail().equals(emailUsuarioLogueado)) {
                redirectAttributes.addFlashAttribute("error", "No puedes agregar tu propio producto al carrito.");
                return "redirect:/dispositivos/detalle/" + dispositivoId;
            }

            if (!disp.isDisponible()) {
                redirectAttributes.addFlashAttribute("error", "Lo sentimos, este producto ya fue vendido.");
                return "redirect:/dispositivos";
            }

            boolean agregado = carritoService.agregarDispositivo(disp);
            if (agregado) {
                redirectAttributes.addFlashAttribute("mensaje", "Producto agregado al carrito con éxito");
            } else {
                redirectAttributes.addFlashAttribute("error", "Este producto ya está en tu carrito");
            }
        }
        return "redirect:/carrito";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarDelCarrito(@PathVariable String id, RedirectAttributes redirectAttributes) {
        carritoService.eliminarDispositivo(id);
        redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito");
        return "redirect:/carrito";
    }
    
    @GetMapping("/vaciar")
    public String vaciarCarrito(RedirectAttributes redirectAttributes) {
        carritoService.vaciarCarrito();
        redirectAttributes.addFlashAttribute("mensaje", "Carrito vaciado");
        return "redirect:/carrito";
    }
}
