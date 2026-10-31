package pe.utp.peluqueria.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.TreeSet;
import pe.utp.peluqueria.model.Cita;
import pe.utp.peluqueria.model.HorarioEstilista;

/** Lógica pura (sin Spring) para calcular los horarios libres de un estilista. Usa {@link Range} de Google Guava. */
public final class Disponibilidad {
    public static final int PASO_MIN = 30;

    private Disponibilidad() { }

    /** Dos intervalos [inicio, fin) se cruzan si comparten algún minuto; citas contiguas no se cruzan. */
    public static boolean seCruza(LocalTime ini1, LocalTime fin1, LocalTime ini2, LocalTime fin2) {
        Range<LocalTime> a = Range.closedOpen(ini1, fin1);
        Range<LocalTime> b = Range.closedOpen(ini2, fin2);
        return a.isConnected(b) && !a.intersection(b).isEmpty();
    }

    /** Horarios de inicio disponibles (cada 30 min) en que cabe un servicio de {@code duracionMin} minutos. */
    public static List<LocalTime> slotsLibres(List<HorarioEstilista> jornadas, List<Cita> ocupadas, int duracionMin,
                                              LocalDate fecha, LocalDateTime ahora) {
        TreeSet<LocalTime> libres = new TreeSet<>();
        DayOfWeek dia = fecha.getDayOfWeek();
        for (HorarioEstilista h : jornadas) {
            if (h.getDiaSemana() != dia) continue;
            for (LocalTime t = h.getHoraInicio(); ; t = t.plusMinutes(PASO_MIN)) {
                final LocalTime ini = t;
                final LocalTime fin = ini.plusMinutes(duracionMin);
                if (fin.isBefore(ini) || fin.isAfter(h.getHoraFin())) break;
                if (LocalDateTime.of(fecha, ini).isBefore(ahora)) continue;
                if (ocupadas.stream().noneMatch(c -> seCruza(ini, fin, c.getHoraInicio(), c.getHoraFin()))) libres.add(ini);
            }
        }
        return ImmutableList.copyOf(libres);
    }
}
