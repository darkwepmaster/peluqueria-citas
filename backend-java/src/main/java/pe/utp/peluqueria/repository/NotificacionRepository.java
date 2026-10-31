package pe.utp.peluqueria.repository;

import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.peluqueria.model.*;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByEstadoEnvioAndFechaProgramadaLessThanEqualOrderByFechaProgramada(EstadoEnvio estado, LocalDateTime hasta);
    List<Notificacion> findByCitaClienteIdOrderByFechaProgramadaDesc(Long clienteId);
    void deleteByCitaIdAndTipoAndEstadoEnvio(Long citaId, TipoNotificacion tipo, EstadoEnvio estado);
    boolean existsByCitaIdAndTipo(Long citaId, TipoNotificacion tipo);
}
