package com.citasalud.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.citasalud.model.Rol;
import com.citasalud.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Crea y lee los tokens JWT con los que el usuario se identifica en cada petición. */
@Service
public class JwtServicio {

    private final SecretKey clave;
    private final Duration validez;

    public JwtServicio(@Value("${app.jwt.secreto}") String secreto,
                       @Value("${app.jwt.horas-validez}") long horasValidez) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.validez = Duration.ofHours(horasValidez);
    }

    public String generar(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getId())
                .claim("correo", usuario.getCorreo())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + validez.toMillis()))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el usuario del token o null si el token no es válido o venció. */
    public UsuarioActual leer(String token) {
        try {
            Claims datos = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return new UsuarioActual(datos.getSubject(), datos.get("correo", String.class),
                    Rol.valueOf(datos.get("rol", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
