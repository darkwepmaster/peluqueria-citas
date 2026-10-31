package pe.utp.peluqueria.export;

import java.util.List;
import pe.utp.peluqueria.dto.Dtos.CitaDto;

/** Estrategia de exportación del reporte de citas. Agregar un formato nuevo no modifica el código existente (abierto/cerrado). */
public interface Exportador {
    /** Extensión del archivo, por ejemplo "xlsx". */
    String formato();

    String contentType();

    byte[] exportar(List<CitaDto> citas);
}
