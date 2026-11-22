package com.citasalud.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reloj de la aplicación. Se inyecta en los servicios (inyección de dependencias)
 * para que las pruebas puedan fijar la hora y probar la regla de las 24 horas.
 */
@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj(@Value("${app.zona-horaria}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
