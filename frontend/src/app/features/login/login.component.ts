import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { RolUsuario } from '../../shared/models';

interface UsuarioPrueba {
  etiqueta: string;
  subtitulo: string;
  icono: string;
  email: string;
  pass: string;
  rol: RolUsuario;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  email = '';
  password = '';
  cargando = false;
  errorMensaje: string | null = null;
  returnUrl = '/inicio';

  usuariosPrueba: UsuarioPrueba[] = [
    {
      etiqueta: 'Administrador',
      subtitulo: 'Control total del sistema',
      icono: '👑',
      email: 'admin@ladorada.com',
      pass: 'admin123',
      rol: 'ADMINISTRADOR'
    },
    {
      etiqueta: 'Vendedor',
      subtitulo: 'Gestión y creación de pedidos',
      icono: '💼',
      email: 'ventas@ladorada.com',
      pass: 'ventas123',
      rol: 'VENDEDOR'
    },
    {
      etiqueta: 'Bodega',
      subtitulo: 'Despacho e inventario',
      icono: '📦',
      email: 'bodega@ladorada.com',
      pass: 'bodega123',
      rol: 'BODEGA'
    }
  ];

  ngOnInit(): void {
    // Si ya está autenticado, redirigir directo al inicio
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/inicio']);
      return;
    }

    const returnParam = this.route.snapshot.queryParamMap.get('returnUrl');
    if (returnParam) {
      this.returnUrl = returnParam;
    }
  }

  seleccionarUsuario(u: UsuarioPrueba): void {
    this.email = u.email;
    this.password = u.pass;
    this.errorMensaje = null;
    this.iniciarSesion();
  }

  iniciarSesion(): void {
    if (!this.email.trim() || !this.password.trim()) {
      this.errorMensaje = 'Por favor ingrese su correo electrónico y contraseña.';
      return;
    }

    this.cargando = true;
    this.errorMensaje = null;

    this.authService.login(this.email, this.password).subscribe({
      next: () => {
        this.cargando = false;
        this.router.navigateByUrl(this.returnUrl);
      },
      error: (err) => {
        this.cargando = false;
        console.error('Error de autenticación:', err);
        if (err.status === 401) {
          this.errorMensaje = 'Credenciales inválidas. Por favor verifique el correo y la contraseña ingresados.';
        } else {
          this.errorMensaje = err?.error?.message || 'Error al conectar con el servidor. Intente nuevamente.';
        }
      }
    });
  }
}

