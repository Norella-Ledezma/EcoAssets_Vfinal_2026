package com.ecommerce.electronicos.controller;

import com.ecommerce.electronicos.model.Usuario;
import com.ecommerce.electronicos.service.NotificacionService;
import com.ecommerce.electronicos.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/** Notificaciones de venta del vendedor autenticado (US-04). */
@Controller
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioService usuarioService;

    public NotificacionController(NotificacionService notificacionService, UsuarioService usuarioService) {
        this.notificacionService = notificacionService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Authentication auth, Model model) {
        Usuario vendedor = usuarioService.obtenerPorEmail(auth.getName()).orElseThrow();
        model.addAttribute("notificaciones", notificacionService.listarDelVendedor(vendedor.getId()));
        model.addAttribute("noLeidas", notificacionService.contarNoLeidas(vendedor.getId()));
        return "notificaciones/lista";
    }

    /** Lo consulta la campana del menú para mostrar el número de avisos sin leer. */
    @GetMapping("/contador")
    @ResponseBody
    public Map<String, Long> contador(Authentication auth) {
        long total = usuarioService.obtenerPorEmail(auth.getName())
                .map(u -> notificacionService.contarNoLeidas(u.getId()))
                .orElse(0L);
        return Map.of("noLeidas", total);
    }

    @PostMapping("/{id}/leida")
    public String marcarLeida(@PathVariable String id, Authentication auth) {
        usuarioService.obtenerPorEmail(auth.getName())
                .ifPresent(u -> notificacionService.marcarLeida(id, u.getId()));
        return "redirect:/notificaciones";
    }

    @PostMapping("/leer-todas")
    public String marcarTodasLeidas(Authentication auth) {
        usuarioService.obtenerPorEmail(auth.getName())
                .ifPresent(u -> notificacionService.marcarTodasLeidas(u.getId()));
        return "redirect:/notificaciones";
    }
}
