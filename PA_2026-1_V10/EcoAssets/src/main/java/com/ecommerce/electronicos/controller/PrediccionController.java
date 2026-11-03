package com.ecommerce.electronicos.controller;

import com.ecommerce.electronicos.dto.PrediccionRequest;
import com.ecommerce.electronicos.service.PrediccionVentasService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/** Predicción de ventas con Weka. Todo lo que está bajo /admin/** ya exige rol ADMIN en SecurityConfig. */
@Controller
@RequestMapping("/admin/prediccion")
public class PrediccionController {

    private final PrediccionVentasService servicio;

    public PrediccionController(PrediccionVentasService servicio) {
        this.servicio = servicio;
    }

    // Página: proyección por categoría, formulario para predecir, historial y datos del modelo
    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("modeloDisponible", servicio.modeloDisponible());
        model.addAttribute("categorias", servicio.categorias());
        model.addAttribute("mesProyeccion", servicio.mesProyeccion());
        model.addAttribute("proyeccion", servicio.proyeccionProximoMes());
        model.addAttribute("bases", servicio.basesPorCategoria());
        model.addAttribute("historial", servicio.historialReciente());
        model.addAttribute("resumen", servicio.resumenModelo());
        return "admin/prediccion";
    }

    // Igual que POST /predecir del video: recibe los datos y responde con la predicción
    @PostMapping("/predecir")
    @ResponseBody
    public ResponseEntity<?> predecir(@RequestBody PrediccionRequest request, Authentication auth) {
        try {
            String usuario = auth != null ? auth.getName() : "desconocido";
            return ResponseEntity.ok(servicio.predecirYGuardar(request, usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body(Map.of("error", e.getMessage()));
        }
    }
}
