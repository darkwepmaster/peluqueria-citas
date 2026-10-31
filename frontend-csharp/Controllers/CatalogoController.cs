using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PeluqueriaWeb.Models;
using PeluqueriaWeb.Services;

namespace PeluqueriaWeb.Controllers;

[AllowAnonymous]
public class CatalogoController : Controller
{
    private readonly ApiClient _api;
    public CatalogoController(ApiClient api) => _api = api;

    public async Task<IActionResult> Index(string? categoria, string? q)
    {
        var lista = await _api.Get<List<ServicioDto>>("api/servicios");
        ViewBag.Categorias = lista.Select(s => s.Categoria).Distinct().OrderBy(c => c).ToList();
        ViewBag.Categoria = categoria;
        ViewBag.Q = q;
        if (!string.IsNullOrEmpty(categoria)) lista = lista.Where(s => s.Categoria == categoria).ToList();
        if (!string.IsNullOrWhiteSpace(q)) lista = lista.Where(s => s.Nombre.Contains(q, StringComparison.OrdinalIgnoreCase)).ToList();
        return View(lista);
    }

    public IActionResult Error() => View();
}
