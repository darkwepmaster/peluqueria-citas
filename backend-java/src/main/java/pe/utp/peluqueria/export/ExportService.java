package pe.utp.peluqueria.export;

import com.google.common.collect.Maps;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import pe.utp.peluqueria.exception.ApiException;
import pe.utp.peluqueria.service.CitaService;

/** Orquesta la exportación: elige el {@link Exportador} según el formato pedido (Guava {@link Maps#uniqueIndex}). */
@Service
public class ExportService {
    /** Archivo generado listo para descargar. */
    public record Archivo(String nombre, String contentType, byte[] contenido) { }

    private final Map<String, Exportador> exportadores;
    private final CitaService citas;

    public ExportService(List<Exportador> exportadores, CitaService citas) {
        this.exportadores = Maps.uniqueIndex(exportadores, Exportador::formato);
        this.citas = citas;
    }

    public Archivo generar(String formato, LocalDate desde, LocalDate hasta) {
        Exportador e = exportadores.get(formato == null ? "" : formato.toLowerCase());
        if (e == null) throw ApiException.invalido("Formato no soportado. Use: " + String.join(", ", exportadores.keySet()));
        if (hasta.isBefore(desde)) throw ApiException.invalido("La fecha final debe ser posterior a la inicial.");
        byte[] datos = e.exportar(citas.entreFechas(desde, hasta));
        return new Archivo("citas_" + desde + "_" + hasta + "." + e.formato(), e.contentType(), datos);
    }
}
