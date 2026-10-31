using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PeluqueriaWeb.Models;
using PeluqueriaWeb.Services;

namespace PeluqueriaWeb.Controllers;

[Authorize(Roles = "ESTILISTA")]
public class AgendaController : Controller
{
    private readonly ApiClient _api;
    public AgendaController(ApiClient api) => _api = api;

    public async Task<IActionResult> Index(DateOnly? fecha)
    {
        var dia = fecha ?? DateOnly.FromDateTime(DateTime.Today);
        ViewBag.Fecha = dia;
        return View(await _api.Get<List<CitaDto>>($"api/agenda?fecha={dia:yyyy-MM-dd}"));
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Cerrar(long id, bool asistio, DateOnly fecha)
    {
        await _api.Post<CitaDto>($"api/agenda/{id}/{(asistio ? "atendida" : "ausente")}");
        TempData["Ok"] = asistio ? "Cita marcada como atendida." : "Cita marcada como ausente.";
        return RedirectToAction(nameof(Index), new { fecha });
    }
}
