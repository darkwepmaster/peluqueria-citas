package pe.utp.peluqueria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Estilista con su jornada semanal y los servicios que realiza (vacío = todos). */
@Entity
public class Estilista extends Usuario {
    @Column(nullable = false, length = 80)
    private String especialidad;

    @OneToMany(mappedBy = "estilista", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HorarioEstilista> horarios = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "estilista_servicio", joinColumns = @JoinColumn(name = "id_estilista"), inverseJoinColumns = @JoinColumn(name = "id_servicio"))
    private Set<Servicio> servicios = new HashSet<>();

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public List<HorarioEstilista> getHorarios() { return horarios; }
    public void setHorarios(List<HorarioEstilista> horarios) { this.horarios = horarios; }
    public Set<Servicio> getServicios() { return servicios; }
    public void setServicios(Set<Servicio> servicios) { this.servicios = servicios; }

    public void agregarHorario(HorarioEstilista h) { h.setEstilista(this); horarios.add(h); }

    public void reemplazarHorarios(List<HorarioEstilista> nuevos) {
        horarios.clear();
        nuevos.forEach(this::agregarHorario);
    }
}
