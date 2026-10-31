namespace PeluqueriaWeb.Models;

// ---- Respuestas de la API Java
public record LoginResponse(string Token, long Id, string Nombre, string Rol);
public record ServicioDto(long Id, string Nombre, string? Descripcion, string Categoria, decimal Precio, int DuracionMin, bool Activo);
public record HorarioDto(int DiaSemana, TimeOnly HoraInicio, TimeOnly HoraFin);
public record EstilistaDto(long Id, string Nombre, string Especialidad, bool Activo, List<HorarioDto> Horarios);
public record ClienteDto(long Id, string Nombre, string Email, string Telefono, long TotalCitas, DateOnly? UltimaVisita);
public record CitaDto(long Id, long ClienteId, string ClienteNombre, long EstilistaId, string EstilistaNombre,
                      DateOnly Fecha, TimeOnly HoraInicio, TimeOnly HoraFin, string Estado, decimal Total,
                      List<string> Servicios, string? MotivoCancelacion)
{
    public bool EsActiva => Estado is "PENDIENTE" or "CONFIRMADA";
    public string Descripcion => string.Join(", ", Servicios);
}
public record NotificacionDto(long Id, long CitaId, string Tipo, string Canal, string Mensaje, DateTime FechaProgramada,
                              DateTime? FechaEnvio, string EstadoEnvio, string CorreoDestino, string TelefonoDestino);
public record ConteoDto(string Etiqueta, long Valor);
public record OcupacionDto(string Estilista, long MinutosAtendidos, long MinutosDisponibles, double Porcentaje);
public record ReporteCitasDto(long Total, long Atendidas, long Canceladas, long Ausentes, List<ConteoDto> PorEstado, List<ConteoDto> PorSemana);

// ---- Solicitudes hacia la API Java
public record ReservaRequest(long EstilistaId, DateOnly Fecha, TimeOnly HoraInicio, List<long> ServicioIds);
public record ReprogramarRequest(DateOnly Fecha, TimeOnly HoraInicio);
public record ServicioRequest(string Nombre, string? Descripcion, string Categoria, decimal Precio, int DuracionMin, bool Activo);
