package com.ecommerce.electronicos.controller;

import com.ecommerce.electronicos.model.Pedido;
import com.ecommerce.electronicos.service.FacturaEmailService;
import com.ecommerce.electronicos.service.PedidoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/facturas")
public class FacturaController {

    private final PedidoService pedidoService;
    private final FacturaEmailService facturaEmailService;

    public FacturaController(PedidoService pedidoService, FacturaEmailService facturaEmailService) {
        this.pedidoService = pedidoService;
        this.facturaEmailService = facturaEmailService;
    }

    // Ver la factura en la web (solo el comprador o un admin)
    @GetMapping("/{id}")
    public String verFactura(@PathVariable String id, Authentication auth, Model model, RedirectAttributes ra) {
        Optional<Pedido> pedidoOpt = pedidoService.obtenerPorId(id);

        if (pedidoOpt.isEmpty() || !puedeVer(pedidoOpt.get(), auth)) {
            // Mismo mensaje en ambos casos para no revelar qué facturas existen
            ra.addFlashAttribute("error", "No se encontró la factura solicitada.");
            return "redirect:/dispositivos";
        }

        model.addAttribute("pedido", pedidoOpt.get());
        return "factura/ver";
    }

    // Reenviar la factura al correo del comprador
    @PostMapping("/{id}/enviar")
    public String reenviarFactura(@PathVariable String id, Authentication auth, RedirectAttributes ra) {
        Optional<Pedido> pedidoOpt = pedidoService.obtenerPorId(id);

        if (pedidoOpt.isEmpty() || !puedeVer(pedidoOpt.get(), auth)) {
            ra.addFlashAttribute("error", "No se encontró la factura solicitada.");
            return "redirect:/dispositivos";
        }

        Pedido pedido = pedidoOpt.get();
        boolean enviado = facturaEmailService.enviarFactura(pedido);
        if (enviado) {
            ra.addFlashAttribute("mensaje", "Te enviamos la factura a " + pedido.getComprador().getEmail() + ".");
        } else {
            ra.addFlashAttribute("error", "No pudimos enviar el correo en este momento. Intenta de nuevo más tarde.");
        }
        return "redirect:/facturas/" + id;
    }

    private boolean puedeVer(Pedido pedido, Authentication auth) {
        if (auth == null) return false;

        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean esComprador = pedido.getComprador() != null
                && auth.getName().equals(pedido.getComprador().getEmail());

        return esAdmin || esComprador;
    }
}
