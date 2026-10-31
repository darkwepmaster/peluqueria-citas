package pe.utp.peluqueria.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.utp.peluqueria.model.Cita;
import pe.utp.peluqueria.model.EstadoCita;

/** Implementación de {@link CitaDao} con JPA (EntityManager) y consultas JPQL parametrizadas. */
@Repository
public class CitaDaoJpa implements CitaDao {
    private static final String ORDEN = " order by c.fecha, c.horaInicio";

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Cita> buscarPorId(Long id) { return Optional.ofNullable(em.find(Cita.class, id)); }

    @Override
    public Cita guardar(Cita cita) {
        if (cita.getId() == null) { em.persist(cita); return cita; }
        return em.merge(cita);
    }

    @Override
    public List<Cita> activasDelDia(Long estilistaId, LocalDate fecha) { return delDia(estilistaId, fecha).getResultList(); }

    @Override
    public List<Cita> bloquearActivasDelDia(Long estilistaId, LocalDate fecha) {
        return delDia(estilistaId, fecha).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
    }

    private TypedQuery<Cita> delDia(Long estilistaId, LocalDate fecha) {
        return em.createQuery("select c from Cita c where c.estilista.id = :e and c.fecha = :f and c.estado <> :x" + ORDEN, Cita.class)
                .setParameter("e", estilistaId).setParameter("f", fecha).setParameter("x", EstadoCita.CANCELADA);
    }

    @Override
    public List<Cita> deCliente(Long clienteId) {
        return em.createQuery("select c from Cita c where c.cliente.id = :id order by c.fecha desc, c.horaInicio desc", Cita.class)
                .setParameter("id", clienteId).getResultList();
    }

    @Override
    public List<Cita> deFecha(LocalDate fecha) {
        return em.createQuery("select c from Cita c where c.fecha = :f order by c.horaInicio", Cita.class)
                .setParameter("f", fecha).getResultList();
    }

    @Override
    public List<Cita> deEstilista(Long estilistaId, LocalDate fecha) {
        return em.createQuery("select c from Cita c where c.estilista.id = :e and c.fecha = :f order by c.horaInicio", Cita.class)
                .setParameter("e", estilistaId).setParameter("f", fecha).getResultList();
    }

    @Override
    public List<Cita> entre(LocalDate desde, LocalDate hasta) {
        return em.createQuery("select c from Cita c where c.fecha between :d and :h" + ORDEN, Cita.class)
                .setParameter("d", desde).setParameter("h", hasta).getResultList();
    }

    @Override
    public List<Cita> entrePorEstado(EstadoCita estado, LocalDate desde, LocalDate hasta) {
        return em.createQuery("select c from Cita c where c.estado = :s and c.fecha between :d and :h" + ORDEN, Cita.class)
                .setParameter("s", estado).setParameter("d", desde).setParameter("h", hasta).getResultList();
    }

    @Override
    public List<Cita> todas() {
        return em.createQuery("select c from Cita c order by c.fecha desc, c.horaInicio desc", Cita.class).getResultList();
    }
}
