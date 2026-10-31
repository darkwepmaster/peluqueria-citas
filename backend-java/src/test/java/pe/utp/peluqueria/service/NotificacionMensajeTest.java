package pe.utp.peluqueria.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import pe.utp.peluqueria.model.*;

class NotificacionMensajeTest {
    @Test
    void elRecordatorioIncluyeFechaHoraYEstilista() {
        Cliente cl = new Cliente();
        cl.setNombres("Valeria");
        Estilista es = new Estilista();
        es.setNombres("Milagros");
        es.setApellidos("Ramos");
        Cita c = new Cita();
        c.setCliente(cl);
        c.setEstilista(es);
        c.setFecha(LocalDate.of(2026, 6, 4));
        c.setHoraInicio(LocalTime.of(15, 0));
        String m = NotificacionService.mensaje(c, TipoNotificacion.RECORDATORIO);
        assertTrue(m.contains("04/06/2026") && m.contains("15:00") && m.contains("Milagros Ramos"));
    }
}
