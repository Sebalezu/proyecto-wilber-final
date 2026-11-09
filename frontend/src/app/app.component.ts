import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  private readonly authService = inject(AuthService);
  title = 'La Dorada S.A.S.';
  isMobileMenuOpen = false;

  usuarioActual$ = this.authService.usuarioActual$;

  toggleMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMenu(): void {
    this.isMobileMenuOpen = false;
  }

  logout(): void {
    this.closeMenu();
    this.authService.logout();
  }

  puedeCrearPedido(): boolean {
    return this.authService.hasRole(['ADMINISTRADOR', 'VENDEDOR']);
  }
}
