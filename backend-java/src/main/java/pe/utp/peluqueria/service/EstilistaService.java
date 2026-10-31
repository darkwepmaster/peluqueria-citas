package pe.utp.peluqueria.service;

import java.time.DayOfWeek;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.dto.Dtos.*;
import pe.utp.peluqueria.exception.ApiException;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.*;
import pe.utp.peluqueria.util.Sanitizador;

/** Alta de estilistas y mantenimiento de su horario semanal. */
@Service
@Transactional
public class EstilistaService {
    private final EstilistaRepository estilistas;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public EstilistaService(EstilistaRepository estilistas, UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.estilistas = estilistas; this.usuarios = usuarios; this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<EstilistaDto> activos() { return estilistas.findByActivoTrueOrderByNombres().stream().map(Mapeador::estilista).toList(); }

    @Transactional(readOnly = true)
    public List<EstilistaDto> todos() { return estilistas.findAll().stream().map(Mapeador::estilista).toList(); }

    public EstilistaDto crear(EstilistaRequest r) {
        String email = Sanitizador.correo(r.email());
        if (usuarios.existsByEmail(email)) throw ApiException.conflicto("Ya existe un usuario con ese correo.");
        Estilista e = new Estilista();
        e.setNombres(Sanitizador.limpiar(r.nombres()));
        e.setApellidos(Sanitizador.limpiar(r.apellidos()));
        e.setEmail(email);
        e.setTelefono(Sanitizador.limpiar(r.telefono()));
        e.setPasswordHash(encoder.encode(r.password()));
        e.setRol(Rol.ESTILISTA);
        e.setActivo(true);
        e.setEspecialidad(Sanitizador.limpiar(r.especialidad()));
        if (r.horarios() != null) e.reemplazarHorarios(convertir(r.horarios()));
        return Mapeador.estilista(estilistas.save(e));
    }

    public EstilistaDto actualizarHorarios(Long id, List<HorarioDto> horarios) {
        Estilista e = estilistas.findById(id).orElseThrow(() -> ApiException.noEncontrado("Estilista"));
        e.reemplazarHorarios(convertir(horarios));
        return Mapeador.estilista(e);
    }

    public void cambiarEstado(Long id, boolean activo) {
        estilistas.findById(id).orElseThrow(() -> ApiException.noEncontrado("Estilista")).setActivo(activo);
    }

    private List<HorarioEstilista> convertir(List<HorarioDto> dtos) {
        return dtos.stream().map(h -> {
            if (!h.horaFin().isAfter(h.horaInicio())) throw ApiException.invalido("La hora de fin debe ser posterior a la de inicio.");
            return HorarioEstilista.de(DayOfWeek.of(h.diaSemana()), h.horaInicio(), h.horaFin());
        }).toList();
    }
}
