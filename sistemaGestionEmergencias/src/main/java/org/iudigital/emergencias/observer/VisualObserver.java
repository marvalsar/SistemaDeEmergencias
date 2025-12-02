package org.iudigital.emergencias.observer;

import org.iudigital.emergencias.domain.Ambulancia;
import org.iudigital.emergencias.domain.CasoEmergencia;
import org.iudigital.emergencias.domain.EquipoMedico;
import org.iudigital.emergencias.util.AnsiColors;
import org.iudigital.emergencias.util.ConsoleUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observer visual mejorado con gráficos avanzados en consola.
 * Muestra eventos con colores, iconos y animaciones.
 */
public class VisualObserver implements EmergenciaObserver {

    private static final Logger logger = LoggerFactory.getLogger(VisualObserver.class);
    private int eventoCounter = 0;

    @Override
    public void onNuevoCasoRecibido(CasoEmergencia caso, String operadorId, int pendientes) {
        eventoCounter++;
        String severidadColor = AnsiColors.colorBySeverity(caso.getSeveridad().toString(),
                caso.getSeveridad().toString());

        System.out.printf("%s📞 [%s%s%s] %sNuevo caso #%d%s → %s en %s%s%s | Cola: %s%d%s\n",
                AnsiColors.BRIGHT_WHITE,
                AnsiColors.BRIGHT_CYAN, operadorId, AnsiColors.RESET,
                AnsiColors.BOLD, caso.getCasoId(), AnsiColors.RESET,
                severidadColor,
                AnsiColors.BRIGHT_YELLOW, caso.getLugar(), AnsiColors.RESET,
                AnsiColors.BRIGHT_MAGENTA, pendientes, AnsiColors.RESET);

        logger.info("Caso {} recibido por {} - Severidad: {} - Pendientes: {}",
                caso.getCasoId(), operadorId, caso.getSeveridad(), pendientes);
    }

    @Override
    public void onCasoAsignado(CasoEmergencia caso, Ambulancia ambulancia) {
        eventoCounter++;
        String severidadColor = AnsiColors.colorBySeverity(caso.getSeveridad().toString(),
                caso.getSeveridad().toString());

        System.out.printf("%s🚑 [DISPATCH]%s Ambulancia %s%d%s → Caso #%s%d%s (%s) | %s%s%s\n",
                AnsiColors.BRIGHT_GREEN, AnsiColors.RESET,
                AnsiColors.BRIGHT_YELLOW, ambulancia.getIdAmbulancia(), AnsiColors.RESET,
                AnsiColors.BOLD, caso.getCasoId(), AnsiColors.RESET,
                severidadColor,
                AnsiColors.BRIGHT_CYAN, caso.getLugar(), AnsiColors.RESET);

        // Dibujar mini mapa visual
        dibujarRutaAmbulancia(ambulancia.getIdAmbulancia(), caso.getLugar());

        logger.info("Caso {} asignado a ambulancia {}", caso.getCasoId(), ambulancia.getIdAmbulancia());
    }

    @Override
    public void onEquipoMedicoAsignado(CasoEmergencia caso, EquipoMedico equipo) {
        eventoCounter++;
        System.out.printf("%s⚕️  [SOPORTE]%s Equipo Médico %s%d%s → Caso #%s%d%s (Especializado)\n",
                AnsiColors.BRIGHT_MAGENTA, AnsiColors.RESET,
                AnsiColors.BRIGHT_YELLOW, equipo.getIdEquipo(), AnsiColors.RESET,
                AnsiColors.BOLD, caso.getCasoId(), AnsiColors.RESET);

        logger.info("Equipo médico {} asignado a caso {}", equipo.getIdEquipo(), caso.getCasoId());
    }

