package com.ladorada.sistema.pedidos.repository;

import com.ladorada.sistema.pedidos.model.EstadoPedido;
import com.ladorada.sistema.pedidos.model.Pedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT p FROM Pedido p " +
            "JOIN FETCH p.cliente c " +
            "WHERE (:estado IS NULL OR p.estado = :estado) " +
            "AND (:clienteId IS NULL OR c.id = :clienteId) " +
            "ORDER BY p.createdAt DESC, p.id DESC")
    List<Pedido> buscarPedidos(
            @Param("estado") EstadoPedido estado,
            @Param("clienteId") Long clienteId
    );

    @Query("SELECT DISTINCT p FROM Pedido p " +
            "JOIN FETCH p.cliente c " +
            "LEFT JOIN FETCH p.lineas l " +
            "LEFT JOIN FETCH l.producto prod " +
            "WHERE p.id = :id")
    Optional<Pedido> findByIdConLineas(@Param("id") Long id);
}

