package com.ladorada.sistema.catalogo.controller;

import com.ladorada.sistema.catalogo.dto.ClienteRequest;
import com.ladorada.sistema.catalogo.dto.ClienteResponse;
import com.ladorada.sistema.catalogo.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(originPatterns = "*")
@Tag(name = "Clientes", description = "Operaciones de consulta y administración de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna la lista de clientes con opción de búsqueda por coincidencia en el nombre.")
    public ResponseEntity<List<ClienteResponse>> listar(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(clienteService.listar(q));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Busca y retorna un cliente por su identificador único.")
    public ResponseEntity<ClienteResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo cliente", description = "Registra un nuevo cliente en el sistema.")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse creado = clienteService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente.")
    public ResponseEntity<ClienteResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request
    ) {
        ClienteResponse actualizado = clienteService.actualizar(id, request);
        return ResponseEntity.ok(actualizado);
    }
}
