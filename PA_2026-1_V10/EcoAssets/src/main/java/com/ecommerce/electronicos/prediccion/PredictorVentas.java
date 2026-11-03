package com.ecommerce.electronicos.prediccion;

import com.ecommerce.electronicos.dto.PrediccionRequest;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Carga el modelo de Weka (ventas_modelo.model) y la estructura de atributos (ventas_estructura.arff),
 * igual que en el video con diabetes.model y diabetes.arff, y estima las ventas mensuales de una categoría.
 *
 * El modelo se entrenó sobre log(1 + ventas), por eso aquí se aplica log1p a los montos de entrada
 * y expm1 al resultado. No depende de Spring.
 */
public class PredictorVentas {

    // Festivos típicos por mes en Colombia (referencia 2026). Solo se usa si el administrador no indica el dato.
    private static final int[] FESTIVOS_TIPICOS = {2, 0, 1, 2, 2, 3, 2, 2, 0, 1, 2, 2};

    private final Classifier modelo;
    private final Instances estructura;
    private final List<String> categorias;

    public PredictorVentas(InputStream modeloIn, InputStream estructuraIn) throws Exception {
        this.modelo = (Classifier) SerializationHelper.read(modeloIn);
        Instances d = new DataSource(estructuraIn).getDataSet();
        d.setClassIndex(d.numAttributes() - 1);
        this.estructura = new Instances(d, 0);                 // solo la estructura, sin filas
        Attribute cat = estructura.attribute("categoria");
        List<String> lista = new ArrayList<>();
        for (int i = 0; i < cat.numValues(); i++) {
            lista.add(cat.value(i));
        }
        this.categorias = Collections.unmodifiableList(lista);
    }

    public List<String> getCategorias() {
        return categorias;
    }

    /** Valida los datos de entrada; lanza IllegalArgumentException con un mensaje entendible. */
    public void validar(PrediccionRequest e) {
        if (e == null) throw new IllegalArgumentException("No se recibieron datos.");
        if (e.getCategoria() == null || !categorias.contains(e.getCategoria())) {
            throw new IllegalArgumentException("Categoría no válida. Opciones: " + categorias);
        }
        if (e.getMesDelAnio() < 1 || e.getMesDelAnio() > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }
        if (e.getFestivosMes() != null && (e.getFestivosMes() < 0 || e.getFestivosMes() > 10)) {
            throw new IllegalArgumentException("Los festivos del mes deben estar entre 0 y 10.");
        }
        if (e.getVentasMesAnterior() < 0 || e.getPublicacionesActivasInicio() < 0
                || e.getPublicacionesNuevasMesAnterior() < 0 || e.getVendedoresActivosMesAnterior() < 0
                || e.getPrecioPromedioMesAnterior() < 0) {
            throw new IllegalArgumentException("Los valores numéricos no pueden ser negativos.");
        }
        double p = e.getProporcionReacondicionadosMesAnterior();
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("La proporción de reacondicionados debe estar entre 0 y 1.");
        }
    }

    /** Estimación de las ventas del mes, en pesos colombianos. */
    public double predecirCop(PrediccionRequest e) throws Exception {
        validar(e);
        int m = e.getMesDelAnio();
        int festivos = e.getFestivosMes() != null ? e.getFestivosMes() : FESTIVOS_TIPICOS[m - 1];

        Instance inst = new DenseInstance(estructura.numAttributes());   // todos los valores empiezan "vacíos"
        inst.setDataset(estructura);
        inst.setValue(estructura.attribute("categoria"), e.getCategoria());
        inst.setValue(estructura.attribute("log_ventas_lag1"), Math.log1p(e.getVentasMesAnterior()));
        inst.setValue(estructura.attribute("log_activas_inicio"), Math.log1p(e.getPublicacionesActivasInicio()));
        inst.setValue(estructura.attribute("log_nuevas_lag1"), Math.log1p(e.getPublicacionesNuevasMesAnterior()));
        inst.setValue(estructura.attribute("log_vendedores_lag1"), Math.log1p(e.getVendedoresActivosMesAnterior()));
        inst.setValue(estructura.attribute("log_precio_prom_lag1"), Math.log1p(e.getPrecioPromedioMesAnterior()));
        inst.setValue(estructura.attribute("prop_reac_lag1"), e.getProporcionReacondicionadosMesAnterior());
        inst.setValue(estructura.attribute("festivos_mes"), festivos);
        inst.setValue(estructura.attribute("es_prima"), (m == 6 || m == 12) ? 1 : 0);
        inst.setValue(estructura.attribute("es_navidad"), m == 12 ? 1 : 0);
        inst.setValue(estructura.attribute("es_black_friday"), m == 11 ? 1 : 0);
        inst.setValue(estructura.attribute("es_regreso_clases"), (m == 1 || m == 2 || m == 8) ? 1 : 0);
        inst.setValue(estructura.attribute("es_dia_madre"), m == 5 ? 1 : 0);
        inst.setValue(estructura.attribute("es_dia_padre"), m == 6 ? 1 : 0);

        double logPred = modelo.classifyInstance(inst);
        return Math.max(0.0, Math.expm1(logPred));
    }
}
