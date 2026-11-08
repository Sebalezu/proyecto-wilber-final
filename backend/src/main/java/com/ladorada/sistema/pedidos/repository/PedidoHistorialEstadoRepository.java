package com.ladorada.sistema.pedidos.repository;

import com.ladorada.sistema.pedidos.model.PedidoHistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoHistorialEstadoRepository extends JpaRepository<PedidoHistorialEstado, Long> {

    List<PedidoHistorialEstado> findByPedidoIdOrderByFechaAsc(Long pedidoId);
}
