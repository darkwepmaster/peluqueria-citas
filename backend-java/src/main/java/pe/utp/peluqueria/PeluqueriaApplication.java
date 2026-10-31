package pe.utp.peluqueria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Punto de entrada de la API REST de gestión y reserva de citas. */
@SpringBootApplication
@EnableScheduling
public class PeluqueriaApplication {
    public static void main(String[] args) {
        SpringApplication.run(PeluqueriaApplication.class, args);
    }
}
