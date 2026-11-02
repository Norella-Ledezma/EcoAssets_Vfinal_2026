package com.ecommerce.electronicos.service;

import com.ecommerce.electronicos.model.Dispositivo;
import com.ecommerce.electronicos.model.LineaPedido;
import com.ecommerce.electronicos.model.Pedido;
import com.ecommerce.electronicos.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {
    @Autowired
    private PedidoRepository pedidoRepository;

    public Pedido guardarPedido(Pedido pedido) {
        // Congelamos los productos tal como se compraron (precio, nombre, vendedor...)
        if ((pedido.getLineas() == null || pedido.getLineas().isEmpty()) && pedido.getProductos() != null) {
            List<LineaPedido> lineas = new ArrayList<>();
            for (Dispositivo d : pedido.getProductos()) {
                if (d != null) {
                    lineas.add(new LineaPedido(d));
                }
            }
            pedido.setLineas(lineas);
        }
        return pedidoRepository.save(pedido);
    }

    public Optional<Pedido> obtenerPorId(String id) {
        return pedidoRepository.findById(id);
    }

    public List<Pedido> obtenerPedidosPorUsuario(String usuarioId) {
        return pedidoRepository.findByCompradorId(usuarioId);
    }
}
