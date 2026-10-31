using System.Net;
using System.Security.Claims;
using System.Text;
using Microsoft.AspNetCore.Http;
using PeluqueriaWeb.Models;
using PeluqueriaWeb.Services;
using Xunit;

namespace PeluqueriaWeb.Tests;

public class ApiClientTests
{
    private sealed class Falso : HttpMessageHandler
    {
        public HttpRequestMessage? Ultima;
        private readonly HttpStatusCode _codigo; private readonly string _cuerpo;
        public Falso(HttpStatusCode codigo, string cuerpo) { _codigo = codigo; _cuerpo = cuerpo; }
        protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage r, CancellationToken ct)
        {
            Ultima = r;
            return Task.FromResult(new HttpResponseMessage(_codigo) { Content = new StringContent(_cuerpo, Encoding.UTF8, "application/json") });
        }
    }

    private static (ApiClient, Falso) Crear(HttpStatusCode codigo, string cuerpo, string? token = "abc")
    {
        var h = new Falso(codigo, cuerpo);
        var ctx = new DefaultHttpContext();
        if (token != null) ctx.User = new ClaimsPrincipal(new ClaimsIdentity(new[] { new Claim("token", token) }, "t"));
        return (new ApiClient(new HttpClient(h) { BaseAddress = new Uri("http://api/") }, new HttpContextAccessor { HttpContext = ctx }), h);
    }

    [Fact]
    public async Task DeserializaLaRespuestaYEnviaElTokenJwt()
    {
        var (api, falso) = Crear(HttpStatusCode.OK, "[{\"id\":1,\"nombre\":\"Corte\",\"descripcion\":null,\"categoria\":\"Corte\",\"precio\":35.0,\"duracionMin\":40,\"activo\":true}]");
        var lista = await api.Get<List<ServicioDto>>("api/servicios");
        Assert.Single(lista);
        Assert.Equal(40, lista[0].DuracionMin);
        Assert.Equal("Bearer abc", falso.Ultima!.Headers.Authorization!.ToString());
    }

    [Fact]
    public async Task MuestraElMensajeDeErrorQueDevuelveLaApi()
    {
        var (api, _) = Crear(HttpStatusCode.Conflict, "{\"error\":\"El horario seleccionado ya no está disponible.\"}");
        var ex = await Assert.ThrowsAsync<ApiException>(() => api.Post<CitaDto>("api/citas", new { }));
        Assert.Equal(HttpStatusCode.Conflict, ex.Status);
        Assert.Equal("El horario seleccionado ya no está disponible.", ex.Message);
    }

    [Fact]
    public void ErrorSinCuerpoUsaUnMensajeGenerico() =>
        Assert.Equal("Su sesión expiró. Inicie sesión nuevamente.", ApiClient.ExtraerError(HttpStatusCode.Unauthorized, ""));

    [Fact]
    public async Task SinSesionNoEnviaEncabezadoDeAutorizacion()
    {
        var (api, falso) = Crear(HttpStatusCode.OK, "[]", token: null);
        await api.Get<List<ServicioDto>>("api/servicios");
        Assert.Null(falso.Ultima!.Headers.Authorization);
    }
}
