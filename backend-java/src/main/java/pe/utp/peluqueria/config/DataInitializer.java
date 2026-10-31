package pe.utp.peluqueria.config;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.*;

/** Datos de demostración. Las contraseñas son SOLO para pruebas: cámbielas (app.seed=false) antes de publicar el sistema. */
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true")
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final UsuarioRepository usuarios;
    private final EstilistaRepository estilistas;
    private final ClienteRepository clientes;
    private final CategoriaRepository categorias;
    private final ServicioRepository servicios;
    private final PasswordEncoder encoder;

    public DataInitializer(UsuarioRepository usuarios, EstilistaRepository estilistas, ClienteRepository clientes,
                           CategoriaRepository categorias, ServicioRepository servicios, PasswordEncoder encoder) {
        this.usuarios = usuarios; this.estilistas = estilistas; this.clientes = clientes;
        this.categorias = categorias; this.servicios = servicios; this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.count() > 0) return;
        Administrador admin = new Administrador();
        datos(admin, "Recepción", "Bella Estilo", "admin@bellaestilo.pe", "Admin123!", Rol.ADMINISTRADOR);
        usuarios.save(admin);
        Cliente demo = new Cliente();
        datos(demo, "Valeria", "Rojas", "valeria@correo.com", "Cliente123!", Rol.CLIENTE);
        demo.setAceptaWhatsapp(true);
        demo.setAceptaCorreo(true);
        clientes.save(demo);
        estilista("Milagros", "Ramos", "milagros@bellaestilo.pe", "Corte");
        estilista("Rosa", "Salas", "rosa@bellaestilo.pe", "Color y tratamientos");
        estilista("Jimena", "Torres", "jimena@bellaestilo.pe", "Peinados de evento");
        servicio("Corte", "Corte de cabello", "Corte con estilista a elección", "35.00", 40);
        servicio("Color", "Tinte y color", "Incluye lavado", "120.00", 90);
        servicio("Peinado", "Peinado para evento", "Reservar con 24 h de anticipación", "80.00", 60);
        servicio("Tratamiento", "Tratamiento capilar", "Hidratación profunda", "65.00", 50);
        servicio("Peinado", "Lavado y secado", "Incluye masaje capilar", "25.00", 30);
        log.info("Datos de demostración cargados: admin@bellaestilo.pe / Admin123!  ·  valeria@correo.com / Cliente123!");
    }

    private void datos(Usuario u, String nombres, String apellidos, String email, String clave, Rol rol) {
        u.setNombres(nombres); u.setApellidos(apellidos); u.setEmail(email); u.setTelefono("+51 900 000 000");
        u.setPasswordHash(encoder.encode(clave)); u.setRol(rol); u.setActivo(true);
    }

    private void estilista(String nombres, String apellidos, String email, String especialidad) {
        Estilista e = new Estilista();
        datos(e, nombres, apellidos, email, "Estilista123!", Rol.ESTILISTA);
        e.setEspecialidad(especialidad);
        for (DayOfWeek d : DayOfWeek.values()) {
            if (d == DayOfWeek.SUNDAY) continue;
            e.agregarHorario(HorarioEstilista.de(d, LocalTime.of(9, 0), d == DayOfWeek.SATURDAY ? LocalTime.of(17, 0) : LocalTime.of(19, 0)));
        }
        estilistas.save(e);
    }

    private void servicio(String categoria, String nombre, String descripcion, String precio, int minutos) {
        CategoriaServicio cat = categorias.findByNombreIgnoreCase(categoria).orElseGet(() -> {
            CategoriaServicio c = new CategoriaServicio();
            c.setNombre(categoria);
            return categorias.save(c);
        });
        Servicio s = new Servicio();
        s.setCategoria(cat); s.setNombre(nombre); s.setDescripcion(descripcion);
        s.setPrecio(new BigDecimal(precio)); s.setDuracionMin(minutos); s.setActivo(true);
        servicios.save(s);
    }
}
