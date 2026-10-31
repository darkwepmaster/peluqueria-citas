package pe.utp.peluqueria.repository;

import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.peluqueria.model.*;

public interface EstilistaRepository extends JpaRepository<Estilista, Long> {
    List<Estilista> findByActivoTrueOrderByNombres();
}
