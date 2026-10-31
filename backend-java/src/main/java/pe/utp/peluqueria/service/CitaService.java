package pe.utp.peluqueria.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.dao.CitaDao;
import pe.utp.peluqueria.dto.Dtos.*;
import pe.utp.peluqueria.exception.ApiException;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.*;
import pe.utp.peluqueria.security.Sesion;
import pe.utp.peluqueria.util.Sanitizador;

/**
 * Reglas de negocio de las citas: reserva sin cruces, confirmación, cancelación, reprogramación y cierre.
 * Depende de abstracciones ({@link CitaDao}), no de la tecnología de persistencia.
 */
@Service
@Transactional
public class CitaService {
    private static final Logger AUDITORIA = LoggerFactory.getLogger("AUDITORIA");

    private final CitaDao citas;
    private final ClienteRepository clientes;
    private final EstilistaRepository estilistas;
    private final ServicioRepository servicios;
    private final NotificacionService notificaciones;
    private final ZoneId zona;

    public CitaService(CitaDao citas, ClienteRepository clientes, EstilistaRepository estilistas, ServicioRepository servicios,
                       NotificacionService notificaciones, @Value("${app.zona}") String zona) {
        this.citas = citas; this.clientes = clientes; this.estilistas = estilistas;
        this.servicios = servicios; this.notificaciones = notificaciones; this.zona = ZoneId.of(zona);
    }

    private LocalDateTime ahora() { return LocalDateTime.now(zona); }

    // ------------------------------------------------------------------ consultas
    @Transactional(readOnly = true)
    public List<LocalTime> horariosLibres(Long estilistaId, LocalDate fecha, List<Long> servicioIds) {
        Estilista e = estilista(estilistaId);
        int duracion = cargarServicios(servicioIds).stream().mapToInt(Servicio::getDuracionMin).sum();
        return Disponibilidad.slotsLibres(e.getHorarios(), citas.activasDelDia(estilistaId, fecha), duracion, fecha, ahora());
    }

    @Transactional(readOnly = true)
    public List<LocalTime> horariosParaReprogramar(Long citaId, LocalDate fecha, Sesion s) {
        Cita c = cita(citaId);
        verificarAcceso(c, s);
        List<Cita> ocupadas = citas.activasDelDia(c.getEstilista().getId(), fecha).stream().filter(o -> !o.getId().equals(c.getId())).toList();
        return Disponibilidad.slotsLibres(c.getEstilista().getHorarios(), ocupadas, c.getDuracionMin(), fecha, ahora());
    }

    @Transactional(readOnly = true)
    public List<CitaDto> delCliente(Long clienteId) { return citas.deCliente(clienteId).stream().map(Mapeador::cita).toList(); }

    @Transactional(readOnly = true)
    public List<CitaDto> listarAdmin(LocalDate fecha, String estado) {
        List<Cita> base = fecha != null ? citas.deFecha(fecha) : citas.todas();
        return base.stream().filter(c -> estado == null || estado.isBlank() || c.getEstado().name().equalsIgnoreCase(estado))
                .map(Mapeador::cita).toList();
    }

    @Transactional(readOnly = true)
    public List<CitaDto> entreFechas(LocalDate desde, LocalDate hasta) {
        return citas.entre(desde, hasta).stream().map(Mapeador::cita).toList();
    }

    @Transactional(readOnly = true)
    public List<CitaDto> agenda(Long estilistaId, LocalDate fecha) { return citas.deEstilista(estilistaId, fecha).stream().map(Mapeador::cita).toList(); }

    // ------------------------------------------------------------------ comandos
    public CitaDto crear(Long clienteId, ReservaRequest r) {
        Cliente cliente = clientes.findById(clienteId).orElseThrow(() -> ApiException.noEncontrado("Cliente"));
        Estilista e = estilista(r.estilistaId());
        if (!e.isActivo()) throw ApiException.invalido("El estilista seleccionado no está disponible.");
        List<Servicio> elegidos = cargarServicios(r.servicioIds());
        if (!e.getServicios().isEmpty() && !e.getServicios().containsAll(elegidos))
            throw ApiException.invalido("El estilista no realiza alguno de los servicios elegidos.");

        int duracion = elegidos.stream().mapToInt(Servicio::getDuracionMin).sum();
        if (LocalDateTime.of(r.fecha(), r.horaInicio()).isBefore(ahora()))
            throw ApiException.invalido("No se puede reservar en una fecha u hora pasada.");
        // El bloqueo de escritura evita que dos clientes tomen el mismo horario a la vez.
        List<Cita> ocupadas = citas.bloquearActivasDelDia(e.getId(), r.fecha());
        if (!Disponibilidad.slotsLibres(e.getHorarios(), ocupadas, duracion, r.fecha(), ahora()).contains(r.horaInicio()))
            throw ApiException.conflicto("El horario seleccionado ya no está disponible.");

        Cita c = new Cita();
        c.setCliente(cliente);
        c.setEstilista(e);
        c.setFecha(r.fecha());
        c.setHoraInicio(r.horaInicio());
        c.setHoraFin(r.horaInicio().plusMinutes(duracion));
        c.setEstado(EstadoCita.PENDIENTE);
        BigDecimal total = BigDecimal.ZERO;
        for (Servicio s : elegidos) {
            DetalleCita d = new DetalleCita();
            d.setServicio(s);
            d.setPrecioAplicado(s.getPrecio());
            d.setDuracionMin(s.getDuracionMin());
            c.agregarDetalle(d);
            total = total.add(s.getPrecio());
        }
        c.setTotal(total);
        citas.guardar(c);
        notificaciones.programar(c, TipoNotificacion.REGISTRO);
        AUDITORIA.info("CITA_CREADA cita={} cliente={} estilista={}", c.getId(), clienteId, e.getId());
        return Mapeador.cita(c);
    }

