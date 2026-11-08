package com.ladorada.sistema.inventario.repository;

import com.ladorada.sistema.inventario.model.MovimientoInventario;
import com.ladorada.sistema.inventario.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    @Query("SELECT m FROM MovimientoInventario m WHERE " +
            "(:productoId IS NULL OR m.productoId = :productoId) AND " +
            "(:pedidoId IS NULL OR m.pedidoId = :pedidoId) AND " +
            "(:tipo IS NULL OR m.tipo = :tipo) " +
            "ORDER BY m.fecha DESC")
    List<MovimientoInventario> buscarMovimientos(
            @Param("productoId") Long productoId,
            @Param("pedidoId") Long pedidoId,
            @Param("tipo") TipoMovimiento tipo
    );
}
