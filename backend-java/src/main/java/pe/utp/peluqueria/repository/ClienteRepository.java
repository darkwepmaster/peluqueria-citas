package pe.utp.peluqueria.repository;

import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.peluqueria.model.*;

public interface ClienteRepository extends JpaRepository<Cliente, Long> { }
