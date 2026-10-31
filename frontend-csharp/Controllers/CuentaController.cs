using System.Security.Claims;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PeluqueriaWeb.Models;
using PeluqueriaWeb.Services;

namespace PeluqueriaWeb.Controllers;

[AllowAnonymous]
public class CuentaController : Controller
{
    private readonly ApiClient _api;
    public CuentaController(ApiClient api) => _api = api;

    [HttpGet] public IActionResult Login() => View(new LoginVm());

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Login(LoginVm vm)
    {
        if (!ModelState.IsValid) return View(vm);
        var r = await _api.Post<LoginResponse>("api/auth/login", new { vm.Email, vm.Password });
        var claims = new List<Claim>
        {
            new(ClaimTypes.NameIdentifier, r.Id.ToString()), new(ClaimTypes.Name, r.Nombre),
            new(ClaimTypes.Role, r.Rol), new("token", r.Token)
        };
        await HttpContext.SignInAsync(new ClaimsPrincipal(new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme)));
        return r.Rol switch
        {
            "ADMINISTRADOR" => RedirectToAction("Panel", "Admin"),
            "ESTILISTA" => RedirectToAction("Index", "Agenda"),
            _ => RedirectToAction("Index", "Catalogo")
        };
    }

    [HttpGet] public IActionResult Registro() => View(new RegistroVm());

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Registro(RegistroVm vm)
    {
        if (!ModelState.IsValid) return View(vm);
        await _api.Post("api/auth/registro", new { vm.Nombres, vm.Apellidos, vm.Email, vm.Telefono, vm.Password, vm.AceptaWhatsapp, vm.AceptaCorreo });
        TempData["Ok"] = "Cuenta creada. Ahora puedes iniciar sesión.";
        return RedirectToAction(nameof(Login));
    }

    public async Task<IActionResult> Salir()
    {
        await HttpContext.SignOutAsync();
        return RedirectToAction(nameof(Login));
    }

    public IActionResult Denegado() => View();
}
