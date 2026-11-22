package com.citasalud.dto;

/** Token que el cliente debe enviar en el encabezado Authorization: Bearer ... */
public record LoginResponse(String token, UsuarioResponse usuario) {
}
