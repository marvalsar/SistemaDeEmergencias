package org.iudigital.emergencias.ui.observer;

import org.iudigital.emergencias.domain.Ambulancia;
import org.iudigital.emergencias.domain.CasoEmergencia;
import org.iudigital.emergencias.domain.EquipoMedico;
import org.iudigital.emergencias.observer.EmergenciaObserver;
import org.iudigital.emergencias.ui.view.SimulacionView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Observer que actualiza la interfaz JavaFX con eventos de la simulación.
 * Usa Platform.runLater() para asegurar thread-safety.
 */
public class JavaFXObserver implements EmergenciaObserver {

    private final SimulacionView view;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public JavaFXObserver(SimulacionView view) {
        this.view = view;
    }

    @Override
    public void onNuevoCasoRecibido(CasoEmergencia caso, String operadorId, int pendientes) {
        String event = String.format("[%s] 🆕 Nueva emergencia #%d - Severidad: %s - Operador: %s (Cola: %d)",
                LocalDateTime.now().format(timeFormatter),
                caso.getCasoId(),
                caso.getSeveridad(),
                operadorId,
                pendientes);
        view.addEventLog(event);
        view.addCasoVisual(caso);
    }

    @Override
    public void onCasoAsignado(CasoEmergencia caso, Ambulancia ambulancia) {
        String event = String.format("[%s] 🚑 Ambulancia #%d asignada al caso #%d",
                LocalDateTime.now().format(timeFormatter),
                ambulancia.getIdAmbulancia(),
                caso.getCasoId());
        view.addEventLog(event);
    }

    @Override
    public void onEquipoMedicoAsignado(CasoEmergencia caso, EquipoMedico equipo) {
        String event = String.format("[%s] ⚕️ Equipo médico #%d asignado al caso #%d",
                LocalDateTime.now().format(timeFormatter),
                equipo.getIdEquipo(),
                caso.getCasoId());
        view.addEventLog(event);
    }

    @Override
    public void onCasoCompletado(CasoEmergencia caso, long tiempoEsperaMs, long tiempoTotalMs) {
        String event = String.format("[%s] ✅ Caso #%d completado - Espera: %dms | Total: %dms",
                LocalDateTime.now().format(timeFormatter),
                caso.getCasoId(),
                tiempoEsperaMs,
                tiempoTotalMs);
        view.addEventLog(event);
    }

    @Override
    public void onCambioEstadoAmbulancia(Ambulancia ambulancia,
            Ambulancia.StatusAmbulancia estadoAnterior,
            Ambulancia.StatusAmbulancia estadoNuevo) {
        String event = String.format("[%s] 🚨 Ambulancia #%d: %s → %s",
                LocalDateTime.now().format(timeFormatter),
                ambulancia.getIdAmbulancia(),
                estadoAnterior,
                estadoNuevo);
        view.addEventLog(event);
    }

    @Override
    public void onCambioEstadoEquipoMedico(EquipoMedico equipo,
            EquipoMedico.StatusEquipo estadoAnterior,
            EquipoMedico.StatusEquipo estadoNuevo) {
        String event = String.format("[%s] 💊 Equipo médico #%d: %s → %s",
                LocalDateTime.now().format(timeFormatter),
                equipo.getIdEquipo(),
                estadoAnterior,
                estadoNuevo);
        view.addEventLog(event);
    }

    @Override
    public void onRecursoNoDisponible(CasoEmergencia caso, String tipoRecurso) {
        String event = String.format("[%s] ⚠️ No hay %s disponible para caso #%d",
                LocalDateTime.now().format(timeFormatter),
                tipoRecurso,
                caso.getCasoId());
        view.addEventLog(event);
    }
}
