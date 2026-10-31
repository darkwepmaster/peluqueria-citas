package pe.utp.peluqueria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Cliente que reserva citas. */
@Entity
public class Cliente extends Usuario {
    private boolean aceptaWhatsapp;

    private boolean aceptaCorreo;

    @Column(length = 200)
    private String observaciones;

    public boolean isAceptaWhatsapp() { return aceptaWhatsapp; }
    public void setAceptaWhatsapp(boolean aceptaWhatsapp) { this.aceptaWhatsapp = aceptaWhatsapp; }
    public boolean isAceptaCorreo() { return aceptaCorreo; }
    public void setAceptaCorreo(boolean aceptaCorreo) { this.aceptaCorreo = aceptaCorreo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
