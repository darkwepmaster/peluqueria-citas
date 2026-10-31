package pe.utp.peluqueria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Reserva de un cliente con un estilista. Ciclo de vida: PENDIENTE, CONFIRMADA, ATENDIDA, AUSENTE o CANCELADA. */
@Entity
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estilista")
    private Estilista estilista;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoCita estado;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal total;

    @Column(length = 200)
    private String motivoCancelacion;

    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "cita", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCita> detalles = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Estilista getEstilista() { return estilista; }
    public void setEstilista(Estilista estilista) { this.estilista = estilista; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public List<DetalleCita> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCita> detalles) { this.detalles = detalles; }

    @PrePersist
    void alCrear() { if (fechaCreacion == null) fechaCreacion = LocalDateTime.now(); }

    public void agregarDetalle(DetalleCita d) { d.setCita(this); detalles.add(d); }

    /** Duración total: suma de la duración de sus servicios. */
    public int getDuracionMin() { return detalles.stream().mapToInt(DetalleCita::getDuracionMin).sum(); }

    /** Una cita activa ocupa agenda (pendiente o confirmada). */
    public boolean estaActiva() { return estado == EstadoCita.PENDIENTE || estado == EstadoCita.CONFIRMADA; }

    public LocalDateTime inicio() { return LocalDateTime.of(fecha, horaInicio); }
}
