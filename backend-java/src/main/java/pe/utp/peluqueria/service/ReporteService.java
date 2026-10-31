package pe.utp.peluqueria.service;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.dao.CitaDao;
import pe.utp.peluqueria.dto.Dtos.*;
import pe.utp.peluqueria.exception.ApiException;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.EstilistaRepository;

/** Reportes de gestión R1 (citas), R2 (servicios más solicitados) y R3 (ocupación). Usa {@link Multiset} de Guava para contar. */
@Service
@Transactional(readOnly = true)
public class ReporteService {
    private final CitaDao citas;
    private final EstilistaRepository estilistas;

    public ReporteService(CitaDao citas, EstilistaRepository estilistas) { this.citas = citas; this.estilistas = estilistas; }

    private void validar(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) throw ApiException.invalido("La fecha final debe ser posterior a la inicial.");
        if (ChronoUnit.DAYS.between(desde, hasta) > 366) throw ApiException.invalido("El rango máximo es de un año.");
    }

    /** R1: citas por período, estado y semana. */
    public ReporteCitasDto citas(LocalDate desde, LocalDate hasta) {
        validar(desde, hasta);
        List<Cita> lista = citas.entre(desde, hasta);
        Multiset<EstadoCita> porEstado = HashMultiset.create();
        Multiset<Long> porSemana = HashMultiset.create();
        for (Cita c : lista) {
            porEstado.add(c.getEstado());
            porSemana.add(ChronoUnit.WEEKS.between(desde, c.getFecha()) + 1);
        }
        List<ConteoDto> estados = Arrays.stream(EstadoCita.values()).map(e -> new ConteoDto(e.name(), porEstado.count(e))).toList();
        List<ConteoDto> semanas = porSemana.elementSet().stream().sorted()
                .map(s -> new ConteoDto("Semana " + s, porSemana.count(s))).toList();
        return new ReporteCitasDto(lista.size(), porEstado.count(EstadoCita.ATENDIDA), porEstado.count(EstadoCita.CANCELADA),
                porEstado.count(EstadoCita.AUSENTE), estados, semanas);
    }

    /** R2: servicios más solicitados (excluye citas canceladas), de mayor a menor. */
    public List<ConteoDto> servicios(LocalDate desde, LocalDate hasta) {
        validar(desde, hasta);
        Multiset<String> conteo = HashMultiset.create();
        for (Cita c : citas.entre(desde, hasta)) {
            if (c.getEstado() == EstadoCita.CANCELADA) continue;
            c.getDetalles().forEach(d -> conteo.add(d.getServicio().getNombre()));
        }
        return Multisets.copyHighestCountFirst(conteo).entrySet().stream()
                .map(e -> new ConteoDto(e.getElement(), e.getCount())).toList();
    }

    /** R3: minutos atendidos / minutos disponibles por estilista. */
    public List<OcupacionDto> ocupacion(LocalDate desde, LocalDate hasta) {
        validar(desde, hasta);
        List<Cita> atendidas = citas.entrePorEstado(EstadoCita.ATENDIDA, desde, hasta);
        List<OcupacionDto> salida = new ArrayList<>();
        for (Estilista e : estilistas.findAll()) {
            long atendido = atendidas.stream().filter(c -> c.getEstilista().getId().equals(e.getId()))
                    .mapToLong(c -> Duration.between(c.getHoraInicio(), c.getHoraFin()).toMinutes()).sum();
            long disponible = 0;
            for (LocalDate d = desde; !d.isAfter(hasta); d = d.plusDays(1)) {
                DayOfWeek dia = d.getDayOfWeek();
                disponible += e.getHorarios().stream().filter(h -> h.getDiaSemana() == dia)
                        .mapToLong(h -> Duration.between(h.getHoraInicio(), h.getHoraFin()).toMinutes()).sum();
            }
            double pct = disponible == 0 ? 0 : Math.round(atendido * 1000.0 / disponible) / 10.0;
            salida.add(new OcupacionDto(e.getNombreCompleto(), atendido, disponible, pct));
        }
        return salida;
    }
}
