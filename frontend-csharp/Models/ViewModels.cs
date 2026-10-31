using System.ComponentModel.DataAnnotations;

namespace PeluqueriaWeb.Models;

public class LoginVm
{
    [Required(ErrorMessage = "Ingrese su correo"), EmailAddress] public string Email { get; set; } = "";
    [Required(ErrorMessage = "Ingrese su contraseña")] public string Password { get; set; } = "";
}

public class RegistroVm
{
    [Required] public string Nombres { get; set; } = "";
    [Required] public string Apellidos { get; set; } = "";
    [Required, EmailAddress] public string Email { get; set; } = "";
    [Required] public string Telefono { get; set; } = "";
    [Required, MinLength(8, ErrorMessage = "Mínimo 8 caracteres")] public string Password { get; set; } = "";
    [Compare(nameof(Password), ErrorMessage = "Las contraseñas no coinciden")] public string Confirmar { get; set; } = "";
    public bool AceptaWhatsapp { get; set; } = true;
    public bool AceptaCorreo { get; set; } = true;
}

public class HorarioVm
{
    public ServicioDto Servicio { get; set; } = null!;
    public EstilistaDto Estilista { get; set; } = null!;
    public DateOnly Fecha { get; set; }
    public List<TimeOnly> Libres { get; set; } = new();
    public IEnumerable<DateOnly> Dias => Enumerable.Range(0, 7).Select(i => DateOnly.FromDateTime(DateTime.Today).AddDays(i));
}

public class ConfirmarVm
{
    public ServicioDto Servicio { get; set; } = null!;
    public EstilistaDto Estilista { get; set; } = null!;
    public DateOnly Fecha { get; set; }
    public TimeOnly Hora { get; set; }
    public TimeOnly Fin => Hora.AddMinutes(Servicio.DuracionMin);
}

public class ReprogramarVm
{
    public CitaDto Cita { get; set; } = null!;
    public DateOnly Fecha { get; set; }
    public List<TimeOnly> Libres { get; set; } = new();
    public IEnumerable<DateOnly> Dias => Enumerable.Range(0, 7).Select(i => DateOnly.FromDateTime(DateTime.Today).AddDays(i));
}

public class PanelVm
{
    public List<CitaDto> Citas { get; set; } = new();
    public int Total => Citas.Count;
    public int Pendientes => Citas.Count(c => c.Estado == "PENDIENTE");
    public int Atendidas => Citas.Count(c => c.Estado == "ATENDIDA");
    public int Canceladas => Citas.Count(c => c.Estado == "CANCELADA");
}

public class ReporteVm
{
    public DateOnly Desde { get; set; }
    public DateOnly Hasta { get; set; }
    public ReporteCitasDto? Citas { get; set; }
    public List<ConteoDto> Servicios { get; set; } = new();
    public List<OcupacionDto> Ocupacion { get; set; } = new();
}
