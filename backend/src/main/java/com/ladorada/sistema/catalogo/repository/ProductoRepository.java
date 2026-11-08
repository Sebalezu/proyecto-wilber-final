package com.ladorada.sistema.catalogo.repository;

import com.ladorada.sistema.catalogo.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdConBloqueo(@Param("id") Long id);

    @Query("SELECT p FROM Producto p WHERE " +
            "(:activo IS NULL OR p.activo = :activo) AND " +
            "(:q IS NULL OR :q = '' OR " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "ORDER BY p.id ASC")
    List<Producto> buscarProductos(@Param("q") String q, @Param("activo") Boolean activo);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.existencias <= :umbral ORDER BY p.existencias ASC, p.nombre ASC")
    List<Producto> findStockBajo(@Param("umbral") int umbral);

    @Query("SELECT p.codigo FROM Producto p WHERE p.codigo LIKE CONCAT(:prefijo, '%')")
    List<String> findCodigosByPrefijo(@Param("prefijo") String prefijo);
}
