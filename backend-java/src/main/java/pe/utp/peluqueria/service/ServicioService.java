package pe.utp.peluqueria.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.peluqueria.dto.Dtos.*;
import pe.utp.peluqueria.exception.ApiException;
import pe.utp.peluqueria.model.*;
import pe.utp.peluqueria.repository.*;
import pe.utp.peluqueria.util.Sanitizador;

/** Mantenimiento del catálogo de servicios (responsabilidad única). */
@Service
@Transactional
public class ServicioService {
    private final ServicioRepository servicios;
    private final CategoriaRepository categorias;

    public ServicioService(ServicioRepository servicios, CategoriaRepository categorias) { this.servicios = servicios; this.categorias = categorias; }

    @Transactional(readOnly = true)
    public List<ServicioDto> activos() { return servicios.findByActivoTrueOrderByNombre().stream().map(Mapeador::servicio).toList(); }

    @Transactional(readOnly = true)
    public List<ServicioDto> todos() { return servicios.findAll().stream().map(Mapeador::servicio).toList(); }

    public ServicioDto crear(ServicioRequest r) { return Mapeador.servicio(servicios.save(aplicar(new Servicio(), r))); }

    public ServicioDto actualizar(Long id, ServicioRequest r) {
        Servicio s = servicios.findById(id).orElseThrow(() -> ApiException.noEncontrado("Servicio"));
        return Mapeador.servicio(aplicar(s, r));
    }

    private Servicio aplicar(Servicio s, ServicioRequest r) {
        String nombreCat = Sanitizador.limpiar(r.categoria());
        CategoriaServicio cat = categorias.findByNombreIgnoreCase(nombreCat).orElseGet(() -> {
            CategoriaServicio nueva = new CategoriaServicio();
            nueva.setNombre(nombreCat);
            return categorias.save(nueva);
        });
        s.setNombre(Sanitizador.limpiar(r.nombre()));
        s.setDescripcion(Sanitizador.limpiar(r.descripcion()));
        s.setCategoria(cat);
        s.setPrecio(r.precio());
        s.setDuracionMin(r.duracionMin());
        s.setActivo(r.activo());
        return s;
    }
}