    @Override
    public void onCasoCompletado(CasoEmergencia caso, long tiempoEsperaMs, long tiempoTotalMs) {
        eventoCounter++;
        double esperaSeg = tiempoEsperaMs / 1000.0;
        double totalSeg = tiempoTotalMs / 1000.0;

        System.out.printf("%s✅ [COMPLETADO]%s Caso #%s%d%s finalizado | " +
                "Espera: %s%.1fs%s | Total: %s%.1fs%s\n",
                AnsiColors.BRIGHT_GREEN + AnsiColors.BOLD, AnsiColors.RESET,
                AnsiColors.BRIGHT_CYAN, caso.getCasoId(), AnsiColors.RESET,
                AnsiColors.BRIGHT_YELLOW, esperaSeg, AnsiColors.RESET,
                AnsiColors.BRIGHT_BLUE, totalSeg, AnsiColors.RESET);

        // Mostrar barra de rendimiento
        if (totalSeg < 10) {
            System.out.println(AnsiColors.BRIGHT_GREEN + "   ⚡ Excelente tiempo de respuesta!" +
                    AnsiColors.RESET);
        } else if (totalSeg < 20) {
            System.out.println(AnsiColors.BRIGHT_YELLOW + "   ⏱  Tiempo de respuesta normal" +
                    AnsiColors.RESET);
        } else {
            System.out.println(AnsiColors.BRIGHT_RED + "   ⚠️  Tiempo de respuesta lento" +
                    AnsiColors.RESET);
        }

        logger.info("Caso {} completado - Espera: {}ms, Total: {}ms",
                caso.getCasoId(), tiempoEsperaMs, tiempoTotalMs);
    }

    @Override
    public void onCambioEstadoAmbulancia(Ambulancia ambulancia,
            Ambulancia.StatusAmbulancia estadoAnterior,
            Ambulancia.StatusAmbulancia estadoNuevo) {
        // Solo mostrar cambios importantes para no saturar
        if (estadoNuevo == Ambulancia.StatusAmbulancia.DISPONIBLE) {
            System.out.printf("%s○ Ambulancia %d%s ahora %sDISPONIBLE%s\n",
                    AnsiColors.BRIGHT_GREEN,
                    ambulancia.getIdAmbulancia(),
                    AnsiColors.RESET,
                    AnsiColors.BRIGHT_GREEN + AnsiColors.BOLD,
                    AnsiColors.RESET);
        } else if (estadoNuevo == Ambulancia.StatusAmbulancia.EN_RUTA) {
            System.out.printf("%s→ Ambulancia %d%s EN RUTA al destino\n",
                    AnsiColors.BRIGHT_YELLOW,
                    ambulancia.getIdAmbulancia(),
                    AnsiColors.RESET);
        }

        logger.debug("Ambulancia {} cambió de {} a {}",
                ambulancia.getIdAmbulancia(), estadoAnterior, estadoNuevo);
    }

    @Override
    public void onCambioEstadoEquipoMedico(EquipoMedico equipo,
            EquipoMedico.StatusEquipo estadoAnterior,
            EquipoMedico.StatusEquipo estadoNuevo) {
        if (estadoNuevo == EquipoMedico.StatusEquipo.DISPONIBLE) {
            System.out.printf("%s○ Equipo Médico %d%s ahora %sDISPONIBLE%s\n",
                    AnsiColors.BRIGHT_GREEN,
                    equipo.getIdEquipo(),
                    AnsiColors.RESET,
                    AnsiColors.BRIGHT_GREEN + AnsiColors.BOLD,
                    AnsiColors.RESET);
        }

        logger.debug("Equipo médico {} cambió de {} a {}",
                equipo.getIdEquipo(), estadoAnterior, estadoNuevo);
    }

    @Override
    public void onRecursoNoDisponible(CasoEmergencia caso, String tipoRecurso) {
        System.out.printf("%s⚠️  [ALERTA]%s No hay %s%s%s disponible para caso #%s%d%s (%s%s%s)\n",
                AnsiColors.BRIGHT_RED + AnsiColors.BOLD, AnsiColors.RESET,
                AnsiColors.BRIGHT_YELLOW, tipoRecurso, AnsiColors.RESET,
                AnsiColors.BRIGHT_CYAN, caso.getCasoId(), AnsiColors.RESET,
                AnsiColors.colorBySeverity(caso.getSeveridad().toString(),
                        caso.getSeveridad().toString()),
                AnsiColors.RESET);

        logger.warn("No hay {} disponible para caso {}", tipoRecurso, caso.getCasoId());
    }

    /**
     * Dibuja una representación visual de la ruta de la ambulancia.
     */
    private void dibujarRutaAmbulancia(int idAmbulancia, String destino) {
        System.out.printf("   %s🏥%s ───→ %s🚑%s ───→ %s%s%s\n",
                AnsiColors.BRIGHT_BLUE, AnsiColors.RESET,
                AnsiColors.BRIGHT_YELLOW, AnsiColors.RESET,
                AnsiColors.BRIGHT_CYAN, destino, AnsiColors.RESET);
    }

    /**
     * Obtiene el contador de eventos procesados.
     */
    public int getEventoCounter() {
        return eventoCounter;
    }
}
