package org.iudigital.emergencias.observer;

import org.iudigital.emergencias.domain.Ambulancia;
import org.iudigital.emergencias.domain.CasoEmergencia;
import org.iudigital.emergencias.domain.EquipoMedico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Publisher de eventos del sistema de emergencias.
 * Implementa el patrón Observer para notificar a múltiples observadores
 * sobre eventos del sistema de manera desacoplada.
 * Thread-safe usando CopyOnWriteArrayList.
 */
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    // Lista thread-safe de observadores
    private final List<EmergenciaObserver> observers;

    /**
     * Constructor que inicializa la lista de observadores.
     */
    public EventPublisher() {
        this.observers = new CopyOnWriteArrayList<>();
    }

    /**
     * Registra un nuevo observador.
     * 
     * @param observer el observador a registrar
     */
    public void registrarObserver(EmergenciaObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            logger.debug("Observer registrado: {}", observer.getClass().getSimpleName());
        }
    }

    /**
     * Elimina un observador registrado.
     * 
     * @param observer el observador a eliminar
     */
    public void eliminarObserver(EmergenciaObserver observer) {
        if (observers.remove(observer)) {
            logger.debug("Observer eliminado: {}", observer.getClass().getSimpleName());
        }
    }

    /**
     * Elimina todos los observadores.
     */
    public void limpiarObservers() {
        int cantidad = observers.size();
        observers.clear();
        logger.debug("{} observers eliminados", cantidad);
    }

    /**
     * Publica evento de nuevo caso recibido.
     */
    public void publicarNuevoCasoRecibido(CasoEmergencia caso, String operadorId, int pendientes) {
        logger.debug("Publicando evento: nuevo caso recibido #{}", caso.getCasoId());
        for (EmergenciaObserver observer : observers) {
            try {
                observer.onNuevoCasoRecibido(caso, operadorId, pendientes);
            } catch (Exception e) {
                logger.error("Error al notificar observer sobre nuevo caso", e);
            }
        }
    }

    /**
     * Publica evento de caso asignado.
     */
    public void publicarCasoAsignado(CasoEmergencia caso, Ambulancia ambulancia) {
        logger.debug("Publicando evento: caso #{} asignado a ambulancia {}",
                caso.getCasoId(), ambulancia.getIdAmbulancia());
        for (EmergenciaObserver observer : observers) {
            try {
                observer.onCasoAsignado(caso, ambulancia);
            } catch (Exception e) {
                logger.error("Error al notificar observer sobre caso asignado", e);
            }
        }
    }

    /**
     * Publica evento de equipo médico asignado.
     */
    public void publicarEquipoMedicoAsignado(CasoEmergencia caso, EquipoMedico equipo) {
        logger.debug("Publicando evento: equipo médico {} asignado a caso #{}",
                equipo.getIdEquipo(), caso.getCasoId());
        for (EmergenciaObserver observer : observers) {
            try {
                observer.onEquipoMedicoAsignado(caso, equipo);
            } catch (Exception e) {
                logger.error("Error al notificar observer sobre equipo médico asignado", e);
            }
        }
    }

    /**
     * Publica evento de caso completado.
     */
    public void publicarCasoCompletado(CasoEmergencia caso, long tiempoEsperaMs, long tiempoTotalMs) {
        logger.debug("Publicando evento: caso #{} completado", caso.getCasoId());
        for (EmergenciaObserver observer : observers) {
            try {
                observer.onCasoCompletado(caso, tiempoEsperaMs, tiempoTotalMs);
            } catch (Exception e) {
                logger.error("Error al notificar observer sobre caso completado", e);
            }
        }
    }

    /**
     * Publica evento de cambio de estado de ambulancia.
     */
    public void publicarCambioEstadoAmbulancia(Ambulancia ambulancia,
            Ambulancia.StatusAmbulancia estadoAnterior,
            Ambulancia.StatusAmbulancia estadoNuevo) {
        logger.debug("Publicando evento: cambio estado ambulancia {} de {} a {}",
                ambulancia.getIdAmbulancia(), estadoAnterior, estadoNuevo);
        for (EmergenciaObserver observer : observers) {
            try {
                observer.onCambioEstadoAmbulancia(ambulancia, estadoAnterior, estadoNuevo);
            } catch (Exception e) {
                logger.error("Error al notificar observer sobre cambio estado ambulancia", e);
            }
        }
    }

    /**
     * Publica evento de cambio de estado de equipo médico.
     */
    public void publicarCambioEstadoEquipoMedico(EquipoMedico equipo,
            EquipoMedico.StatusEquipo estadoAnterior,
            EquipoMedico.StatusEquipo estadoNuevo) {
        logger.debug("Publicando evento: cambio estado equipo médico {} de {} a {}",
                equipo.getIdEquipo(), estadoAnterior, estadoNuevo);
        for (EmergenciaObserver observer : observers) {
            try {
                observer.onCambioEstadoEquipoMedico(equipo, estadoAnterior, estadoNuevo);
            } catch (Exception e) {
                logger.error("Error al notificar observer sobre cambio estado equipo médico", e);
            }
        }
    }

    /**
     * Publica evento de recurso no disponible.
     */
    public void publicarRecursoNoDisponible(CasoEmergencia caso, String tipoRecurso) {
        logger.debug("Publicando evento: recurso {} no disponible para caso #{}",
                tipoRecurso, caso.getCasoId());
        for (EmergenciaObserver observer : observers) {
            try {
                observer.onRecursoNoDisponible(caso, tipoRecurso);
            } catch (Exception e) {
                logger.error("Error al notificar observer sobre recurso no disponible", e);
            }
        }
    }

    /**
     * Obtiene el número de observadores registrados.
     * 
     * @return cantidad de observadores
     */
    public int getCantidadObservers() {
        return observers.size();
    }
}
