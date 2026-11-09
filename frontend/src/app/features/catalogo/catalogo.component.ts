import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CatalogoService } from '../../core/services/catalogo.service';
import { Producto, ProductoRequest, ProductoUpdateRequest } from '../../shared/models';

@Component({
  selector: 'app-catalogo',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './catalogo.component.html',
  styleUrl: './catalogo.component.scss'
})
export class CatalogoComponent implements OnInit {
  private readonly catalogoService = inject(CatalogoService);

  productos: Producto[] = [];
  cargando = false;
  errorMensaje: string | null = null;
  exitoMensaje: string | null = null;

  // Filtros
  filtroTexto = '';
  filtroActivo: boolean | null = null; // null = todos, true = activos, false = inactivos

  // Modal para Crear / Editar
  mostrarModal = false;
  modoEdicion = false;
  productoSeleccionadoId: number | null = null;

  // Formulario
  formCodigo = '';
  formNombre = '';
  formPrecio: number | null = null;
  formExistencias: number | null = null;
  formError: string | null = null;
  guardando = false;

  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos(): void {
    this.cargando = true;
    this.errorMensaje = null;

    const activoParam = this.filtroActivo !== null ? this.filtroActivo : undefined;

    this.catalogoService.getProductos(this.filtroTexto, activoParam).subscribe({
      next: (data) => {
        this.productos = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar catálogo:', err);
        this.errorMensaje = 'No se pudo cargar la lista de productos.';
        this.cargando = false;
      }
    });
  }

  onBuscar(): void {
    this.cargarProductos();
  }

  onCambioFiltroActivo(valor: string): void {
    if (valor === 'true') {
      this.filtroActivo = true;
    } else if (valor === 'false') {
      this.filtroActivo = false;
    } else {
      this.filtroActivo = null;
    }
    this.cargarProductos();
  }

  abrirModalCrear(): void {
    this.modoEdicion = false;
    this.productoSeleccionadoId = null;
    this.formCodigo = '';
    this.formNombre = '';
    this.formPrecio = null;
    this.formExistencias = 0;
    this.formError = null;
    this.mostrarModal = true;
  }

  abrirModalEditar(p: Producto): void {
    this.modoEdicion = true;
    this.productoSeleccionadoId = p.id;
    this.formCodigo = p.codigo;
    this.formNombre = p.nombre;
    this.formPrecio = p.precio;
    this.formExistencias = p.existencias;
    this.formError = null;
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.formError = null;
  }

  guardarProducto(): void {
    this.formError = null;

    if (!this.formNombre || !this.formNombre.trim()) {
      this.formError = 'El nombre del producto es obligatorio.';
      return;
    }

    if (this.formPrecio === null || this.formPrecio <= 0) {
      this.formError = 'El precio debe ser un número mayor a cero.';
      return;
    }

    if (this.formExistencias === null || this.formExistencias < 0) {
      this.formError = 'Las existencias deben ser un número entero mayor o igual a cero.';
      return;
    }

    this.guardando = true;

    if (this.modoEdicion && this.productoSeleccionadoId) {
      const updateReq: ProductoUpdateRequest = {
        nombre: this.formNombre.trim(),
        precio: this.formPrecio,
        existencias: Math.floor(this.formExistencias)
      };

      this.catalogoService.actualizarProducto(this.productoSeleccionadoId, updateReq).subscribe({
        next: (prodActualizado) => {
          this.guardando = false;
          this.cerrarModal();
          this.mostrarMensajeExito(`Producto '${prodActualizado.nombre}' actualizado correctamente.`);
          this.cargarProductos();
        },
        error: (err) => {
          this.guardando = false;
          this.formError = err?.error?.message || 'Error al actualizar el producto.';
        }
      });
    } else {
      const createReq: ProductoRequest = {
        codigo: this.formCodigo.trim() ? this.formCodigo.trim() : undefined,
        nombre: this.formNombre.trim(),
        precio: this.formPrecio,
        existencias: Math.floor(this.formExistencias)
      };

      this.catalogoService.crearProducto(createReq).subscribe({
        next: (prodCreado) => {
          this.guardando = false;
          this.cerrarModal();
          this.mostrarMensajeExito(`Producto '${prodCreado.nombre}' (${prodCreado.codigo}) creado exitosamente.`);
          this.cargarProductos();
        },
        error: (err) => {
          this.guardando = false;
          this.formError = err?.error?.message || 'Error al crear el producto. Verifique los datos enviados.';
        }
      });
    }
  }

  toggleActivo(p: Producto): void {
    const nuevoEstado = !p.activo;
    const accion = nuevoEstado ? 'activar' : 'desactivar';

    this.catalogoService.cambiarEstadoActivo(p.id, nuevoEstado).subscribe({
      next: (actualizado) => {
        p.activo = actualizado.activo;
        this.mostrarMensajeExito(`Producto '${p.nombre}' ${nuevoEstado ? 'activado' : 'desactivado'} exitosamente.`);
      },
      error: (err) => {
        console.error(`Error al ${accion} producto:`, err);
        this.errorMensaje = `No se pudo ${accion} el producto.`;
      }
    });
  }

  private mostrarMensajeExito(msg: string): void {
    this.exitoMensaje = msg;
    setTimeout(() => {
      this.exitoMensaje = null;
    }, 4500);
  }
}
