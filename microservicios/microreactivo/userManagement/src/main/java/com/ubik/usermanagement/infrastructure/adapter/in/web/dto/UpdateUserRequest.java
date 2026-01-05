package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest (
        @Size(min = 10, max = 20, message = "El número de teléfono debe tener entre 10 y 20 caracteres")
        String phoneNumber,

        Boolean anonymous,

        @Email(message = "Correo electrónico no válido")
        @Size(max = 100, message = "El correo electrónico no debe exceder 100 caracteres")
        String email
)
{}
