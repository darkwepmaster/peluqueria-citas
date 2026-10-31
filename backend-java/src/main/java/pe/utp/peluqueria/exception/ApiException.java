package pe.utp.peluqueria.exception;

import org.springframework.http.HttpStatus;

/** Error de negocio con el código HTTP que debe devolver la API. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensaje) { super(mensaje); this.status = status; }

    public HttpStatus getStatus() { return status; }

    public static ApiException noEncontrado(String que) { return new ApiException(HttpStatus.NOT_FOUND, que + " no encontrado."); }
    public static ApiException invalido(String msg) { return new ApiException(HttpStatus.BAD_REQUEST, msg); }
    public static ApiException conflicto(String msg) { return new ApiException(HttpStatus.CONFLICT, msg); }
    public static ApiException prohibido() { return new ApiException(HttpStatus.FORBIDDEN, "No tiene permiso para realizar esta acción."); }
}
