package com.ladorada.sistema.catalogo.dto;

import jakarta.validation.constraints.NotNull;

public record ProductoActivoRequest(
        @NotNull(message = "El estado activo es obligatorio (true o false)")
        Boolean activo
) {}
