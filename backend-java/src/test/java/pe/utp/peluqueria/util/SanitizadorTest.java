package pe.utp.peluqueria.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SanitizadorTest {
    @Test
    void limpiaEspaciosYNormaliza() { assertEquals("Ana María", Sanitizador.limpiar("   Ana    María  ")); }

    @Test
    void nuloSeConvierteEnCadenaVacia() { assertEquals("", Sanitizador.limpiar(null)); }

    @Test
    void enmascaraElCorreoParaLosRegistros() {
        assertEquals("va***@correo.com", Sanitizador.enmascararCorreo("valeria@correo.com"));
        assertEquals("***", Sanitizador.enmascararCorreo("sin-arroba"));
    }

    @Test
    void neutralizaFormulasPeligrosas() {
        assertEquals("'=1+1", Sanitizador.neutralizarFormula("=1+1"));
        assertEquals("'@SUM(A1)", Sanitizador.neutralizarFormula("@SUM(A1)"));
        assertEquals("Corte", Sanitizador.neutralizarFormula("Corte"));
    }
}
