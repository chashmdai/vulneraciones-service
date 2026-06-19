package cl.smid.vulneraciones.api.mapper;

import cl.smid.vulneraciones.api.dto.AntecedenteResponse;
import cl.smid.vulneraciones.api.dto.ContenidoReservadoResponse;
import cl.smid.vulneraciones.api.dto.EnriquecimientoResponse;
import cl.smid.vulneraciones.api.dto.FichaDetalleResponse;
import cl.smid.vulneraciones.api.dto.FichaResumenResponse;
import cl.smid.vulneraciones.api.dto.ItemReservadoResponse;
import cl.smid.vulneraciones.api.dto.PaginaResponse;
import cl.smid.vulneraciones.api.dto.TransicionResponse;
import cl.smid.vulneraciones.api.dto.VulneracionResponse;
import cl.smid.vulneraciones.dominio.modelo.Complejidad;
import cl.smid.vulneraciones.dominio.modelo.EstadoFicha;
import cl.smid.vulneraciones.dominio.modelo.Ficha;
import cl.smid.vulneraciones.dominio.modelo.ResultadoPagina;
import cl.smid.vulneraciones.dominio.modelo.Transicion;
import cl.smid.vulneraciones.dominio.modelo.vista.ContenidoReservado;
import cl.smid.vulneraciones.dominio.modelo.vista.EnriquecimientoFicha;
import cl.smid.vulneraciones.dominio.modelo.vista.ItemReservado;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaAntecedente;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaFicha;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaVulneracion;

import java.util.List;

/**
 * Ensamblador de la capa API: traduce las vistas y modelos de dominio a los DTOs de respuesta. Los
 * enumerados se exponen por su nombre (cadena) para estabilizar el contrato JSON. Utilidad sin estado.
 */
public final class EnsambladorRespuestas {

    private EnsambladorRespuestas() {
    }

    public static FichaDetalleResponse aDetalle(VistaFicha v) {
        return new FichaDetalleResponse(
                v.altKey(),
                v.numeroFicha(),
                v.idCasoAlt(),
                v.numeroExpediente(),
                nombre(v.estado()),
                nombre(v.complejidad()),
                v.idSedeAlt(),
                v.idUnidadAlt(),
                v.esBeta(),
                v.abiertaEn(),
                v.cerradaEn(),
                v.creadaEn(),
                v.actualizadaEn(),
                v.reservadoOculto(),
                aEnriquecimiento(v.enriquecimiento()),
                v.vulneraciones().stream().map(EnsambladorRespuestas::aVulneracion).toList(),
                v.antecedentes().stream().map(EnsambladorRespuestas::aAntecedente).toList(),
                v.historial().stream().map(EnsambladorRespuestas::aTransicion).toList());
    }

    public static FichaResumenResponse aResumen(Ficha f) {
        return new FichaResumenResponse(
                f.getAltKey(),
                f.getNumeroFicha(),
                f.getIdCasoAlt(),
                f.getNumeroExpediente(),
                f.getEstado() == null ? null : f.getEstado().name(),
                f.getComplejidad() == null ? null : f.getComplejidad().name(),
                f.getIdSedeAlt(),
                f.getIdUnidadAlt(),
                f.isEsBeta(),
                f.getAbiertaEn(),
                f.getCerradaEn(),
                f.getCreadaEn(),
                f.getActualizadaEn());
    }

    public static PaginaResponse<FichaResumenResponse> aPagina(ResultadoPagina<Ficha> pagina) {
        List<FichaResumenResponse> contenido = pagina.contenido().stream()
                .map(EnsambladorRespuestas::aResumen)
                .toList();
        return new PaginaResponse<>(contenido, pagina.pagina(), pagina.tamano(), pagina.total());
    }

    public static VulneracionResponse aVulneracion(VistaVulneracion v) {
        return new VulneracionResponse(
                v.altKey(),
                v.idDerechoAlt(),
                v.idCausaAlt(),
                v.idNnaAlt(),
                v.nnaNombreLegible(),
                v.etiquetaDerecho(),
                v.etiquetaCausa(),
                v.gravedad() == null ? null : v.gravedad().name(),
                v.relato(),
                v.fechaHecho(),
                v.registradoEn(),
                v.registradoPor());
    }

    public static AntecedenteResponse aAntecedente(VistaAntecedente a) {
        return new AntecedenteResponse(
                a.altKey(),
                a.tipo() == null ? null : a.tipo().name(),
                a.descripcion(),
                a.fecha(),
                a.fuente(),
                a.registradoEn(),
                a.registradoPor());
    }

    public static TransicionResponse aTransicion(Transicion t) {
        return new TransicionResponse(
                t.altKey(),
                t.estadoOrigen() == null ? null : t.estadoOrigen().name(),
                t.estadoDestino() == null ? null : t.estadoDestino().name(),
                t.accion() == null ? null : t.accion().name(),
                t.observacion(),
                t.actorAlt(),
                t.ocurridoEn());
    }

    public static EnriquecimientoResponse aEnriquecimiento(EnriquecimientoFicha e) {
        if (e == null) {
            return new EnriquecimientoResponse(false, null, null);
        }
        return new EnriquecimientoResponse(e.disponible(), e.estadoCaso(), e.numeroExpedienteVigente());
    }

    public static ContenidoReservadoResponse aReservado(ContenidoReservado c) {
        return new ContenidoReservadoResponse(
                c.fichaAltKey(),
                c.relatos().stream().map(EnsambladorRespuestas::aItem).toList(),
                c.descripciones().stream().map(EnsambladorRespuestas::aItem).toList());
    }

    private static ItemReservadoResponse aItem(ItemReservado i) {
        return new ItemReservadoResponse(i.altKey(), i.texto());
    }

    private static String nombre(EstadoFicha estado) {
        return estado == null ? null : estado.name();
    }

    private static String nombre(Complejidad complejidad) {
        return complejidad == null ? null : complejidad.name();
    }
}
