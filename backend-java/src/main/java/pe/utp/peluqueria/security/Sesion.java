package pe.utp.peluqueria.security;

import pe.utp.peluqueria.model.Rol;

/** Datos del usuario autenticado, extraídos del token JWT. */
public record Sesion(Long id, String email, Rol rol) {
    public boolean esAdmin() { return rol == Rol.ADMINISTRADOR; }
}
