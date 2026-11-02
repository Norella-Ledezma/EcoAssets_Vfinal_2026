package com.ecommerce.electronicos.service;

import com.ecommerce.electronicos.model.Pedido;
import com.ecommerce.electronicos.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;

/** Renderiza la plantilla de factura (templates/factura/email.html) y la envía al comprador. */
@Service
public class FacturaEmailService {

    private static final Logger log = LoggerFactory.getLogger(FacturaEmailService.class);

    private final SpringTemplateEngine templateEngine;
    private final CorreoService correoService;
    private final PedidoService pedidoService;

    @Value("${app.base-url:http://localhost:8082}")
    private String baseUrl;

    public FacturaEmailService(SpringTemplateEngine templateEngine,
                               CorreoService correoService,
                               PedidoService pedidoService) {
        this.templateEngine = templateEngine;
        this.correoService = correoService;
        this.pedidoService = pedidoService;
    }

    /** Envío directo (bloquea hasta tener respuesta). Lo usa el botón "Reenviar por correo". */
    public boolean enviarFactura(Pedido pedido) {
        Usuario comprador = pedido.getComprador();
        if (comprador == null || comprador.getEmail() == null || comprador.getEmail().isBlank()) {
            log.warn("Factura {} sin enviar: el comprador no tiene email.", pedido.getNumeroFactura());
            return false;
        }

        Context ctx = new Context(Locale.forLanguageTag("es-CO"));
        ctx.setVariable("pedido", pedido);
        ctx.setVariable("urlFactura", urlBase() + "/facturas/" + pedido.getId());

        String html = templateEngine.process("factura/email", ctx);
        String asunto = "Tu factura " + pedido.getNumeroFactura() + " - EcoAssets";
        return correoService.enviar(comprador.getEmail(), comprador.getNombre(), asunto, html);
    }

    /** Envío en segundo plano: la compra no se demora ni se rompe si el correo falla. */
    @Async
    public void enviarFacturaAsync(String pedidoId) {
        try {
            pedidoService.obtenerPorId(pedidoId).ifPresentOrElse(
                    this::enviarFactura,
                    () -> log.warn("No se envió factura: pedido {} no encontrado.", pedidoId));
        } catch (Exception e) {
            log.error("Error enviando factura del pedido {}", pedidoId, e);
        }
    }

    private String urlBase() {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
