using System.Net;
using System.Net.Http.Json;
using System.Net.Mail;

namespace PeluqueriaRecordatorios;

/// <summary>Canal de envío de una notificación (patrón Strategy: agregar un canal no modifica el despachador).</summary>
public interface IMensajero
{
    string Canal { get; }
    Task EnviarAsync(NotificacionDto n, CancellationToken ct);
}

/// <summary>Envía por SMTP. Sin servidor configurado solo registra el mensaje (modo demostración).</summary>
public class MensajeroCorreo : IMensajero
{
    private readonly IConfiguration _cfg;
    private readonly ILogger<MensajeroCorreo> _log;
    public MensajeroCorreo(IConfiguration cfg, ILogger<MensajeroCorreo> log) { _cfg = cfg; _log = log; }
    public string Canal => "CORREO";

    public async Task EnviarAsync(NotificacionDto n, CancellationToken ct)
    {
        var host = _cfg["Smtp:Host"];
        if (string.IsNullOrWhiteSpace(host)) { _log.LogInformation("[CORREO simulado] Notificación {Id} tipo {Tipo}", n.Id, n.Tipo); return; }
        using var cliente = new SmtpClient(host, _cfg.GetValue("Smtp:Puerto", 587))
        {
            EnableSsl = _cfg.GetValue("Smtp:UsarSsl", true),
            Credentials = new NetworkCredential(_cfg["Smtp:Usuario"], _cfg["Smtp:Clave"])
        };
        using var mail = new MailMessage(_cfg["Smtp:Remitente"]!, n.CorreoDestino, "Bella Estilo — tu cita", n.Mensaje);
        await cliente.SendMailAsync(mail, ct);
    }
}

/// <summary>Envía por WhatsApp Business API. Sin configuración solo registra el mensaje (modo demostración).</summary>
public class MensajeroWhatsApp : IMensajero
{
    private static readonly HttpClient Http = new();
    private readonly IConfiguration _cfg;
    private readonly ILogger<MensajeroWhatsApp> _log;
    public MensajeroWhatsApp(IConfiguration cfg, ILogger<MensajeroWhatsApp> log) { _cfg = cfg; _log = log; }
    public string Canal => "WHATSAPP";

    public async Task EnviarAsync(NotificacionDto n, CancellationToken ct)
    {
        if (!_cfg.GetValue("WhatsApp:Habilitado", false)) { _log.LogInformation("[WHATSAPP simulado] Notificación {Id} tipo {Tipo}", n.Id, n.Tipo); return; }
        // Ajustar el cuerpo según el proveedor de WhatsApp Business API contratado.
        using var req = new HttpRequestMessage(HttpMethod.Post, _cfg["WhatsApp:Url"])
        {
            Content = JsonContent.Create(new { to = n.TelefonoDestino, type = "text", text = new { body = n.Mensaje } })
        };
        req.Headers.Authorization = new("Bearer", _cfg["WhatsApp:Token"]);
        (await Http.SendAsync(req, ct)).EnsureSuccessStatusCode();
    }
}
