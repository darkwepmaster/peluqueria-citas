using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PeluqueriaWeb.Models;
using PeluqueriaWeb.Services;

namespace PeluqueriaWeb.Controllers;

[Authorize(Roles = "CLIENTE")]
public class NotificacionesController : Controller
{
    private readonly ApiClient _api;
    public NotificacionesController(ApiClient api) => _api = api;

    public async Task<IActionResult> Index() => View(await _api.Get<List<NotificacionDto>>("api/notificaciones/mias"));
}
