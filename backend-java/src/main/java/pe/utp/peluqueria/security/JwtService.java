package pe.utp.peluqueria.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.utp.peluqueria.model.Rol;
import pe.utp.peluqueria.model.Usuario;

/** Emite y valida tokens JWT firmados con HMAC-SHA. La clave se lee de la variable de entorno JWT_SECRET. */
@Service
public class JwtService {
    private final SecretKey clave;
    private final long horas;

    public JwtService(@Value("${app.jwt.secret}") String secreto, @Value("${app.jwt.horas}") long horas) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.horas = horas;
    }

    public String generar(Usuario u) {
        Instant ahora = Instant.now();
        return Jwts.builder().subject(u.getEmail()).claim("id", u.getId()).claim("rol", u.getRol().name())
                .issuedAt(Date.from(ahora)).expiration(Date.from(ahora.plus(horas, ChronoUnit.HOURS)))
                .signWith(clave).compact();
    }

    /** @throws io.jsonwebtoken.JwtException si el token es inválido o expiró */
    public Sesion leer(String token) {
        Claims c = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
        return new Sesion(((Number) c.get("id")).longValue(), c.getSubject(), Rol.valueOf(c.get("rol", String.class)));
    }
}
