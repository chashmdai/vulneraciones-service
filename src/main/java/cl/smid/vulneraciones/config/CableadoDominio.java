package cl.smid.vulneraciones.config;

import cl.smid.vulneraciones.dominio.puerto.salida.ClienteCasos;
import cl.smid.vulneraciones.dominio.puerto.salida.ClienteCatalogo;
import cl.smid.vulneraciones.dominio.puerto.salida.ClientePersonas;
import cl.smid.vulneraciones.dominio.puerto.salida.CorrelativoFirPort;
import cl.smid.vulneraciones.dominio.puerto.salida.DirectorioSedes;
import cl.smid.vulneraciones.dominio.puerto.salida.GeneradorIdentificadores;
import cl.smid.vulneraciones.dominio.puerto.salida.ProtectorDatosReservados;
import cl.smid.vulneraciones.dominio.puerto.salida.PublicadorEventos;
import cl.smid.vulneraciones.dominio.puerto.salida.Reloj;
import cl.smid.vulneraciones.dominio.puerto.salida.RepositorioFichas;
import cl.smid.vulneraciones.dominio.servicio.EvaluadorAlcance;
import cl.smid.vulneraciones.dominio.servicio.GeneradorNumeroFir;
import cl.smid.vulneraciones.dominio.servicio.MaquinaEstadosFicha;
import cl.smid.vulneraciones.dominio.servicio.ServicioVulneraciones;
import cl.smid.vulneraciones.infraestructura.reservado.ProtectorAesGcm;
import cl.smid.vulneraciones.infraestructura.reservado.ProtectorPassthrough;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Raíz de composición del dominio (hexagonal estricto): instancia los servicios POJO puros, el
 * protector de datos reservados según configuración y el orquestador {@link ServicioVulneraciones},
 * inyectándole los puertos de salida (adaptadores de infraestructura) y las listas de roles.
 *
 * <p>El dominio no conoce Spring; aquí se realiza todo el cableado. Las anotaciones de configuración
 * viven exclusivamente en esta capa.</p>
 */
@Configuration
public class CableadoDominio {

    private static final Logger log = LoggerFactory.getLogger(CableadoDominio.class);

    /** Corte por defecto de la serie oficial si no se configura {@code smid.fir.inicio-oficial}. */
    private static final LocalDate INICIO_OFICIAL_DEFECTO = LocalDate.of(2027, 1, 1);

    private static final List<String> ROLES_COORDINACION_DEFECTO =
            List.of("COORDINADOR", "COORDINACION", "ADMIN_SEDE", "ADMIN_NACIONAL");

    private static final List<String> ROLES_ACCESO_RESERVADO_DEFECTO =
            List.of("COORDINADOR", "ADMIN_SEDE", "ADMIN_NACIONAL");

    @Bean
    public EvaluadorAlcance evaluadorAlcance() {
        return new EvaluadorAlcance();
    }

    @Bean
    public MaquinaEstadosFicha maquinaEstadosFicha() {
        return new MaquinaEstadosFicha();
    }

    @Bean
    public GeneradorNumeroFir generadorNumeroFir(PropiedadesFir propiedadesFir) {
        LocalDate inicio = propiedadesFir != null && propiedadesFir.inicioOficial() != null
                ? propiedadesFir.inicioOficial()
                : INICIO_OFICIAL_DEFECTO;
        return new GeneradorNumeroFir(inicio, ZoneOffset.UTC);
    }

    /**
     * Selecciona el protector de datos reservados (DT-1) según {@code smid.reservado.cifrado}.
     * {@code aes-gcm} → AES-256-GCM (producción); {@code none} → passthrough (solo desarrollo).
     */
    @Bean
    public ProtectorDatosReservados protectorDatosReservados(PropiedadesReservado propiedades) {
        String perfil = propiedades == null || propiedades.cifrado() == null
                ? "aes-gcm" : propiedades.cifrado().trim().toLowerCase();
        if ("none".equals(perfil)) {
            log.warn("Cifrado de datos reservados DESACTIVADO (perfil 'none'): solo apto para desarrollo/pruebas.");
            return new ProtectorPassthrough();
        }
        return new ProtectorAesGcm(propiedades == null ? null : propiedades.clave());
    }

    @Bean
    public ServicioVulneraciones servicioVulneraciones(
            RepositorioFichas repositorio,
            CorrelativoFirPort correlativo,
            DirectorioSedes directorioSedes,
            PublicadorEventos publicador,
            ProtectorDatosReservados protector,
            ClienteCasos clienteCasos,
            ClientePersonas clientePersonas,
            ClienteCatalogo clienteCatalogo,
            Reloj reloj,
            GeneradorIdentificadores generador,
            MaquinaEstadosFicha maquina,
            GeneradorNumeroFir generadorNumero,
            EvaluadorAlcance evaluador,
            PropiedadesSeguridad propiedadesSeguridad) {
        Set<String> rolesCoordinacion = resolverRoles(
                propiedadesSeguridad == null ? null : propiedadesSeguridad.rolesCoordinacion(),
                ROLES_COORDINACION_DEFECTO);
        Set<String> rolesAccesoReservado = resolverRoles(
                propiedadesSeguridad == null ? null : propiedadesSeguridad.rolesAccesoReservado(),
                ROLES_ACCESO_RESERVADO_DEFECTO);
        return new ServicioVulneraciones(repositorio, correlativo, directorioSedes, publicador, protector,
                clienteCasos, clientePersonas, clienteCatalogo, reloj, generador, maquina, generadorNumero,
                evaluador, rolesCoordinacion, rolesAccesoReservado);
    }

    private Set<String> resolverRoles(List<String> configurados, List<String> defecto) {
        List<String> fuente = (configurados == null || configurados.isEmpty()) ? defecto : configurados;
        Set<String> normalizados = new LinkedHashSet<>();
        for (String rol : fuente) {
            if (rol != null && !rol.isBlank()) {
                normalizados.add(rol.trim().toUpperCase());
            }
        }
        return Set.copyOf(normalizados);
    }
}
