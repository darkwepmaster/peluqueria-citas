using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;

namespace PeluqueriaWeb.Services;

public class ApiException : Exception
{
    public HttpStatusCode Status { get; }
    public ApiException(HttpStatusCode status, string mensaje) : base(mensaje) => Status = status;
}

/// <summary>Cliente HTTP hacia la API REST en Java. Adjunta el token JWT guardado en la cookie de sesión.</summary>
public class ApiClient
{
    private static readonly JsonSerializerOptions Json = new(JsonSerializerDefaults.Web);
    private readonly HttpClient _http;
    private readonly IHttpContextAccessor _ctx;

    public ApiClient(HttpClient http, IHttpContextAccessor ctx) { _http = http; _ctx = ctx; }

    public Task<T> Get<T>(string url) => Enviar<T>(HttpMethod.Get, url, null);
    public Task<T> Post<T>(string url, object? cuerpo = null) => Enviar<T>(HttpMethod.Post, url, cuerpo);
    public Task Post(string url, object? cuerpo = null) => Enviar<object>(HttpMethod.Post, url, cuerpo);
    public Task<T> Put<T>(string url, object? cuerpo = null) => Enviar<T>(HttpMethod.Put, url, cuerpo);

    /// <summary>Descarga un archivo (por ejemplo, el reporte en Excel generado por la API con Apache POI).</summary>
    public async Task<(byte[] Contenido, string TipoMime)> Descargar(string url)
    {
        using var res = await Solicitar(HttpMethod.Get, url, null);
        var bytes = await res.Content.ReadAsByteArrayAsync();
        if (!res.IsSuccessStatusCode) throw new ApiException(res.StatusCode, ExtraerError(res.StatusCode, System.Text.Encoding.UTF8.GetString(bytes)));
        return (bytes, res.Content.Headers.ContentType?.MediaType ?? "application/octet-stream");
    }

    private async Task<T> Enviar<T>(HttpMethod metodo, string url, object? cuerpo)
    {
        using var res = await Solicitar(metodo, url, cuerpo);
        var texto = await res.Content.ReadAsStringAsync();
        if (!res.IsSuccessStatusCode) throw new ApiException(res.StatusCode, ExtraerError(res.StatusCode, texto));
        return string.IsNullOrWhiteSpace(texto) ? default! : JsonSerializer.Deserialize<T>(texto, Json)!;
    }

    private async Task<HttpResponseMessage> Solicitar(HttpMethod metodo, string url, object? cuerpo)
    {
        using var req = new HttpRequestMessage(metodo, url);
        var token = _ctx.HttpContext?.User.FindFirst("token")?.Value;
        if (!string.IsNullOrEmpty(token)) req.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
        if (cuerpo != null) req.Content = JsonContent.Create(cuerpo, null, Json);
        try { return await _http.SendAsync(req); }
        catch (HttpRequestException) { throw new ApiException(HttpStatusCode.ServiceUnavailable, "No se pudo conectar con el servidor. Intente más tarde."); }
    }

    public static string ExtraerError(HttpStatusCode codigo, string texto)
    {
        try
        {
            using var doc = JsonDocument.Parse(texto);
            if (doc.RootElement.TryGetProperty("error", out var e)) return e.GetString() ?? "Error";
        }
        catch (JsonException) { }
        return codigo switch
        {
            HttpStatusCode.Unauthorized => "Su sesión expiró. Inicie sesión nuevamente.",
            HttpStatusCode.Forbidden => "No tiene permiso para realizar esta acción.",
            _ => "Ocurrió un error al procesar la solicitud."
        };
    }
}
