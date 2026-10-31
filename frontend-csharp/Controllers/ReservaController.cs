using System.Net;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PeluqueriaWeb.Models;
using PeluqueriaWeb.Services;

namespace PeluqueriaWeb.Controllers;

/// <summary>Asistente de reserva en 4 pasos: servicio (catálogo) → estilista → horario → confirmación.</summary>
[Authorize(Roles = "CLIENTE")]
public class ReservaController : Controller
{
    private readonly ApiClient _api;
    public ReservaController(ApiClient api) => _api = api;

    private async Task<ServicioDto> Servicio(long id) =>
        (await _api.Get<List<ServicioDto>>("api/servicios")).FirstOrDefault(s => s.Id == id)
        ?? throw new ApiException(HttpStatusCode.NotFound, "El servicio no existe o no está disponible.");

    private async Task<EstilistaDto> Estilista(long id) =>
        (await _api.Get<List<EstilistaDto>>("api/estilistas")).FirstOrDefault(e => e.Id == id)
        ?? throw new ApiException(HttpStatusCode.NotFound, "El estilista no existe o no está disponible.");

    public async Task<IActionResult> Index(long servicioId)
    {
        ViewBag.Servicio = await Servicio(servicioId);
        return View("Estilista", await _api.Get<List<EstilistaDto>>("api/estilistas"));
    }

    public async Task<IActionResult> Horario(long servicioId, long estilistaId, DateOnly? fecha)
    {
        var dia = fecha ?? DateOnly.FromDateTime(DateTime.Today);
        var vm = new HorarioVm { Servicio = await Servicio(servicioId), Estilista = await Estilista(estilistaId), Fecha = dia };
        vm.Libres = await _api.Get<List<TimeOnly>>($"api/estilistas/{estilistaId}/horarios-libres?fecha={dia:yyyy-MM-dd}&servicioIds={servicioId}");
        return View(vm);
    }

    public async Task<IActionResult> Confirmar(long servicioId, long estilistaId, DateOnly fecha, TimeOnly hora) =>
        View(new ConfirmarVm { Servicio = await Servicio(servicioId), Estilista = await Estilista(estilistaId), Fecha = fecha, Hora = hora });

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Crear(long servicioId, long estilistaId, DateOnly fecha, TimeOnly hora)
    {
        await _api.Post<CitaDto>("api/citas", new ReservaRequest(estilistaId, fecha, hora, new List<long> { servicioId }));
        TempData["Ok"] = "Cita registrada. Recibirás la confirmación y los recordatorios por tus canales elegidos.";
        return RedirectToAction("Index", "MisCitas");
    }
}
