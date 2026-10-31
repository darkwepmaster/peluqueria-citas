package pe.utp.peluqueria.util;

import org.apache.commons.lang3.StringUtils;

/** Utilidades de seguridad para el tratamiento de texto (Apache Commons Lang). */
public final class Sanitizador {
    private Sanitizador() { }

    /** Recorta y normaliza espacios; devuelve cadena vacía si es nulo. */
    public static String limpiar(String texto) { return StringUtils.normalizeSpace(StringUtils.trimToEmpty(texto)); }

    public static String correo(String correo) { return StringUtils.lowerCase(StringUtils.trimToEmpty(correo)); }

    /** Enmascara un correo para los registros (logs): "va***@correo.com". */
    public static String enmascararCorreo(String correo) {
        String c = StringUtils.trimToEmpty(correo);
        int arroba = c.indexOf('@');
        if (arroba < 1) return "***";
        return StringUtils.left(c, Math.min(2, arroba)) + "***" + c.substring(arroba);
    }

    /**
     * Evita la inyección de fórmulas en Excel/CSV: si el texto empieza por = + - @ se antepone un apóstrofo,
     * de modo que Excel lo trate como texto y no como una fórmula.
     */
    public static String neutralizarFormula(String texto) {
        if (texto == null || texto.isEmpty()) return "";
        return "=+-@".indexOf(texto.charAt(0)) >= 0 ? "'" + texto : texto;
    }
}
