namespace PeluqueriaRecordatorios;

/// <summary>Servicio en segundo plano: consulta periódicamente las notificaciones vencidas y las despacha por su canal.</summary>
public class Despachador : BackgroundService
{
    private readonly ApiCliente _api;
    private readonly Dictionary<string, IMensajero> _mensajeros;
    private readonly ILogger<Despachador> _log;
    private readonly TimeSpan _intervalo;

    public Despachador(ApiCliente api, IEnumerable<IMensajero> mensajeros, IConfiguration cfg, ILogger<Despachador> log)
    {
        _api = api; _log = log;
        _mensajeros = mensajeros.ToDictionary(m => m.Canal);
        _intervalo = TimeSpan.FromSeconds(cfg.GetValue("Worker:IntervaloSegundos", 60));
    }

    protected override async Task ExecuteAsync(CancellationToken ct)
    {
        _log.LogInformation("Despachador de recordatorios iniciado (cada {Seg} s)", _intervalo.TotalSeconds);
        while (!ct.IsCancellationRequested)
        {
            try { await ProcesarAsync(ct); }
            catch (Exception ex) when (ex is not OperationCanceledException) { _log.LogWarning("No se pudo consultar la API: {Mensaje}", ex.Message); }
            await Task.Delay(_intervalo, ct);
        }
    }

    private async Task ProcesarAsync(CancellationToken ct)
    {
        var pendientes = await _api.PendientesAsync(ct);
        foreach (var n in pendientes)
        {
            bool ok;
            try
            {
                if (!_mensajeros.TryGetValue(n.Canal, out var m)) throw new InvalidOperationException($"Canal no soportado: {n.Canal}");
                await m.EnviarAsync(n, ct);
                ok = true;
            }
            catch (Exception ex) when (ex is not OperationCanceledException)
            {
                _log.LogWarning("Falló el envío de la notificación {Id}: {Mensaje}", n.Id, ex.Message);
                ok = false;
            }
            await _api.MarcarAsync(n.Id, ok, ct);
        }
        if (pendientes.Count > 0) _log.LogInformation("Notificaciones procesadas: {Cantidad}", pendientes.Count);
    }
}
