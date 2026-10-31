package pe.utp.peluqueria.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pe.utp.peluqueria.dto.Dtos.CitaDto;
import pe.utp.peluqueria.util.Sanitizador;

/** Exporta a Excel (.xlsx) con Apache POI. Los textos se neutralizan para impedir inyección de fórmulas. */
@Component
public class ExportadorExcel implements Exportador {

    @Override public String formato() { return "xlsx"; }

    @Override public String contentType() { return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; }

    @Override
    public byte[] exportar(List<CitaDto> citas) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Citas");
            Font negrita = libro.createFont();
            negrita.setBold(true);
            CellStyle estiloCab = libro.createCellStyle();
            estiloCab.setFont(negrita);
            estiloCab.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            estiloCab.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row cab = hoja.createRow(0);
            for (int i = 0; i < ExportadorCsv.ENCABEZADO.length; i++) {
                Cell cel = cab.createCell(i);
                cel.setCellValue(ExportadorCsv.ENCABEZADO[i]);
                cel.setCellStyle(estiloCab);
            }
            int fila = 1;
            for (CitaDto c : citas) {
                Row r = hoja.createRow(fila++);
                r.createCell(0).setCellValue(c.fecha().toString());
                r.createCell(1).setCellValue(c.horaInicio().toString());
                r.createCell(2).setCellValue(Sanitizador.neutralizarFormula(c.clienteNombre()));
                r.createCell(3).setCellValue(Sanitizador.neutralizarFormula(c.estilistaNombre()));
                r.createCell(4).setCellValue(Sanitizador.neutralizarFormula(String.join(", ", c.servicios())));
                r.createCell(5).setCellValue(c.estado());
                r.createCell(6).setCellValue(c.total().doubleValue());
            }
            int[] anchos = {12, 8, 28, 24, 36, 14, 10};
            for (int i = 0; i < anchos.length; i++) hoja.setColumnWidth(i, anchos[i] * 256);
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
