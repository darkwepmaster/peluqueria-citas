package pe.utp.peluqueria.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import pe.utp.peluqueria.model.Cita;
import pe.utp.peluqueria.model.HorarioEstilista;

/** Pruebas (TDD) de la regla central: calcular horarios libres sin cruces. */
class DisponibilidadTest {
    private static final LocalDate JUEVES = LocalDate.of(2026, 6, 4);
    private static final LocalDateTime ANTES = LocalDateTime.of(2026, 6, 1, 8, 0);

    private List<HorarioEstilista> jornada() {
        return List.of(HorarioEstilista.de(DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)));
    }

    private Cita cita(int hIni, int hFin) {
        Cita c = new Cita();
        c.setHoraInicio(LocalTime.of(hIni, 0));
        c.setHoraFin(LocalTime.of(hFin, 0));
        return c;
    }

    @Test
    void sinCitasOfreceTodosLosHorariosQueCaben() {
        assertEquals(List.of(LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(10, 30), LocalTime.of(11, 0)),
                Disponibilidad.slotsLibres(jornada(), List.of(), 60, JUEVES, ANTES));
    }

    @Test
    void excluyeHorariosQueSeCruzanConUnaCita() {
        List<LocalTime> libres = Disponibilidad.slotsLibres(jornada(), List.of(cita(10, 11)), 60, JUEVES, ANTES);
        assertFalse(libres.contains(LocalTime.of(9, 30)));
        assertFalse(libres.contains(LocalTime.of(10, 0)));
        assertFalse(libres.contains(LocalTime.of(10, 30)));
        assertTrue(libres.contains(LocalTime.of(9, 0)));
        assertTrue(libres.contains(LocalTime.of(11, 0)));
    }

    @Test
    void noOfreceHorasPasadas() {
        List<LocalTime> libres = Disponibilidad.slotsLibres(jornada(), List.of(), 30, JUEVES, LocalDateTime.of(2026, 6, 4, 10, 15));
        assertEquals(LocalTime.of(10, 30), libres.get(0));
    }

    @Test
    void diaSinJornadaNoTieneHorarios() {
        assertTrue(Disponibilidad.slotsLibres(jornada(), List.of(), 30, JUEVES.plusDays(1), ANTES).isEmpty());
    }

    @Test
    void citasContiguasNoSeCruzan() {
        assertFalse(Disponibilidad.seCruza(LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertTrue(Disponibilidad.seCruza(LocalTime.of(9, 30), LocalTime.of(10, 30), LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }
}
