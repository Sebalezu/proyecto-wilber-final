import { defineConfig } from 'vite';

// Configuración del servidor de desarrollo Vite para La Dorada S.A.S.
// Permite conexiones externas, túneles de ngrok y proxy al backend Spring Boot
export default defineConfig({
  server: {
    host: true,
    port: 4200,
    allowedHosts: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
});
