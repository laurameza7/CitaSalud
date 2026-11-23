package com.citasalud;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la API de CitaSalud.
 * @EnableScheduling activa la tarea que genera los recordatorios de citas (HU_017).
 */
@SpringBootApplication
@EnableScheduling
public class CitaSaludApplication {

    public static void main(String[] args) {
        // Todas las fechas se manejan en la hora de Colombia, aunque el servidor esté en otro país.
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
        SpringApplication.run(CitaSaludApplication.class, args);
    }
}
