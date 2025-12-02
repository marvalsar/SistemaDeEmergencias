package org.iudigital.emergencias.factory;

import org.iudigital.emergencias.domain.Ambulancia;
import org.iudigital.emergencias.domain.CasoEmergencia;
import org.iudigital.emergencias.domain.EquipoMedico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Factory para la creación de recursos del sistema de emergencias.
 * Implementa el patrón Factory Method para centralizar y estandarizar
 * la creación de Ambulancias y Equipos Médicos.
 */
public class RecursoFactory {

    private static final Logger logger = LoggerFactory.getLogger(RecursoFactory.class);

    // Contadores atómicos para IDs únicos
    private static final AtomicInteger ambulanciaIdCounter = new AtomicInteger(100);
    private static final AtomicInteger equipoMedicoIdCounter = new AtomicInteger(200);

    // Prefijos para identificación
    private static final int AMBULANCIA_ID_PREFIX = 100;
    private static final int EQUIPO_MEDICO_ID_PREFIX = 200;

    /**
     * Tipo de recurso que puede crear la factory.
     */
    public enum TipoRecurso {
        AMBULANCIA,
        EQUIPO_MEDICO
    }

    /**
     * Constructor privado para evitar instanciación.
     * Esta clase funciona como utility class con métodos estáticos.
     */
    private RecursoFactory() {
        throw new IllegalStateException("Utility class - no debe ser instanciada");
    }

    /**
     * Crea una nueva ambulancia con ID único.
     * 
     * @param casosCompletados lista compartida de casos completados
     * @return nueva instancia de Ambulancia
     */
    public static Ambulancia crearAmbulancia(List<CasoEmergencia> casosCompletados) {
        int id = ambulanciaIdCounter.incrementAndGet();
        Ambulancia ambulancia = new Ambulancia(id, casosCompletados);
        logger.debug("Ambulancia creada con ID: {}", id);
        return ambulancia;
    }

    /**
     * Crea un nuevo equipo médico con ID único.
     * 
     * @return nueva instancia de EquipoMedico
     */
    public static EquipoMedico crearEquipoMedico() {
        int id = equipoMedicoIdCounter.incrementAndGet();
        EquipoMedico equipo = new EquipoMedico(id);
        logger.debug("Equipo Médico creado con ID: {}", id);
        return equipo;
    }

    /**
     * Crea múltiples ambulancias en lote.
     * 
     * @param cantidad         número de ambulancias a crear
     * @param casosCompletados lista compartida de casos completados
     * @return lista de ambulancias creadas
     */
    public static List<Ambulancia> crearAmbulancias(int cantidad, List<CasoEmergencia> casosCompletados) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        logger.info("Creando {} ambulancias", cantidad);
        return java.util.stream.IntStream.range(0, cantidad)
                .mapToObj(i -> crearAmbulancia(casosCompletados))
                .toList();
    }

    /**
     * Crea múltiples equipos médicos en lote.
     * 
     * @param cantidad número de equipos médicos a crear
     * @return lista de equipos médicos creados
     */
    public static List<EquipoMedico> crearEquiposMedicos(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        logger.info("Creando {} equipos médicos", cantidad);
        return java.util.stream.IntStream.range(0, cantidad)
                .mapToObj(i -> crearEquipoMedico())
                .toList();
    }

    /**
     * Reinicia los contadores de IDs (útil para testing).
     */
    public static void resetContadores() {
        ambulanciaIdCounter.set(AMBULANCIA_ID_PREFIX);
        equipoMedicoIdCounter.set(EQUIPO_MEDICO_ID_PREFIX);
        logger.debug("Contadores de IDs reiniciados");
    }

    /**
     * Obtiene el próximo ID disponible para ambulancias sin incrementar el
     * contador.
     * 
     * @return el próximo ID de ambulancia
     */
    public static int getProximoIdAmbulancia() {
        return ambulanciaIdCounter.get() + 1;
    }

    /**
     * Obtiene el próximo ID disponible para equipos médicos sin incrementar el
     * contador.
     * 
     * @return el próximo ID de equipo médico
     */
    public static int getProximoIdEquipoMedico() {
        return equipoMedicoIdCounter.get() + 1;
    }
}
