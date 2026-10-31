package pe.utp.peluqueria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import pe.utp.peluqueria.dao.CitaDao;
import pe.utp.peluqueria.dto.Dtos.ReservaRequest;
import pe.utp.peluqueria.exception.ApiException;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.*;
import pe.utp.peluqueria.security.Sesion;

/** Pruebas de la lógica de negocio con el DAO simulado (principio de inversión de dependencias). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CitaServiceTest {
    @Mock CitaDao citas;
    @Mock ClienteRepository clientes;
    @Mock EstilistaRepository estilistas;
    @Mock ServicioRepository servicios;
    @Mock NotificacionService notificaciones;
    CitaService service;
    Cliente cliente;
    Estilista estilista;
    Servicio corte;

    @BeforeEach
    void preparar() {
        service = new CitaService(citas, clientes, estilistas, servicios, notificaciones, "America/Lima");
        cliente = new Cliente(); cliente.setId(1L); cliente.setNombres("Valeria"); cliente.setApellidos("Rojas");
        estilista = new Estilista(); estilista.setId(2L); estilista.setNombres("Milagros"); estilista.setApellidos("Ramos"); estilista.setActivo(true);
        corte = new Servicio(); corte.setId(5L); corte.setNombre("Corte"); corte.setPrecio(new BigDecimal("35.00")); corte.setDuracionMin(60); corte.setActivo(true);
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(estilistas.findById(2L)).thenReturn(Optional.of(estilista));
        when(servicios.findAllById(List.of(5L))).thenReturn(List.of(corte));
    }

    private Cita cita(EstadoCita estado) {
        Cita c = new Cita();
        c.setId(10L); c.setCliente(cliente); c.setEstilista(estilista); c.setEstado(estado);
        c.setFecha(LocalDate.now().plusDays(3)); c.setHoraInicio(LocalTime.of(10, 0)); c.setHoraFin(LocalTime.of(11, 0));
        c.setTotal(new BigDecimal("35.00"));
        DetalleCita d = new DetalleCita(); d.setServicio(corte); d.setDuracionMin(60); d.setPrecioAplicado(corte.getPrecio());
        c.agregarDetalle(d);
        when(citas.buscarPorId(10L)).thenReturn(Optional.of(c));
        return c;
    }

    @Test
    void confirmarUnaCitaPendienteProgramaNotificaciones() {
        Cita c = cita(EstadoCita.PENDIENTE);
        assertEquals("CONFIRMADA", service.confirmar(10L).estado());
        verify(notificaciones).programar(c, TipoNotificacion.CONFIRMACION);
        verify(notificaciones).programarRecordatorios(c);
    }

    @Test
    void noSePuedeConfirmarUnaCitaCancelada() {
        cita(EstadoCita.CANCELADA);
        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class, () -> service.confirmar(10L)).getStatus());
    }

    @Test
    void unClienteNoPuedeCancelarLaCitaDeOtro() {
        cita(EstadoCita.PENDIENTE);
        assertEquals(HttpStatus.FORBIDDEN,
                assertThrows(ApiException.class, () -> service.cancelar(10L, new Sesion(99L, "otro@correo.com", Rol.CLIENTE), null)).getStatus());
    }

    @Test
    void elClienteCancelaSuCitaYSeLiberaElHorario() {
        Cita c = cita(EstadoCita.CONFIRMADA);
        service.cancelar(10L, new Sesion(1L, "valeria@correo.com", Rol.CLIENTE), "Imprevisto");
        assertEquals(EstadoCita.CANCELADA, c.getEstado());
        verify(notificaciones).descartarRecordatorios(c);
    }

    @Test
    void soloElEstilistaAsignadoPuedeCerrarLaCita() {
        cita(EstadoCita.CONFIRMADA);
        assertEquals(HttpStatus.FORBIDDEN,
                assertThrows(ApiException.class, () -> service.cerrar(10L, new Sesion(77L, "otra@bellaestilo.pe", Rol.ESTILISTA), true)).getStatus());
    }

    @Test
    void reservarUnHorarioOcupadoDevuelveConflicto() {
        LocalDate fecha = LocalDate.now().plusDays(7);
        estilista.agregarHorario(HorarioEstilista.de(fecha.getDayOfWeek(), LocalTime.of(9, 0), LocalTime.of(13, 0)));
        Cita ocupada = new Cita();
        ocupada.setId(3L); ocupada.setHoraInicio(LocalTime.of(10, 0)); ocupada.setHoraFin(LocalTime.of(11, 0));
        when(citas.bloquearActivasDelDia(2L, fecha)).thenReturn(List.of(ocupada));
        ApiException e = assertThrows(ApiException.class,
                () -> service.crear(1L, new ReservaRequest(2L, fecha, LocalTime.of(10, 0), List.of(5L))));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        verify(citas, never()).guardar(any());
    }

    @Test
    void reservarUnHorarioLibreRegistraLaCitaPendiente() {
        LocalDate fecha = LocalDate.now().plusDays(7);
        estilista.agregarHorario(HorarioEstilista.de(fecha.getDayOfWeek(), LocalTime.of(9, 0), LocalTime.of(13, 0)));
        when(citas.bloquearActivasDelDia(2L, fecha)).thenReturn(List.of());
        var dto = service.crear(1L, new ReservaRequest(2L, fecha, LocalTime.of(10, 0), List.of(5L)));
        assertEquals("PENDIENTE", dto.estado());
        assertEquals(LocalTime.of(11, 0), dto.horaFin());
        verify(citas).guardar(any(Cita.class));
    }
}
