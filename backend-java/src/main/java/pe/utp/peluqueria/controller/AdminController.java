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

/** Controlador REST de administración: citas, servicios, estilistas, clientes y reportes (solo rol ADMINISTRADOR). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final CitaService citas;
    private final ServicioService servicios;
    private final EstilistaService estilistas;
    private final ClienteService clientes;
    private final ReporteService reportes;
    private final ExportService exportacion;

    public AdminController(CitaService citas, ServicioService servicios, EstilistaService estilistas, ClienteService clientes,
                           ReporteService reportes, ExportService exportacion) {
        this.citas = citas; this.servicios = servicios; this.estilistas = estilistas;
        this.clientes = clientes; this.reportes = reportes; this.exportacion = exportacion;
    }

    @GetMapping("/citas")
    public List<CitaDto> citas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                               @RequestParam(required = false) String estado) { return citas.listarAdmin(fecha, estado); }

    @PostMapping("/citas/{id}/confirmar")
    public CitaDto confirmar(@PathVariable Long id) { return citas.confirmar(id); }

    @PostMapping("/citas/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@AuthenticationPrincipal Sesion s, @PathVariable Long id, @Valid @RequestBody(required = false) CancelarRequest r) {
        citas.cancelar(id, s, r == null ? null : r.motivo());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/servicios")
    public List<ServicioDto> servicios() { return servicios.todos(); }

    @PostMapping("/servicios")
    public ServicioDto crearServicio(@Valid @RequestBody ServicioRequest r) { return servicios.crear(r); }

    @PutMapping("/servicios/{id}")
    public ServicioDto actualizarServicio(@PathVariable Long id, @Valid @RequestBody ServicioRequest r) { return servicios.actualizar(id, r); }

    @GetMapping("/estilistas")
    public List<EstilistaDto> estilistas() { return estilistas.todos(); }

    @PostMapping("/estilistas")
    public EstilistaDto crearEstilista(@Valid @RequestBody EstilistaRequest r) { return estilistas.crear(r); }

    @PutMapping("/estilistas/{id}/horarios")
    public EstilistaDto horarios(@PathVariable Long id, @Valid @RequestBody List<HorarioDto> horarios) { return estilistas.actualizarHorarios(id, horarios); }

    @PostMapping("/estilistas/{id}/estado")
    public ResponseEntity<Void> estado(@PathVariable Long id, @RequestParam boolean activo) { estilistas.cambiarEstado(id, activo); return ResponseEntity.noContent().build(); }

    @GetMapping("/clientes")
    public List<ClienteDto> clientes(@RequestParam(required = false) String q) { return clientes.listar(q); }

    @GetMapping("/reportes/citas")
    public ReporteCitasDto reporteCitas(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) { return reportes.citas(desde, hasta); }

    @GetMapping("/reportes/servicios")
    public List<ConteoDto> reporteServicios(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) { return reportes.servicios(desde, hasta); }

    @GetMapping("/reportes/ocupacion")
    public List<OcupacionDto> reporteOcupacion(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) { return reportes.ocupacion(desde, hasta); }

    /** Descarga el reporte de citas en Excel (xlsx) o CSV. */
    @GetMapping("/reportes/exportar")
    public ResponseEntity<byte[]> exportar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                           @RequestParam(defaultValue = "xlsx") String formato) {
        ExportService.Archivo a = exportacion.generar(formato, desde, hasta);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + a.nombre() + "\"")
                .contentType(MediaType.parseMediaType(a.contentType())).body(a.contenido());
    }
}
