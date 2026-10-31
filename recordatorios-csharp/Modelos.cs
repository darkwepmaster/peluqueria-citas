namespace PeluqueriaRecordatorios;

public record LoginResponse(string Token, long Id, string Nombre, string Rol);

public record NotificacionDto(long Id, long CitaId, string Tipo, string Canal, string Mensaje, DateTime FechaProgramada,
                              DateTime? FechaEnvio, string EstadoEnvio, string CorreoDestino, string TelefonoDestino);
