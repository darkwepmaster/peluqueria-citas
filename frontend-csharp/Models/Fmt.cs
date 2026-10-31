using System.Globalization;

namespace PeluqueriaWeb.Models;

/// <summary>Formatos de fecha, hora y dinero en español para las vistas.</summary>
public static class Fmt
{
    private static readonly CultureInfo Es = CultureInfo.GetCultureInfo("es-PE");
    public static string Fecha(DateOnly f) => f.ToString("ddd dd MMM yyyy", Es);
    public static string FechaLarga(DateOnly f) => f.ToString("dddd d 'de' MMMM 'de' yyyy", Es);
    public static string Hora(TimeOnly h) => h.ToString("HH:mm", Es);
    public static string Dinero(decimal d) => "S/ " + d.ToString("N2", Es);
    public static string Dia(int diaSemana) => new[] { "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo" }[diaSemana - 1];
}
