package pe.utp.peluqueria.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;
import pe.utp.peluqueria.dto.Dtos.CitaDto;
import pe.utp.peluqueria.util.Sanitizador;

/** Exporta a CSV con Apache Commons CSV (separador «;» y BOM UTF-8 para que Excel abra bien las tildes). */
@Component
public class ExportadorCsv implements Exportador {
    static final String[] ENCABEZADO = {"Fecha", "Hora", "Cliente", "Estilista", "Servicios", "Estado", "Total"};

    @Override public String formato() { return "csv"; }

    @Override public String contentType() { return "text/csv; charset=UTF-8"; }

    @Override
    public byte[] exportar(List<CitaDto> citas) {
        CSVFormat formato = CSVFormat.DEFAULT.builder().setDelimiter(';').setHeader(ENCABEZADO).build();
        try (StringWriter sw = new StringWriter(); CSVPrinter p = new CSVPrinter(sw, formato)) {
            for (CitaDto c : citas) {
                p.printRecord(c.fecha(), c.horaInicio(), Sanitizador.neutralizarFormula(c.clienteNombre()),
                        Sanitizador.neutralizarFormula(c.estilistaNombre()), Sanitizador.neutralizarFormula(String.join(", ", c.servicios())),
                        c.estado(), c.total());
            }
            p.flush();
            return ("\uFEFF" + sw).getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
