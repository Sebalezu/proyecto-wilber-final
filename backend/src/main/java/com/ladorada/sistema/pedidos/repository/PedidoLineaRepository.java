package com.ladorada.sistema.pedidos.repository;

import com.ladorada.sistema.pedidos.model.PedidoLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoLineaRepository extends JpaRepository<PedidoLinea, Long> {

    List<PedidoLinea> findByPedidoId(Long pedidoId);
}
