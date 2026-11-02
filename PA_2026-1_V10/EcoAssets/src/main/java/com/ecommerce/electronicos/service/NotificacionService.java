package com.ecommerce.electronicos.service;

import com.ecommerce.electronicos.model.Dispositivo;
import com.ecommerce.electronicos.model.Notificacion;
import com.ecommerce.electronicos.model.Pedido;
import com.ecommerce.electronicos.model.Usuario;
import com.ecommerce.electronicos.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * US-04: avisa al vendedor cuando alguien compra uno de sus productos.
 * Crea una notificación dentro de la app (campana del menú) y envía un correo.
 */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository notificacionRepository;
    private final PedidoService pedidoService;
    private final CorreoService correoService;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.base-url:http://localhost:8082}")
    private String baseUrl;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               PedidoService pedidoService,
                               CorreoService correoService,
                               SpringTemplateEngine templateEngine) {
        this.notificacionRepository = notificacionRepository;
        this.pedidoService = pedidoService;
        this.correoService = correoService;
        this.templateEngine = templateEngine;
    }

    /** Se llama justo después de guardar el pedido. Corre en segundo plano: nunca rompe la compra. */
    @Async
    public void notificarVentaAsync(String pedidoId) {
        try {
            Optional<Pedido> pedidoOpt = pedidoService.obtenerPorId(pedidoId);
            if (pedidoOpt.isEmpty()) {
                log.warn("No se notificó la venta: pedido {} no encontrado.", pedidoId);
                return;
            }
            notificarVendedores(pedidoOpt.get());
        } catch (Exception e) {
            log.error("Error notificando ventas del pedido {}", pedidoId, e);
        }
    }

    private void notificarVendedores(Pedido pedido) {
        Usuario comprador = pedido.getComprador();
        if (pedido.getProductos() == null) {
            return;
        }

        // Una notificación por cada producto vendido (cada uno puede ser de un vendedor distinto)
        for (Dispositivo d : pedido.getProductos()) {
            if (d == null || d.getVendedor() == null || d.getVendedor().getId() == null) {
                continue;
            }
            try {
                Usuario vendedor = d.getVendedor();

                Notificacion n = new Notificacion();
                n.setVendedorId(vendedor.getId());
                n.setPedidoId(pedido.getId());
                n.setProductoNombre(d.getNombre());
                n.setProductoMarcaModelo(((d.getMarca() != null ? d.getMarca() : "") + " "
                        + (d.getModelo() != null ? d.getModelo() : "")).trim());
                n.setPrecio(d.getPrecio());
                n.setFechaVenta(pedido.getFecha());
                if (comprador != null) {
                    n.setCompradorNombre(comprador.getNombre());
                    n.setCompradorEmail(comprador.getEmail());
                    n.setCompradorTelefono(comprador.getTelefono());
                }
                n = notificacionRepository.save(n);

                // Correo (si falla o no hay SMTP, la notificación dentro de la app ya quedó guardada)
                n.setCorreoEnviado(enviarCorreo(vendedor, n));
                notificacionRepository.save(n);

            } catch (Exception e) {
                log.error("No se pudo notificar la venta de '{}' (pedido {})", d.getNombre(), pedido.getId(), e);
            }
        }
    }

    private boolean enviarCorreo(Usuario vendedor, Notificacion n) {
        Context ctx = new Context(Locale.forLanguageTag("es-CO"));
        ctx.setVariable("vendedor", vendedor);
        ctx.setVariable("n", n);
        ctx.setVariable("urlNotificaciones", urlBase() + "/notificaciones");

        String html = templateEngine.process("notificaciones/email-venta", ctx);
        String asunto = "¡Vendiste \"" + n.getProductoNombre() + "\"! - EcoAssets";
        return correoService.enviar(vendedor.getEmail(), vendedor.getNombre(), asunto, html);
    }

    // ---- Consultas para la web ----

    public List<Notificacion> listarDelVendedor(String vendedorId) {
        return notificacionRepository.findByVendedorIdOrderByFechaVentaDesc(vendedorId);
    }

    public long contarNoLeidas(String vendedorId) {
        return notificacionRepository.countByVendedorIdAndLeidaFalse(vendedorId);
    }

    /** Solo marca como leída si la notificación pertenece a ese vendedor. */
    public void marcarLeida(String notificacionId, String vendedorId) {
        notificacionRepository.findById(notificacionId).ifPresent(n -> {
            if (vendedorId.equals(n.getVendedorId()) && !n.isLeida()) {
                n.setLeida(true);
                notificacionRepository.save(n);
            }
        });
    }

    public void marcarTodasLeidas(String vendedorId) {
        List<Notificacion> pendientes = notificacionRepository.findByVendedorIdAndLeidaFalse(vendedorId);
        pendientes.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(pendientes);
    }

    private String urlBase() {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
