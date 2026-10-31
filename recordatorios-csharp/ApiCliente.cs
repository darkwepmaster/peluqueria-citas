using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;

namespace PeluqueriaRecordatorios;

/// <summary>Cliente de la API Java: inicia sesión con una cuenta de administración y consume las notificaciones pendientes.</summary>
public class ApiCliente
{
    private static readonly JsonSerializerOptions Json = new(JsonSerializerDefaults.Web);
    private readonly HttpClient _http;
    private readonly IConfiguration _cfg;
    private DateTime _tokenExpira = DateTime.MinValue;

    public ApiCliente(HttpClient http, IConfiguration cfg) { _http = http; _cfg = cfg; }

    private async Task AutenticarAsync(CancellationToken ct)
    {
        if (DateTime.UtcNow < _tokenExpira) return;
        var res = await _http.PostAsJsonAsync("api/auth/login", new { email = _cfg["Api:Usuario"], password = _cfg["Api:Clave"] }, Json, ct);
        res.EnsureSuccessStatusCode();
        var login = (await res.Content.ReadFromJsonAsync<LoginResponse>(Json, ct))!;
        _http.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", login.Token);
        _tokenExpira = DateTime.UtcNow.AddHours(7);
    }

    public async Task<List<NotificacionDto>> PendientesAsync(CancellationToken ct)
    {
        await AutenticarAsync(ct);
        return await _http.GetFromJsonAsync<List<NotificacionDto>>("api/notificaciones/pendientes", Json, ct) ?? new();
    }

    public async Task MarcarAsync(long id, bool enviada, CancellationToken ct)
    {
        await AutenticarAsync(ct);
        (await _http.PostAsync($"api/notificaciones/{id}/{(enviada ? "enviada" : "fallida")}", null, ct)).EnsureSuccessStatusCode();
    }
}
