package com.citasalud.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.citasalud.security.FiltroJwt;

/**
 * Seguridad de la API (RNF_004): rutas públicas, JWT sin sesión y permisos por rol
 * (los permisos finos están en cada controller con @PreAuthorize).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String JSON_401 = "{\"estado\":401,\"error\":\"Unauthorized\",\"mensaje\":\"Debes iniciar sesión para usar esta función\"}";
    private static final String JSON_403 = "{\"estado\":403,\"error\":\"Forbidden\",\"mensaje\":\"No tienes permiso para esta acción\"}";

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http, FiltroJwt filtroJwt) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/registro", "/api/auth/login", "/api/estado").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/especialidades", "/api/especialidades/{id}",
                        "/api/especialidades/{id}/valor", "/api/medicos", "/api/medicos/{id}",
                        "/api/medicos/{id}/disponibilidad").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> escribir(res, 401, JSON_401))
                .accessDeniedHandler((req, res, ex) -> escribir(res, 403, JSON_403)))
            .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void escribir(jakarta.servlet.http.HttpServletResponse respuesta, int estado, String json)
            throws java.io.IOException {
        respuesta.setStatus(estado);
        respuesta.setContentType("application/json;charset=UTF-8");
        respuesta.getWriter().write(json);
    }

    @Bean
    public PasswordEncoder codificadorContrasenas() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origenes}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", config);
        return fuente;
    }
}
