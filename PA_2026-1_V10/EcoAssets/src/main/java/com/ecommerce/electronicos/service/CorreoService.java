package com.ecommerce.electronicos.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Envía correos HTML por SMTP (variables MAIL_HOST, MAIL_PORT, MAIL_USER y MAIL_PASS).
 * Si MAIL_HOST no está definido no se envía nada y la app sigue funcionando.
 */
@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.from:no-reply@ecoassets.com}")
    private String remitente;

    public CorreoService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    /** @return true si el correo salió hacia el servidor SMTP; false si no está configurado o falló. */
    public boolean enviar(String paraEmail, String paraNombre, String asunto, String html) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("Correo NO enviado: el servidor de correo no está configurado (MAIL_HOST).");
            return false;
        }
        if (paraEmail == null || paraEmail.isBlank()) {
            log.warn("Correo NO enviado: el destinatario no tiene email.");
            return false;
        }

        try {
            MimeMessage mensaje = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, "UTF-8");
            helper.setFrom(remitente, "EcoAssets");
            helper.setTo(paraEmail);
            helper.setSubject(asunto);
            helper.setText(html, true);
            sender.send(mensaje);
            log.info("Correo enviado a {}", paraEmail);
            return true;
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}", paraEmail, e.getMessage(), e);
            return false;
        }
    }
    @jakarta.annotation.PostConstruct
public void probarSmtp() {
    log.info("SMTP host={} port={} user={}",
        System.getenv("MAIL_HOST"),
        System.getenv("MAIL_PORT"),
        System.getenv("MAIL_USERNAME"));
    try (java.net.Socket s = new java.net.Socket()) {
        s.connect(new java.net.InetSocketAddress("smtp.gmail.com", 587), 10000);
        log.info("SMTP 587 ALCANZABLE");
    } catch (Exception e) {
        log.error("SMTP 587 BLOQUEADO", e);
    }
}
}
