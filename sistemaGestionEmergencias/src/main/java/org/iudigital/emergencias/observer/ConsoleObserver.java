package org.iudigital.emergencias.observer;

import org.iudigital.emergencias.domain.Ambulancia;
import org.iudigital.emergencias.domain.CasoEmergencia;
import org.iudigital.emergencias.domain.EquipoMedico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de observer para logging de eventos.
 * Registra todos los eventos del sistema en el logger.
 */
public class ConsoleObserver implements EmergenciaObserver {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleObserver.class);

    @Override
    public void onNuevoCasoRecibido(CasoEmergencia caso, String operadorId, int pendientes) {
        System.out.printf("📞 %s Recibe llamada #%d: %s en %s. Pendientes: %d\n",
                operadorId,
                caso.getCasoId(),
                caso.getSeveridad(),
                caso.getLugar(),
                pendientes);
        logger.info("Caso {} recibido por {} - Severidad: {} - Pendientes: {}",
                caso.getCasoId(), operadorId, caso.getSeveridad(), pendientes);
    }

    @Override
    public void onCasoAsignado(CasoEmergencia caso, Ambulancia ambulancia) {
        System.out.printf("🚑 [ASIGNADO] Ambulancia %d asignada a caso #%d (%s)\n",
                ambulancia.getIdAmbulancia(),
                caso.getCasoId(),
                caso.getSeveridad());
        logger.info("Caso {} asignado a ambulancia {}", caso.getCasoId(), ambulancia.getIdAmbulancia());
    }

    @Override
    public void onEquipoMedicoAsignado(CasoEmergencia caso, EquipoMedico equipo) {
        System.out.printf("⚕️ [ASIGNADO] Equipo Médico %d asignado a caso #%d\n",
                equipo.getIdEquipo(),
                caso.getCasoId());
        logger.info("Equipo médico {} asignado a caso {}", equipo.getIdEquipo(), caso.getCasoId());
    }

    @Override
    public void onCasoCompletado(CasoEmergencia caso, long tiempoEsperaMs, long tiempoTotalMs) {
        System.out.printf("✅ [COMPLETADO] Caso #%d finalizado - Espera: %.2fs, Total: %.2fs\n",
                caso.getCasoId(),
                tiempoEsperaMs / 1000.0,
                tiempoTotalMs / 1000.0);
        logger.info("Caso {} completado - Espera: {}ms, Total: {}ms",
                caso.getCasoId(), tiempoEsperaMs, tiempoTotalMs);
    }

    @Override
    public void onCambioEstadoAmbulancia(Ambulancia ambulancia,
            Ambulancia.StatusAmbulancia estadoAnterior,
            Ambulancia.StatusAmbulancia estadoNuevo) {
        // Solo mostrar cambios significativos para no saturar la consola
        if (estadoNuevo == Ambulancia.StatusAmbulancia.DISPONIBLE ||
                estadoNuevo == Ambulancia.StatusAmbulancia.EN_RUTA) {
            System.out.printf("🚑 Ambulancia %d: %s → %s\n",
                    ambulancia.getIdAmbulancia(),
                    estadoAnterior,
                    estadoNuevo);
        }
        logger.debug("Ambulancia {} cambió de {} a {}",
                ambulancia.getIdAmbulancia(), estadoAnterior, estadoNuevo);
    }

    @Override
    public void onCambioEstadoEquipoMedico(EquipoMedico equipo,
            EquipoMedico.StatusEquipo estadoAnterior,
            EquipoMedico.StatusEquipo estadoNuevo) {
        // Solo mostrar cambios significativos
        if (estadoNuevo == EquipoMedico.StatusEquipo.DISPONIBLE ||
                estadoNuevo == EquipoMedico.StatusEquipo.ASIGNADO) {
            System.out.printf("⚕️ Equipo Médico %d: %s → %s\n",
                    equipo.getIdEquipo(),
                    estadoAnterior,
                    estadoNuevo);
        }
        logger.debug("Equipo médico {} cambió de {} a {}",
                equipo.getIdEquipo(), estadoAnterior, estadoNuevo);
    }

    @Override
    public void onRecursoNoDisponible(CasoEmergencia caso, String tipoRecurso) {
        System.out.printf("⚠️ [ALERTA] No hay %s disponible para caso #%d (%s)\n",
                tipoRecurso,
                caso.getCasoId(),
                caso.getSeveridad());
        logger.warn("No hay {} disponible para caso {}", tipoRecurso, caso.getCasoId());
    }
}
