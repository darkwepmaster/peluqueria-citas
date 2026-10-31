using System.Net;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PeluqueriaWeb.Models;
using PeluqueriaWeb.Services;

namespace PeluqueriaWeb.Controllers;

[Authorize(Roles = "CLIENTE,ADMINISTRADOR")]
public class MisCitasController : Controller
{
    private readonly ApiClient _api;
    public MisCitasController(ApiClient api) => _api = api;

    [Authorize(Roles = "CLIENTE")]
    public async Task<IActionResult> Index() => View(await _api.Get<List<CitaDto>>("api/citas/mias"));

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Cancelar(long id, string? motivo)
    {
        await _api.Post($"api/citas/{id}/cancelar", new { motivo });
        TempData["Ok"] = "La cita fue cancelada y el horario quedó libre.";
        return Volver();
    }

    [HttpGet]
    public async Task<IActionResult> Reprogramar(long id, DateOnly? fecha)
    {
        var dia = fecha ?? DateOnly.FromDateTime(DateTime.Today);
        var citas = User.IsInRole("ADMINISTRADOR") ? await _api.Get<List<CitaDto>>("api/admin/citas") : await _api.Get<List<CitaDto>>("api/citas/mias");
        var cita = citas.FirstOrDefault(c => c.Id == id) ?? throw new ApiException(HttpStatusCode.NotFound, "Cita no encontrada.");
        var libres = await _api.Get<List<TimeOnly>>($"api/citas/{id}/horarios-libres?fecha={dia:yyyy-MM-dd}");
        return View(new ReprogramarVm { Cita = cita, Fecha = dia, Libres = libres });
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Reprogramar(long id, DateOnly fecha, TimeOnly hora)
    {
        await _api.Post<CitaDto>($"api/citas/{id}/reprogramar", new ReprogramarRequest(fecha, hora));
        TempData["Ok"] = "La cita fue reprogramada y quedó pendiente de confirmación.";
        return Volver();
    }

    private IActionResult Volver() => User.IsInRole("ADMINISTRADOR") ? RedirectToAction("Citas", "Admin") : RedirectToAction(nameof(Index));
}
