using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PeluqueriaWeb.Models;
using PeluqueriaWeb.Services;

namespace PeluqueriaWeb.Controllers;

[Authorize(Roles = "ADMINISTRADOR")]
public class AdminController : Controller
{
    private readonly ApiClient _api;
    public AdminController(ApiClient api) => _api = api;

    private static DateOnly Hoy => DateOnly.FromDateTime(DateTime.Today);

    // ---- Panel y citas
    public async Task<IActionResult> Panel() => View(new PanelVm { Citas = await _api.Get<List<CitaDto>>($"api/admin/citas?fecha={Hoy:yyyy-MM-dd}") });

    public async Task<IActionResult> Citas(DateOnly? fecha, string? estado)
    {
        ViewBag.Fecha = fecha; ViewBag.Estado = estado;
        var url = "api/admin/citas?" + (fecha.HasValue ? $"fecha={fecha:yyyy-MM-dd}&" : "") + (string.IsNullOrEmpty(estado) ? "" : $"estado={estado}");
        return View(await _api.Get<List<CitaDto>>(url));
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Confirmar(long id)
    {
        await _api.Post<CitaDto>($"api/admin/citas/{id}/confirmar");
        TempData["Ok"] = "Cita confirmada. Se notificó al cliente.";
        return RedirectToAction(nameof(Citas));
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Cancelar(long id, string? motivo)
    {
        await _api.Post($"api/admin/citas/{id}/cancelar", new { motivo });
        TempData["Ok"] = "Cita cancelada.";
        return RedirectToAction(nameof(Citas));
    }

    // ---- Servicios
    public async Task<IActionResult> Servicios(long? editar)
    {
        var lista = await _api.Get<List<ServicioDto>>("api/admin/servicios");
        ViewBag.Editar = editar.HasValue ? lista.FirstOrDefault(s => s.Id == editar) : null;
        return View(lista);
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> GuardarServicio(long? id, string nombre, string? descripcion, string categoria, decimal precio, int duracionMin, bool activo)
    {
        var req = new ServicioRequest(nombre, descripcion, categoria, precio, duracionMin, activo);
        if (id.HasValue) await _api.Put<ServicioDto>($"api/admin/servicios/{id}", req);
        else await _api.Post<ServicioDto>("api/admin/servicios", req);
        TempData["Ok"] = "Servicio guardado.";
        return RedirectToAction(nameof(Servicios));
    }

    // ---- Estilistas y horarios
    public async Task<IActionResult> Estilistas(long? horarioDe)
    {
        var lista = await _api.Get<List<EstilistaDto>>("api/admin/estilistas");
        ViewBag.Seleccionado = horarioDe.HasValue ? lista.FirstOrDefault(e => e.Id == horarioDe) : null;
        return View(lista);
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> CrearEstilista(string nombres, string apellidos, string email, string telefono, string password, string especialidad)
    {
        await _api.Post<EstilistaDto>("api/admin/estilistas", new { nombres, apellidos, email, telefono, password, especialidad, horarios = new List<HorarioDto>() });
        TempData["Ok"] = "Estilista registrado. Defina ahora su horario semanal.";
        return RedirectToAction(nameof(Estilistas));
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> GuardarHorario(long id, string[] inicio, string[] fin)
    {
        var horarios = new List<HorarioDto>();
        for (var i = 0; i < 7 && i < inicio.Length && i < fin.Length; i++)
            if (TimeOnly.TryParse(inicio[i], out var a) && TimeOnly.TryParse(fin[i], out var b)) horarios.Add(new HorarioDto(i + 1, a, b));
        await _api.Put<EstilistaDto>($"api/admin/estilistas/{id}/horarios", horarios);
        TempData["Ok"] = "Horario guardado.";
        return RedirectToAction(nameof(Estilistas), new { horarioDe = id });
    }

    // ---- Clientes y reportes
    public async Task<IActionResult> Clientes(string? q)
    {
        ViewBag.Q = q;
        return View(await _api.Get<List<ClienteDto>>("api/admin/clientes?q=" + Uri.EscapeDataString(q ?? "")));
    }

    public async Task<IActionResult> Reportes(DateOnly? desde, DateOnly? hasta)
    {
        var h = hasta ?? Hoy;
        var d = desde ?? new DateOnly(h.Year, h.Month, 1);
        var q = $"desde={d:yyyy-MM-dd}&hasta={h:yyyy-MM-dd}";
        return View(new ReporteVm
        {
            Desde = d, Hasta = h,
            Citas = await _api.Get<ReporteCitasDto>("api/admin/reportes/citas?" + q),
            Servicios = await _api.Get<List<ConteoDto>>("api/admin/reportes/servicios?" + q),
            Ocupacion = await _api.Get<List<OcupacionDto>>("api/admin/reportes/ocupacion?" + q)
        });
    }

    /// <summary>Descarga el reporte generado por la API Java (Apache POI para xlsx, Commons CSV para csv).</summary>
    public async Task<IActionResult> Exportar(DateOnly desde, DateOnly hasta, string formato = "xlsx")
    {
        var f = formato == "csv" ? "csv" : "xlsx";
        var (bytes, mime) = await _api.Descargar($"api/admin/reportes/exportar?desde={desde:yyyy-MM-dd}&hasta={hasta:yyyy-MM-dd}&formato={f}");
        return File(bytes, mime, $"citas_{desde:yyyyMMdd}_{hasta:yyyyMMdd}.{f}");
    }
}
