package pe.utp.peluqueria.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Objetos de transferencia de datos (entrada y salida de la API). Las entradas se validan con Bean Validation. */
public final class Dtos {
    private Dtos() { }

    // ---- Entrada
    public record RegistroRequest(@NotBlank String nombres, @NotBlank String apellidos, @NotBlank @Email String email,
                                  @NotBlank String telefono, @NotBlank @Size(min = 8, message = "mínimo 8 caracteres") String password,
                                  boolean aceptaWhatsapp, boolean aceptaCorreo) { }
    public record LoginRequest(@NotBlank String email, @NotBlank String password) { }
    public record ReservaRequest(@NotNull Long estilistaId, @NotNull LocalDate fecha, @NotNull LocalTime horaInicio, @NotEmpty List<Long> servicioIds) { }
    public record ReprogramarRequest(@NotNull LocalDate fecha, @NotNull LocalTime horaInicio) { }
    public record CancelarRequest(@Size(max = 200) String motivo) { }
    public record ServicioRequest(@NotBlank String nombre, String descripcion, @NotBlank String categoria,
                                  @NotNull @DecimalMin("0.0") BigDecimal precio, @Min(5) int duracionMin, boolean activo) { }
    public record HorarioDto(@Min(1) @Max(7) int diaSemana, @NotNull LocalTime horaInicio, @NotNull LocalTime horaFin) { }
    public record EstilistaRequest(@NotBlank String nombres, @NotBlank String apellidos, @NotBlank @Email String email,
                                   @NotBlank String telefono, @NotBlank @Size(min = 8) String password,
                                   @NotBlank String especialidad, List<HorarioDto> horarios) { }

    // ---- Salida
    public record LoginResponse(String token, Long id, String nombre, String rol) { }
    public record ServicioDto(Long id, String nombre, String descripcion, String categoria, BigDecimal precio, int duracionMin, boolean activo) { }
    public record EstilistaDto(Long id, String nombre, String especialidad, boolean activo, List<HorarioDto> horarios) { }
    public record ClienteDto(Long id, String nombre, String email, String telefono, long totalCitas, LocalDate ultimaVisita) { }
    public record CitaDto(Long id, Long clienteId, String clienteNombre, Long estilistaId, String estilistaNombre, LocalDate fecha,
                          LocalTime horaInicio, LocalTime horaFin, String estado, BigDecimal total, List<String> servicios, String motivoCancelacion) { }
    public record NotificacionDto(Long id, Long citaId, String tipo, String canal, String mensaje, LocalDateTime fechaProgramada,
                                  LocalDateTime fechaEnvio, String estadoEnvio, String correoDestino, String telefonoDestino) { }
    public record ConteoDto(String etiqueta, long valor) { }
    public record OcupacionDto(String estilista, long minutosAtendidos, long minutosDisponibles, double porcentaje) { }
    public record ReporteCitasDto(long total, long atendidas, long canceladas, long ausentes, List<ConteoDto> porEstado, List<ConteoDto> porSemana) { }
}
