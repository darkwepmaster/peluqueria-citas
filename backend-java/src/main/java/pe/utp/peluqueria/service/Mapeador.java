package pe.utp.peluqueria.service;

import java.util.Comparator;
import java.util.List;
import pe.utp.peluqueria.dto.Dtos.*;
import pe.utp.peluqueria.model.*;

/** Convierte entidades en DTOs. Debe invocarse dentro de una transacción (relaciones perezosas). */
final class Mapeador {
    private Mapeador() { }

    static CitaDto cita(Cita c) {
        List<String> servicios = c.getDetalles().stream().map(d -> d.getServicio().getNombre()).toList();
        return new CitaDto(c.getId(), c.getCliente().getId(), c.getCliente().getNombreCompleto(), c.getEstilista().getId(),
                c.getEstilista().getNombreCompleto(), c.getFecha(), c.getHoraInicio(), c.getHoraFin(), c.getEstado().name(),
                c.getTotal(), servicios, c.getMotivoCancelacion());
    }

    static ServicioDto servicio(Servicio s) {
        return new ServicioDto(s.getId(), s.getNombre(), s.getDescripcion(), s.getCategoria().getNombre(), s.getPrecio(), s.getDuracionMin(), s.isActivo());
    }

    static EstilistaDto estilista(Estilista e) {
        List<HorarioDto> hs = e.getHorarios().stream()
                .sorted(Comparator.comparing(HorarioEstilista::getDiaSemana).thenComparing(HorarioEstilista::getHoraInicio))
                .map(h -> new HorarioDto(h.getDiaSemana().getValue(), h.getHoraInicio(), h.getHoraFin())).toList();
        return new EstilistaDto(e.getId(), e.getNombreCompleto(), e.getEspecialidad(), e.isActivo(), hs);
    }

    static NotificacionDto notificacion(Notificacion n) {
        Cliente c = n.getCita().getCliente();
        return new NotificacionDto(n.getId(), n.getCita().getId(), n.getTipo().name(), n.getCanal().name(), n.getMensaje(),
                n.getFechaProgramada(), n.getFechaEnvio(), n.getEstadoEnvio().name(), c.getEmail(), c.getTelefono());
    }
}
