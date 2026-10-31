package pe.utp.peluqueria.export;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import pe.utp.peluqueria.dto.Dtos.CitaDto;

class ExportadorTest {
    private final List<CitaDto> citas = List.of(new CitaDto(1L, 1L, "=HYPERLINK(\"http://malo\")", 2L, "Milagros Ramos",
            LocalDate.of(2026, 6, 4), LocalTime.of(15, 0), LocalTime.of(15, 40), "CONFIRMADA", new BigDecimal("35.00"), List.of("Corte"), null));

    @Test
    void csvTieneEncabezadoYNeutralizaFormulas() {
        String csv = new String(new ExportadorCsv().exportar(citas), StandardCharsets.UTF_8);
        assertTrue(csv.contains("Fecha;Hora;Cliente;Estilista;Servicios;Estado;Total"));
        assertTrue(csv.contains("'=HYPERLINK"));
        assertTrue(csv.contains("Milagros Ramos"));
    }

    @Test
    void excelGeneraUnaHojaConLosDatos() throws IOException {
        byte[] bytes = new ExportadorExcel().exportar(citas);
        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet hoja = libro.getSheet("Citas");
            assertNotNull(hoja);
            assertEquals("Fecha", hoja.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Milagros Ramos", hoja.getRow(1).getCell(3).getStringCellValue());
            assertEquals(35.0, hoja.getRow(1).getCell(6).getNumericCellValue(), 0.001);
            assertTrue(hoja.getRow(1).getCell(2).getStringCellValue().startsWith("'="));
        }
    }
}
