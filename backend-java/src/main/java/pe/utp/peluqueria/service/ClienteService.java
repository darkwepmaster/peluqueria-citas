package pe.utp.peluqueria.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.dao.CitaDao;
import pe.utp.peluqueria.dto.Dtos.ClienteDto;
import pe.utp.peluqueria.model.Cita;
import pe.utp.peluqueria.model.EstadoCita;
import pe.utp.peluqueria.repository.ClienteRepository;

/** Consulta de clientes con su historial resumido. */
@Service
@Transactional(readOnly = true)
public class ClienteService {
    private final ClienteRepository clientes;
    private final CitaDao citas;

    public ClienteService(ClienteRepository clientes, CitaDao citas) { this.clientes = clientes; this.citas = citas; }

    public List<ClienteDto> listar(String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase();
        return clientes.findAll().stream()
                .filter(c -> q.isEmpty() || c.getNombreCompleto().toLowerCase().contains(q) || c.getEmail().contains(q))
                .map(c -> {
                    List<Cita> historial = citas.deCliente(c.getId());
                    var ultima = historial.stream().filter(x -> x.getEstado() == EstadoCita.ATENDIDA)
                            .map(Cita::getFecha).max(Comparator.naturalOrder()).orElse(null);
                    return new ClienteDto(c.getId(), c.getNombreCompleto(), c.getEmail(), c.getTelefono(), historial.size(), ultima);
                }).toList();
    }
}
