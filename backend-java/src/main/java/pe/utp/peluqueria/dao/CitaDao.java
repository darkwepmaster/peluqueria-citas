package pe.utp.peluqueria.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import pe.utp.peluqueria.model.Cita;
import pe.utp.peluqueria.model.EstadoCita;

/**
 * Objeto de acceso a datos (patrón DAO) de las citas.
 * <p>Los servicios dependen de esta interfaz y no de JPA (principio de inversión de dependencias):
 * se puede sustituir por un doble de pruebas o por otra tecnología de persistencia.
 */
public interface CitaDao {
    Optional<Cita> buscarPorId(Long id);

    Cita guardar(Cita cita);

    /** Citas no canceladas de un estilista en una fecha (ocupan agenda). */
    List<Cita> activasDelDia(Long estilistaId, LocalDate fecha);

    /** Como {@link #activasDelDia} pero con bloqueo de escritura, para evitar dobles reservas concurrentes. */
    List<Cita> bloquearActivasDelDia(Long estilistaId, LocalDate fecha);

    List<Cita> deCliente(Long clienteId);

    List<Cita> deFecha(LocalDate fecha);

    List<Cita> deEstilista(Long estilistaId, LocalDate fecha);

    List<Cita> entre(LocalDate desde, LocalDate hasta);

    List<Cita> entrePorEstado(EstadoCita estado, LocalDate desde, LocalDate hasta);

    List<Cita> todas();
}
