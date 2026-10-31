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

/** Controlador REST de la agenda del estilista autenticado. */
@RestController
@RequestMapping("/api/agenda")
public class AgendaController {
    private final CitaService citas;

    public AgendaController(CitaService citas) { this.citas = citas; }

    @GetMapping
    public List<CitaDto> agenda(@AuthenticationPrincipal Sesion s, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citas.agenda(s.id(), fecha == null ? LocalDate.now() : fecha);
    }

    @PostMapping("/{id}/atendida")
    public CitaDto atendida(@AuthenticationPrincipal Sesion s, @PathVariable Long id) { return citas.cerrar(id, s, true); }

    @PostMapping("/{id}/ausente")
    public CitaDto ausente(@AuthenticationPrincipal Sesion s, @PathVariable Long id) { return citas.cerrar(id, s, false); }
}
