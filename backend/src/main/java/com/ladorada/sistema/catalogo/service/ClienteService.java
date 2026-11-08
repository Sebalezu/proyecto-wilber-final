package com.ladorada.sistema.catalogo.service;

import com.ladorada.sistema.catalogo.dto.ClienteRequest;
import com.ladorada.sistema.catalogo.dto.ClienteResponse;
import com.ladorada.sistema.catalogo.model.Cliente;
import com.ladorada.sistema.catalogo.repository.ClienteRepository;
import com.ladorada.sistema.common.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(String q) {
        List<Cliente> clientes;
        if (q != null && !q.trim().isEmpty()) {
            clientes = clienteRepository.findByNombreContainingIgnoreCase(q.trim());
        } else {
            clientes = clienteRepository.findAll();
        }
        return clientes.stream().map(ClienteResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
        return ClienteResponse.fromEntity(cliente);
    }

    public ClienteResponse crear(ClienteRequest request) {
        Cliente cliente = new Cliente(
                request.nombre().trim(),
                request.telefono() != null ? request.telefono().trim() : ""
        );
        Cliente guardado = clienteRepository.save(cliente);
        return ClienteResponse.fromEntity(guardado);
    }

    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));

        cliente.setNombre(request.nombre().trim());
        cliente.setTelefono(request.telefono() != null ? request.telefono().trim() : "");

        Cliente actualizado = clienteRepository.save(cliente);
        return ClienteResponse.fromEntity(actualizado);
    }
}
