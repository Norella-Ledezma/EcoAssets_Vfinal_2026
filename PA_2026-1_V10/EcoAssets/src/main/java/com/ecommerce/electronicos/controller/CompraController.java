package com.ecommerce.electronicos.controller;

import com.ecommerce.electronicos.model.*;
import com.ecommerce.electronicos.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/compra")
public class CompraController {

    @Autowired private CarritoService carritoService;
    @Autowired private PedidoService pedidoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private DispositivoService dispositivoService;
    @Autowired private FacturaEmailService facturaEmailService;
    @Autowired private NotificacionService notificacionService;

    @GetMapping("/procesar")
public String procesarCompra(Authentication auth, RedirectAttributes ra) {
    try {
        String email = auth.getName();
        Usuario usuario = usuarioService.obtenerPorEmail(email).orElseThrow();
        
        if (usuario.getCarrito() == null || usuario.getCarrito().isEmpty()) {
            ra.addFlashAttribute("error", "Tu carrito está vacío.");
            return "redirect:/carrito";
        }

        //generar el pedido y quitarle el stock al dispositivo que fue seleccionado
        List<Dispositivo> prosesado = new ArrayList<>();
        for (Dispositivo d : new ArrayList<>(usuario.getCarrito())) {
            if (dispositivoService.quitarstock(d.getId(), 1)) {
                prosesado.add(d);
            }
        }

        // Si no hay stock no se crea
        if (prosesado.isEmpty()) {
            usuario.getCarrito().clear();
            usuarioService.actualizarUsuario(usuario);
            ra.addFlashAttribute("error", "Los productos de tu carrito ya no tienen stock.");
            return "redirect:/carrito";
        } 

        // 1. Crear el Pedido
        Pedido pedido = new Pedido();
        pedido.setComprador(usuario);
        pedido.setProductos(new ArrayList<>(usuario.getCarrito()));
        pedido.setTotal(carritoService.obtenerTotal());

        // 2. Persistencia
        Pedido guardado = pedidoService.guardarPedido(pedido);
        
        // 3. Limpiar carrito y actualizar usuario
        usuario.getCarrito().clear();
        usuarioService.actualizarUsuario(usuario);

        // 4. Factura: enviarla al correo (en segundo plano) y mostrarla en la web
        facturaEmailService.enviarFacturaAsync(guardado.getId());
        // 5. US-04: avisar a cada vendedor (en la app y por correo, en segundo plano)
        notificacionService.notificarVentaAsync(guardado.getId());
        ra.addFlashAttribute("mensaje", "¡Compra realizada con éxito! Esta es tu factura.");
        return "redirect:/facturas/" + guardado.getId();

    } catch (Exception e) {
        // Esto evitará el error 500 y te mostrará qué pasó en la consola
        e.printStackTrace(); 
        ra.addFlashAttribute("error", "Error al procesar la compra: " + e.getMessage());
        return "redirect:/carrito";
    }
    }

    @PostMapping("/rapida")
public String compraRapida(@RequestParam String dispositivoId, Authentication auth, RedirectAttributes ra) {
    try {
        String email = auth.getName();
        Usuario usuario = usuarioService.obtenerPorEmail(email).orElseThrow();
        
        Optional<Dispositivo> dispositivoOpt = dispositivoService.obtenerPorId(dispositivoId);
        
        if (dispositivoOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/dispositivos";
        }

        Dispositivo disp = dispositivoOpt.get();

        // Validaciones de seguridad
        if (!disp.isDisponible()) {
            ra.addFlashAttribute("error", "Lo sentimos, este producto ya no está disponible.");
            return "redirect:/dispositivos";
        }
        
        if (disp.getVendedor() != null && disp.getVendedor().getEmail().equals(email)) {
            ra.addFlashAttribute("error", "No puedes comprar tu propio producto.");
            return "redirect:/dispositivos/" + dispositivoId;
        }

        // 1. Crear el Pedido (solo con este producto)
        Pedido pedido = new Pedido();
        pedido.setComprador(usuario);
        pedido.setProductos(List.of(disp));
        pedido.setTotal(disp.getPrecio());
        
        // 2. Marcar como vendido si y solo si el stock llega a 0
        if (!dispositivoService.quitarstock(disp.getId(), 1)) {
            ra.addFlashAttribute("error", "Lo sentimos, este producto ya no tiene stock.");
            return "redirect:/dispositivos";
        }

        // 3. Guardar pedido
        Pedido guardado = pedidoService.guardarPedido(pedido);

        // 4. Factura: enviarla al correo (en segundo plano) y mostrarla en la web
        facturaEmailService.enviarFacturaAsync(guardado.getId());
        // 6. US-04: avisar a cada vendedor (en la app y por correo, en segundo plano)
        notificacionService.notificarVentaAsync(guardado.getId());
        ra.addFlashAttribute("mensaje", "¡Compra rápida realizada con éxito! Esta es tu factura.");
        return "redirect:/facturas/" + guardado.getId();

    } catch (Exception e) {
        e.printStackTrace();
        ra.addFlashAttribute("error", "Error al procesar la compra rápida.");
        return "redirect:/dispositivos/" + dispositivoId;
    }
}
}
