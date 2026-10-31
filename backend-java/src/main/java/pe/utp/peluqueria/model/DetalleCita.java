package pe.utp.peluqueria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Servicio incluido en una cita, con el precio y la duración vigentes al reservar. */
@Entity
public class DetalleCita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cita")
    private Cita cita;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servicio")
    private Servicio servicio;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precioAplicado;

    @Column(nullable = false)
    private int duracionMin;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }
    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }
    public BigDecimal getPrecioAplicado() { return precioAplicado; }
    public void setPrecioAplicado(BigDecimal precioAplicado) { this.precioAplicado = precioAplicado; }
    public int getDuracionMin() { return duracionMin; }
    public void setDuracionMin(int duracionMin) { this.duracionMin = duracionMin; }
}
