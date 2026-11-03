package com.ecommerce.electronicos.service;

import com.ecommerce.electronicos.dto.PrediccionRequest;
import com.ecommerce.electronicos.dto.PrediccionResponse;
import com.ecommerce.electronicos.model.HistorialPrediccion;
import com.ecommerce.electronicos.prediccion.PredictorVentas;
import com.ecommerce.electronicos.repository.HistorialPrediccionRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Servicio de predicción de ventas con Weka. Al arrancar carga el modelo y la estructura de atributos
 * desde src/main/resources/weka/ (igual que el video carga diabetes.model y diabetes.arff).
 */
@Service
public class PrediccionVentasService {

    private static final Logger log = LoggerFactory.getLogger(PrediccionVentasService.class);

    private final HistorialPrediccionRepository historialRepository;

    private PredictorVentas predictor;
    private final Properties metricas = new Properties();
    private final List<PrediccionRequest> basesProximoMes = new ArrayList<>();
    private String mesProximo = "";

    public PrediccionVentasService(HistorialPrediccionRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @PostConstruct
    public void cargar() {
        try (InputStream modelo = new ClassPathResource("weka/ventas_modelo.model").getInputStream();
             InputStream estructura = new ClassPathResource("weka/ventas_estructura.arff").getInputStream()) {
            predictor = new PredictorVentas(modelo, estructura);
            log.info("Modelo de predicción de ventas cargado correctamente (ventas_modelo.model + ventas_estructura.arff).");
        } catch (Exception e) {
            predictor = null;
            log.error("No se pudo cargar el modelo de predicción de ventas: {}", e.getMessage(), e);
        }

        try (InputStream in = new ClassPathResource("weka/ventas_metricas.properties").getInputStream();
             BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            metricas.load(r);
        } catch (Exception e) {
            log.warn("No se pudieron leer las métricas del modelo: {}", e.getMessage());
        }

        cargarBaseProximoMes();
    }

    // ---------------- Estado ----------------

    public boolean modeloDisponible() {
        return predictor != null;
    }

    public List<String> categorias() {
        return predictor != null ? predictor.getCategorias() : new ArrayList<>();
    }

    // ---------------- Predicciones ----------------

    public PrediccionResponse predecir(PrediccionRequest req) {
        if (predictor == null) {
            throw new IllegalStateException("El modelo de predicción no está disponible. Revisa el log del servidor.");
        }
        double estimado;
        try {
            estimado = predictor.predecirCop(req);
        } catch (IllegalArgumentException e) {
            throw e;                                          // datos inválidos: se informa tal cual
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular la predicción: " + e.getMessage(), e);
        }
        double r = numero("rmse_log", 0.35);                  // error de prueba en escala logarítmica
        return new PrediccionResponse(req.getCategoria(), req.getMesDelAnio(), redondear(estimado),
                redondear(estimado * Math.exp(-r)), redondear(estimado * Math.exp(r)), nombreModelo());
    }

    public PrediccionResponse predecirYGuardar(PrediccionRequest req, String usuario) {
        PrediccionResponse resp = predecir(req);
        HistorialPrediccion h = new HistorialPrediccion();
        h.setUsuario(usuario);
        h.setEntrada(req);
        h.setPrediccionCop(resp.getPrediccionCop());
        h.setRangoMinCop(resp.getRangoMinCop());
        h.setRangoMaxCop(resp.getRangoMaxCop());
        h.setModelo(resp.getModelo());
        historialRepository.save(h);
        return resp;
    }

    /** Proyección automática del próximo mes para cada categoría (a partir de los datos base del modelo). */
    public List<PrediccionResponse> proyeccionProximoMes() {
        List<PrediccionResponse> lista = new ArrayList<>();
        if (predictor == null) return lista;
        for (PrediccionRequest base : basesProximoMes) {
            try {
                lista.add(predecir(base));
            } catch (RuntimeException e) {
                log.warn("Sin proyección para {}: {}", base.getCategoria(), e.getMessage());
            }
        }
        return lista;
    }

    public List<PrediccionRequest> basesPorCategoria() {
        return basesProximoMes;
    }

    public String mesProyeccion() {
        return mesProximo;
    }

    public List<HistorialPrediccion> historialReciente() {
        return historialRepository.findTop20ByOrderByFechaDesc();
    }

    /** Datos del modelo para mostrar en la página (texto ya formateado). */
    public Map<String, String> resumenModelo() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("modelo", nombreModelo());
        m.put("wekaVersion", metricas.getProperty("weka_version", "-"));
        m.put("entrenadoCon", metricas.getProperty("entrenado_con", "-"));
        m.put("prueba", metricas.getProperty("prueba", "-"));
        m.put("mae", miles(numero("mae_cop", 0)));
        m.put("rmse", miles(numero("rmse_cop", 0)));
        m.put("r2", String.format("%.3f", numero("r2", 0)));
        m.put("baseMae", miles(numero("base_mae_cop", 0)));
        m.put("baseR2", String.format("%.3f", numero("base_r2", 0)));
        double baseMae = numero("base_mae_cop", 0);
        m.put("mejora", baseMae > 0 ? String.format("%.0f", (1 - numero("mae_cop", 0) / baseMae) * 100) : "-");
        m.put("origen", metricas.getProperty("origen_datos", "simulado"));
        return m;
    }

    // ---------------- Utilidades ----------------

    private void cargarBaseProximoMes() {
        try (InputStream in = new ClassPathResource("weka/proyeccion_base.csv").getInputStream();
             BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String[] cab = r.readLine().split(",");
            Map<String, Integer> ix = new LinkedHashMap<>();
            for (int i = 0; i < cab.length; i++) ix.put(cab[i].trim(), i);
            String linea;
            while ((linea = r.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] c = linea.split(",");
                PrediccionRequest p = new PrediccionRequest();
                p.setCategoria(c[ix.get("categoria")].trim());
                p.setMesDelAnio(Integer.parseInt(c[ix.get("mes_del_anio")].trim()));
                p.setFestivosMes(Integer.parseInt(c[ix.get("festivos_mes")].trim()));
                p.setVentasMesAnterior(Double.parseDouble(c[ix.get("ventas_mes_anterior")]));
                p.setPublicacionesActivasInicio(Double.parseDouble(c[ix.get("publicaciones_activas_inicio")]));
                p.setPublicacionesNuevasMesAnterior(Double.parseDouble(c[ix.get("publicaciones_nuevas_mes_anterior")]));
                p.setVendedoresActivosMesAnterior(Double.parseDouble(c[ix.get("vendedores_activos_mes_anterior")]));
                p.setPrecioPromedioMesAnterior(Double.parseDouble(c[ix.get("precio_promedio_mes_anterior")]));
                p.setProporcionReacondicionadosMesAnterior(Double.parseDouble(c[ix.get("proporcion_reacondicionados_mes_anterior")]));
                basesProximoMes.add(p);
                mesProximo = c[ix.get("mes")].trim();
            }
        } catch (Exception e) {
            log.warn("No se pudo leer proyeccion_base.csv: {}", e.getMessage());
        }
    }

    private String nombreModelo() {
        return metricas.getProperty("modelo", "Regresión lineal (Weka)");
    }

    private double numero(String clave, double porDefecto) {
        try {
            return Double.parseDouble(metricas.getProperty(clave));
        } catch (Exception e) {
            return porDefecto;
        }
    }

    private static double redondear(double v) {
        return Math.round(v);
    }

    private static String miles(double v) {
        return String.format("%,.0f", v);
    }
}
