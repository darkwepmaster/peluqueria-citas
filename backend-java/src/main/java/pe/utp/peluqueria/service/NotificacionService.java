package pe.utp.peluqueria.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.dao.CitaDao;
import pe.utp.peluqueria.dto.Dtos.NotificacionDto;
import pe.utp.peluqueria.exception.ApiException;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.NotificacionRepository;

/** Programa los avisos de cada cita y controla su envío (reintentos hasta 3 veces). */
@Service
@Transactional
public class NotificacionService {
    static final int MAX_INTENTOS = 3;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificacionRepository repo;
    private final CitaDao citas;
    private final ZoneId zona;

    public NotificacionService(NotificacionRepository repo, CitaDao citas, @Value("${app.zona}") String zona) {
        this.repo = repo; this.citas = citas; this.zona = ZoneId.of(zona);
    }

    private LocalDateTime ahora() { return LocalDateTime.now(zona); }

    /** Programa un aviso inmediato por cada canal que el cliente aceptó. */
    public void programar(Cita c, TipoNotificacion tipo) { crear(c, tipo, ahora()); }

    /** Programa recordatorios 24 h y 2 h antes de la cita (solo si aún están en el futuro). */
    public void programarRecordatorios(Cita c) {
        descartarRecordatorios(c);
        for (int horas : new int[] {24, 2}) {
            LocalDateTime cuando = c.inicio().minusHours(horas);
            if (cuando.isAfter(ahora())) crear(c, TipoNotificacion.RECORDATORIO, cuando);
        }
    }

    public void descartarRecordatorios(Cita c) {
        repo.deleteByCitaIdAndTipoAndEstadoEnvio(c.getId(), TipoNotificacion.RECORDATORIO, EstadoEnvio.PENDIENTE);
    }

    private void crear(Cita c, TipoNotificacion tipo, LocalDateTime cuando) {
        Cliente cl = c.getCliente();
        List<Canal> canales = new ArrayList<>();
        if (cl.isAceptaWhatsapp()) canales.add(Canal.WHATSAPP);
        if (cl.isAceptaCorreo()) canales.add(Canal.CORREO);
        for (Canal canal : canales) {
            Notificacion n = new Notificacion();
            n.setCita(c); n.setTipo(tipo); n.setCanal(canal); n.setMensaje(mensaje(c, tipo));
            n.setFechaProgramada(cuando); n.setEstadoEnvio(EstadoEnvio.PENDIENTE);
            repo.save(n);
        }
    }

    static String mensaje(Cita c, TipoNotificacion tipo) {
        String cuando = c.getFecha().format(FECHA) + " a las " + c.getHoraInicio().format(HORA);
        String nombre = c.getCliente().getNombres();
        String con = c.getEstilista().getNombreCompleto();
        return switch (tipo) {
            case REGISTRO -> "Hola " + nombre + ", registramos tu cita para el " + cuando + " con " + con + ". Queda pendiente de confirmación.";
            case CONFIRMACION -> "Hola " + nombre + ", tu cita del " + cuando + " con " + con + " fue confirmada.";
            case RECORDATORIO -> "Hola " + nombre + ", te recordamos tu cita el " + cuando + " con " + con + ".";
            case CANCELACION -> "Hola " + nombre + ", tu cita del " + cuando + " fue cancelada.";
            case REPROGRAMACION -> "Hola " + nombre + ", tu cita fue reprogramada para el " + cuando + " con " + con + ".";
        };
    }

    @Transactional(readOnly = true)
    public List<NotificacionDto> pendientes() {
        return repo.findByEstadoEnvioAndFechaProgramadaLessThanEqualOrderByFechaProgramada(EstadoEnvio.PENDIENTE, ahora())
                .stream().map(Mapeador::notificacion).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacionDto> delCliente(Long clienteId) {
        return repo.findByCitaClienteIdOrderByFechaProgramadaDesc(clienteId).stream().map(Mapeador::notificacion).toList();
    }

    public void marcarEnviada(Long id) {
        Notificacion n = repo.findById(id).orElseThrow(() -> ApiException.noEncontrado("Notificación"));
        n.setEstadoEnvio(EstadoEnvio.ENVIADA);
        n.setFechaEnvio(ahora());
    }

    /** Reintenta hasta {@value #MAX_INTENTOS} veces; luego queda FALLIDA. */
    public void marcarFallida(Long id) {
        Notificacion n = repo.findById(id).orElseThrow(() -> ApiException.noEncontrado("Notificación"));
        n.setIntentos(n.getIntentos() + 1);
        if (n.getIntentos() >= MAX_INTENTOS) n.setEstadoEnvio(EstadoEnvio.FALLIDA);
    }

    /** Red de seguridad: crea los recordatorios que falten para citas confirmadas próximas. */
    public int generarRecordatoriosFaltantes() {
        LocalDate hoy = ahora().toLocalDate();
        int creados = 0;
        for (Cita c : citas.entrePorEstado(EstadoCita.CONFIRMADA, hoy, hoy.plusDays(2))) {
            if (!repo.existsByCitaIdAndTipo(c.getId(), TipoNotificacion.RECORDATORIO)) { programarRecordatorios(c); creados++; }
        }
        return creados;
    }
}
