package pe.utp.peluqueria.controller;

import java.time.*;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.utp.peluqueria.dto.Dtos.*;
import pe.utp.peluqueria.export.ExportService;
import pe.utp.peluqueria.security.Sesion;
import pe.utp.peluqueria.service.*;

/** Controlador REST público: registro, inicio de sesión, catálogo y horarios libres. */
@RestController
@RequestMapping("/api")
public class PublicoController {
    private final AuthService auth;
    private final ServicioService servicios;
    private final EstilistaService estilistas;
    private final CitaService citas;

    public PublicoController(AuthService auth, ServicioService servicios, EstilistaService estilistas, CitaService citas) {
        this.auth = auth; this.servicios = servicios; this.estilistas = estilistas; this.citas = citas;
    }

    @PostMapping("/auth/registro")
    public ResponseEntity<Void> registro(@Valid @RequestBody RegistroRequest r) { auth.registrar(r); return ResponseEntity.status(201).build(); }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r) { return auth.login(r); }

    @GetMapping("/servicios")
    public List<ServicioDto> servicios() { return servicios.activos(); }

    @GetMapping("/estilistas")
    public List<EstilistaDto> estilistas() { return estilistas.activos(); }

    @GetMapping("/estilistas/{id}/horarios-libres")
    public List<LocalTime> horariosLibres(@PathVariable Long id, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                          @RequestParam List<Long> servicioIds) {
        return citas.horariosLibres(id, fecha, servicioIds);
    }
}
