package com.ladorada.sistema.pedidos.model;

import com.ladorada.sistema.catalogo.model.Cliente;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Getter
@Setter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Column(name = "stock_descontado", nullable = false)
    private boolean stockDescontado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoLinea> lineas = new ArrayList<>();

    public Pedido() {
        this.estado = EstadoPedido.PENDIENTE;
        this.stockDescontado = false;
        this.total = BigDecimal.ZERO;
    }

    public Pedido(Cliente cliente) {
        this();
        this.cliente = cliente;
    }

    public Pedido(Long id, Cliente cliente, EstadoPedido estado, boolean stockDescontado, BigDecimal total, LocalDateTime createdAt) {
        this.id = id;
        this.cliente = cliente;
        this.estado = estado;
        this.stockDescontado = stockDescontado;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoPedido.PENDIENTE;
        }
        if (total == null) {
            total = BigDecimal.ZERO;
        }
    }

    public void agregarLinea(PedidoLinea linea) {
        lineas.add(linea);
        linea.setPedido(this);
    }

    public void limpiarLineas() {
        lineas.clear();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public boolean isStockDescontado() {
        return stockDescontado;
    }

    public void setStockDescontado(boolean stockDescontado) {
        this.stockDescontado = stockDescontado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<PedidoLinea> getLineas() {
        return lineas;
    }

    public void setLineas(List<PedidoLinea> lineas) {
        this.lineas = lineas;
    }
}

