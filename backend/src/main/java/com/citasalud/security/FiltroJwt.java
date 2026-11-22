package com.citasalud.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que lee el encabezado "Authorization: Bearer <token>" de cada petición.
 * Es un eslabón de la cadena de filtros de Spring Security (patrón Chain of Responsibility).
 */
@Component
public class FiltroJwt extends OncePerRequestFilter {

    private final JwtServicio jwtServicio;

    public FiltroJwt(JwtServicio jwtServicio) {
        this.jwtServicio = jwtServicio;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String encabezado = peticion.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            UsuarioActual usuario = jwtServicio.leer(encabezado.substring(7));
            if (usuario != null) {
                var autenticacion = new UsernamePasswordAuthenticationToken(usuario, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol().name())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        }
        cadena.doFilter(peticion, respuesta);
    }
}
