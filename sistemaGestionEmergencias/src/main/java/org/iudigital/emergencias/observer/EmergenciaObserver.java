package org.iudigital.emergencias.observer;

import org.iudigital.emergencias.domain.Ambulancia;
import org.iudigital.emergencias.domain.CasoEmergencia;
import org.iudigital.emergencias.domain.EquipoMedico;

/**
 * Interface Observer para recibir notificaciones de eventos del sistema.
 * Implementa el patrón Observer para desacoplar la lógica de negocio
 * de la presentación y logging.
 */
public interface EmergenciaObserver {

    /**
     * Notifica cuando se recibe un nuevo caso de emergencia.
     * 
     * @param caso       el caso de emergencia recibido
     * @param operadorId ID del operador que recibió la llamada
     * @param pendientes número de casos pendientes en cola
     */
    void onNuevoCasoRecibido(CasoEmergencia caso, String operadorId, int pendientes);

    /**
     * Notifica cuando un caso es asignado a una ambulancia.
     * 
     * @param caso       el caso asignado
     * @param ambulancia la ambulancia asignada
     */
    void onCasoAsignado(CasoEmergencia caso, Ambulancia ambulancia);

    /**
     * Notifica cuando un equipo médico es asignado a un caso.
     * 
     * @param caso   el caso que requiere equipo médico
     * @param equipo el equipo médico asignado
     */
    void onEquipoMedicoAsignado(CasoEmergencia caso, EquipoMedico equipo);

    /**
     * Notifica cuando un caso ha sido completado.
     * 
     * @param caso           el caso completado
     * @param tiempoEsperaMs tiempo de espera en milisegundos
     * @param tiempoTotalMs  tiempo total de servicio en milisegundos
     */
    void onCasoCompletado(CasoEmergencia caso, long tiempoEsperaMs, long tiempoTotalMs);

    /**
     * Notifica cuando cambia el estado de una ambulancia.
     * 
     * @param ambulancia     la ambulancia con cambio de estado
     * @param estadoAnterior el estado anterior
     * @param estadoNuevo    el estado nuevo
     */
    void onCambioEstadoAmbulancia(Ambulancia ambulancia,
            Ambulancia.StatusAmbulancia estadoAnterior,
            Ambulancia.StatusAmbulancia estadoNuevo);

    /**
     * Notifica cuando cambia el estado de un equipo médico.
     * 
     * @param equipo         el equipo médico con cambio de estado
     * @param estadoAnterior el estado anterior
     * @param estadoNuevo    el estado nuevo
     */
    void onCambioEstadoEquipoMedico(EquipoMedico equipo,
            EquipoMedico.StatusEquipo estadoAnterior,
            EquipoMedico.StatusEquipo estadoNuevo);

    /**
     * Notifica cuando no hay recursos disponibles para atender un caso.
     * 
     * @param caso        el caso que no puede ser atendido
     * @param tipoRecurso el tipo de recurso no disponible
     */
    void onRecursoNoDisponible(CasoEmergencia caso, String tipoRecurso);
}
