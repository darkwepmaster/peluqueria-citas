package pe.utp.peluqueria.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Tarea programada: cada hora asegura que toda cita confirmada próxima tenga sus recordatorios. */
@Component
public class RecordatorioScheduler {
    private static final Logger log = LoggerFactory.getLogger(RecordatorioScheduler.class);
    private final NotificacionService notificaciones;

    public RecordatorioScheduler(NotificacionService notificaciones) { this.notificaciones = notificaciones; }

    @Scheduled(cron = "0 0 * * * *")
    public void ejecutar() {
        int creados = notificaciones.generarRecordatoriosFaltantes();
        if (creados > 0) log.info("Recordatorios generados para {} citas", creados);
    }
}
