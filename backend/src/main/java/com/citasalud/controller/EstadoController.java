package com.citasalud.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Permite comprobar rápido que la API está viva. */
@RestController
@RequestMapping("/api/estado")
public class EstadoController {

    @GetMapping
    public Map<String, String> estado() {
        return Map.of("aplicacion", "CitaSalud", "estado", "funcionando");
    }
}
