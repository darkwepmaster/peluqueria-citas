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

/** Controlador REST de citas y notificaciones del cliente autenticado (y del worker de recordatorios). */
@RestController
@RequestMapping("/api")
public class CitaController {
    private final CitaService citas;
    private final NotificacionService notificaciones;

    public CitaController(CitaService citas, NotificacionService notificaciones) { this.citas = citas; this.notificaciones = notificaciones; }

    @PostMapping("/citas")
    public CitaDto crear(@AuthenticationPrincipal Sesion s, @Valid @RequestBody ReservaRequest r) { return citas.crear(s.id(), r); }

    @GetMapping("/citas/mias")
    public List<CitaDto> mias(@AuthenticationPrincipal Sesion s) { return citas.delCliente(s.id()); }

    @GetMapping("/citas/{id}/horarios-libres")
    public List<LocalTime> horarios(@AuthenticationPrincipal Sesion s, @PathVariable Long id,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citas.horariosParaReprogramar(id, fecha, s);
    }

    @PostMapping("/citas/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@AuthenticationPrincipal Sesion s, @PathVariable Long id, @Valid @RequestBody(required = false) CancelarRequest r) {
        citas.cancelar(id, s, r == null ? null : r.motivo());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/citas/{id}/reprogramar")
    public CitaDto reprogramar(@AuthenticationPrincipal Sesion s, @PathVariable Long id, @Valid @RequestBody ReprogramarRequest r) {
        return citas.reprogramar(id, s, r);
    }

    @GetMapping("/notificaciones/mias")
    public List<NotificacionDto> notificaciones(@AuthenticationPrincipal Sesion s) { return notificaciones.delCliente(s.id()); }

    @GetMapping("/notificaciones/pendientes")
    public List<NotificacionDto> pendientes() { return notificaciones.pendientes(); }

    @PostMapping("/notificaciones/{id}/enviada")
    public ResponseEntity<Void> enviada(@PathVariable Long id) { notificaciones.marcarEnviada(id); return ResponseEntity.noContent().build(); }

    @PostMapping("/notificaciones/{id}/fallida")
    public ResponseEntity<Void> fallida(@PathVariable Long id) { notificaciones.marcarFallida(id); return ResponseEntity.noContent().build(); }
}
