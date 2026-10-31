package pe.utp.peluqueria.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.dto.Dtos.*;
import pe.utp.peluqueria.exception.ApiException;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.ClienteRepository;
import pe.utp.peluqueria.repository.UsuarioRepository;
import pe.utp.peluqueria.security.JwtService;
import pe.utp.peluqueria.util.Sanitizador;

/**
 * Registro e inicio de sesión. Seguridad: contraseñas con BCrypt y bloqueo temporal tras 5 intentos fallidos
 * (contador en un {@link Cache} de Guava que expira a los 15 minutos).
 */
@Service
@Transactional
public class AuthService {
    static final int MAX_INTENTOS = 5;
    private static final Logger AUDITORIA = LoggerFactory.getLogger("AUDITORIA");

    private final Cache<String, AtomicInteger> fallidos = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES).maximumSize(10_000).build();
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, ClienteRepository clientes, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios; this.clientes = clientes; this.encoder = encoder; this.jwt = jwt;
    }

    public void registrar(RegistroRequest r) {
        String email = Sanitizador.correo(r.email());
        if (usuarios.existsByEmail(email)) throw ApiException.conflicto("Ya existe una cuenta con ese correo.");
        Cliente c = new Cliente();
        c.setNombres(Sanitizador.limpiar(r.nombres()));
        c.setApellidos(Sanitizador.limpiar(r.apellidos()));
        c.setEmail(email);
        c.setTelefono(Sanitizador.limpiar(r.telefono()));
        c.setPasswordHash(encoder.encode(r.password()));
        c.setRol(Rol.CLIENTE);
        c.setActivo(true);
        c.setAceptaWhatsapp(r.aceptaWhatsapp());
        c.setAceptaCorreo(r.aceptaCorreo());
        clientes.save(c);
        AUDITORIA.info("REGISTRO_CLIENTE correo={}", Sanitizador.enmascararCorreo(email));
    }

    public LoginResponse login(LoginRequest r) {
        String email = Sanitizador.correo(r.email());
        AtomicInteger intentos = fallidos.getIfPresent(email);
        if (intentos != null && intentos.get() >= MAX_INTENTOS)
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos fallidos. Intente nuevamente en 15 minutos.");
        Usuario u = usuarios.findByEmail(email).filter(Usuario::isActivo).orElse(null);
        if (u == null || !encoder.matches(r.password(), u.getPasswordHash())) {
            fallidos.asMap().computeIfAbsent(email, k -> new AtomicInteger()).incrementAndGet();
            AUDITORIA.warn("LOGIN_FALLIDO correo={}", Sanitizador.enmascararCorreo(email));
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
        }
        fallidos.invalidate(email);
        AUDITORIA.info("LOGIN_OK usuario={} rol={}", u.getId(), u.getRol());
        return new LoginResponse(jwt.generar(u), u.getId(), u.getNombreCompleto(), u.getRol().name());
    }
}
