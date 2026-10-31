package pe.utp.peluqueria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pe.utp.peluqueria.dto.Dtos.LoginRequest;
import pe.utp.peluqueria.dto.Dtos.LoginResponse;
import pe.utp.peluqueria.exception.ApiException;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.ClienteRepository;
import pe.utp.peluqueria.repository.UsuarioRepository;
import pe.utp.peluqueria.security.JwtService;

/** Seguridad: hash BCrypt, token JWT y bloqueo tras 5 intentos fallidos. */
class AuthServiceTest {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private AuthService auth;

    @BeforeEach
    void preparar() {
        auth = new AuthService(usuarios, mock(ClienteRepository.class), encoder, new JwtService("clave-de-prueba-de-al-menos-32-bytes-12345", 1));
    }

    @Test
    void loginCorrectoDevuelveTokenYRol() {
        Cliente c = new Cliente();
        c.setId(7L); c.setNombres("Valeria"); c.setApellidos("Rojas"); c.setEmail("valeria@correo.com");
        c.setPasswordHash(encoder.encode("Secreta123!")); c.setRol(Rol.CLIENTE); c.setActivo(true);
        when(usuarios.findByEmail("valeria@correo.com")).thenReturn(Optional.of(c));
        LoginResponse r = auth.login(new LoginRequest("  VALERIA@correo.com ", "Secreta123!"));
        assertEquals("CLIENTE", r.rol());
        assertFalse(r.token().isBlank());
    }

    @Test
    void tras5IntentosFallidosBloqueaLaCuenta() {
        when(usuarios.findByEmail(anyString())).thenReturn(Optional.empty());
        LoginRequest malo = new LoginRequest("x@correo.com", "incorrecta");
        for (int i = 0; i < AuthService.MAX_INTENTOS; i++)
            assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(ApiException.class, () -> auth.login(malo)).getStatus());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, assertThrows(ApiException.class, () -> auth.login(malo)).getStatus());
    }

    @Test
    void laContrasenaNoSeGuardaEnTextoPlano() {
        String hash = encoder.encode("Secreta123!");
        assertNotEquals("Secreta123!", hash);
        assertTrue(encoder.matches("Secreta123!", hash));
    }
}