    public CitaDto confirmar(Long id) {
        Cita c = cita(id);
        if (c.getEstado() != EstadoCita.PENDIENTE) throw ApiException.conflicto("Solo se puede confirmar una cita pendiente.");
        c.setEstado(EstadoCita.CONFIRMADA);
        notificaciones.programar(c, TipoNotificacion.CONFIRMACION);
        notificaciones.programarRecordatorios(c);
        AUDITORIA.info("CITA_CONFIRMADA cita={}", id);
        return Mapeador.cita(c);
    }

    public void cancelar(Long id, Sesion s, String motivo) {
        Cita c = cita(id);
        verificarAcceso(c, s);
        if (!c.estaActiva()) throw ApiException.conflicto("La cita ya no se puede cancelar.");
        c.setEstado(EstadoCita.CANCELADA);
        c.setMotivoCancelacion(Sanitizador.limpiar(motivo));
        notificaciones.descartarRecordatorios(c);
        notificaciones.programar(c, TipoNotificacion.CANCELACION);
        AUDITORIA.info("CITA_CANCELADA cita={} por={}", id, s.id());
    }

    public CitaDto reprogramar(Long id, Sesion s, ReprogramarRequest r) {
        Cita c = cita(id);
        verificarAcceso(c, s);
        if (!c.estaActiva()) throw ApiException.conflicto("La cita ya no se puede reprogramar.");
        if (LocalDateTime.of(r.fecha(), r.horaInicio()).isBefore(ahora()))
            throw ApiException.invalido("No se puede reprogramar a una fecha u hora pasada.");
        List<Cita> ocupadas = citas.bloquearActivasDelDia(c.getEstilista().getId(), r.fecha()).stream()
                .filter(o -> !o.getId().equals(c.getId())).toList();
        int duracion = c.getDuracionMin();
        if (!Disponibilidad.slotsLibres(c.getEstilista().getHorarios(), ocupadas, duracion, r.fecha(), ahora()).contains(r.horaInicio()))
            throw ApiException.conflicto("El horario seleccionado no está disponible.");
        c.setFecha(r.fecha());
        c.setHoraInicio(r.horaInicio());
        c.setHoraFin(r.horaInicio().plusMinutes(duracion));
        c.setEstado(EstadoCita.PENDIENTE);
        notificaciones.descartarRecordatorios(c);
        notificaciones.programar(c, TipoNotificacion.REPROGRAMACION);
        AUDITORIA.info("CITA_REPROGRAMADA cita={} por={}", id, s.id());
        return Mapeador.cita(c);
    }

    /** Cierra la cita como atendida o ausente (solo el estilista asignado o el administrador). */
    public CitaDto cerrar(Long id, Sesion s, boolean asistio) {
        Cita c = cita(id);
        if (!s.esAdmin() && !c.getEstilista().getId().equals(s.id())) throw ApiException.prohibido();
        if (!c.estaActiva()) throw ApiException.conflicto("La cita ya fue cerrada o cancelada.");
        c.setEstado(asistio ? EstadoCita.ATENDIDA : EstadoCita.AUSENTE);
        notificaciones.descartarRecordatorios(c);
        AUDITORIA.info("CITA_CERRADA cita={} estado={}", id, c.getEstado());
        return Mapeador.cita(c);
    }

    // ------------------------------------------------------------------ auxiliares
    private Cita cita(Long id) { return citas.buscarPorId(id).orElseThrow(() -> ApiException.noEncontrado("Cita")); }

    private Estilista estilista(Long id) { return estilistas.findById(id).orElseThrow(() -> ApiException.noEncontrado("Estilista")); }

    private List<Servicio> cargarServicios(List<Long> ids) {
        List<Servicio> lista = servicios.findAllById(ids);
        if (lista.isEmpty() || lista.size() != ids.stream().distinct().count()) throw ApiException.invalido("Alguno de los servicios elegidos no existe.");
        if (lista.stream().anyMatch(s -> !s.isActivo())) throw ApiException.invalido("Alguno de los servicios no está disponible.");
        return lista;
    }

    /** Un cliente solo accede a sus citas; el estilista, a las suyas; el administrador, a todas. */
    private void verificarAcceso(Cita c, Sesion s) {
        boolean propia = switch (s.rol()) {
            case CLIENTE -> c.getCliente().getId().equals(s.id());
            case ESTILISTA -> c.getEstilista().getId().equals(s.id());
            case ADMINISTRADOR -> true;
        };
        if (!propia) throw ApiException.prohibido();
    }
}
