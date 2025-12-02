package org.iudigital.emergencias;

import org.iudigital.emergencias.manager.SimulacionManager;
import org.iudigital.emergencias.observer.VisualObserver;
import org.iudigital.emergencias.observer.EventPublisher;
import org.iudigital.emergencias.util.ConsoleUI;
import org.iudigital.emergencias.util.AnsiColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase principal del Sistema de Gestión de Emergencias Médicas.
 * 
 * Versión 2.0.0 - Con visualización gráfica avanzada en consola:
 * - Singleton: SimulacionManager para gestión centralizada
 * - Factory: RecursoFactory para creación de recursos
 * - Observer: EventPublisher con VisualObserver para eventos gráficos
 * - ExecutorService: Para gestión moderna de threads
 * - Console UI: Gráficos Unicode con colores ANSI
 * 
 * @author IUDigital
 * @version 2.0.0
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // Configuración de la simulación
    private static final int NUM_AMBULANCIAS = 4;
    private static final int NUM_EQUIPOS_MEDICOS = 2;
    private static final int NUM_OPERADORES = 2;
    private static final int DURACION_SEGUNDOS = 30;

    public static void main(String[] args) {
        logger.info("=== Iniciando Sistema de Gestión de Emergencias Médicas v2.0.0 ===");

        try {
            // Mostrar banner animado
            ConsoleUI.mostrarBannerAnimado();

            // Animación de carga
            System.out.println();
            ConsoleUI.mostrarLoading("Inicializando sistema de eventos");

            // Configurar sistema de eventos con observer visual
            EventPublisher eventPublisher = new EventPublisher();
            VisualObserver visualObserver = new VisualObserver();
            eventPublisher.registrarObserver(visualObserver);

            logger.info("Sistema de eventos visuales configurado");

            ConsoleUI.mostrarLoading("Preparando recursos del sistema");

            // Obtener instancia del manager (Singleton Pattern)
            SimulacionManager manager = SimulacionManager.getInstance();
            manager.setEventPublisher(eventPublisher);

            logger.info("SimulacionManager obtenido");

            // Mostrar información de configuración
            mostrarConfiguracion();

            // Inicializar y ejecutar simulación
            System.out.println("\n" + AnsiColors.BRIGHT_CYAN +
                    "═".repeat(80) + AnsiColors.RESET);
            System.out.println(AnsiColors.BRIGHT_WHITE + AnsiColors.BOLD +
                    "           🚀 INICIANDO SIMULACIÓN 🚀" + AnsiColors.RESET);
            System.out.println(AnsiColors.BRIGHT_CYAN +
                    "═".repeat(80) + AnsiColors.RESET + "\n");

            manager.inicializarSimulacion(NUM_AMBULANCIAS, NUM_EQUIPOS_MEDICOS,
                    NUM_OPERADORES, DURACION_SEGUNDOS);

            System.out.println("\n" + AnsiColors.SUCCESS +
                    "✓ Simulación iniciada exitosamente" + AnsiColors.RESET);
            System.out.printf("%s⏳ Duración programada: %d segundos%s\n",
                    AnsiColors.BRIGHT_YELLOW, DURACION_SEGUNDOS, AnsiColors.RESET);
            System.out.println(AnsiColors.DIM +
                    "⏸️  Presiona Ctrl+C para detener antes de tiempo" +
                    AnsiColors.RESET + "\n");

            // Esperar a que finalice la simulación
            esperarFinalizacion(manager);

            // Generar resumen final
            System.out.println("\n" + AnsiColors.BRIGHT_CYAN +
                    "═".repeat(80) + AnsiColors.RESET);
            ConsoleUI.mostrarLoading("Generando resumen final");
            manager.generarResumenFinal();

            // Mostrar estadísticas del observer
            System.out.println("\n" + AnsiColors.SUCCESS +
                    String.format("✓ Total de eventos procesados: %d",
                            visualObserver.getEventoCounter())
                    + AnsiColors.RESET);

            logger.info("=== Sistema de Gestión de Emergencias finalizado exitosamente ===");

        } catch (InterruptedException e) {
            logger.error("Simulación interrumpida", e);
            Thread.currentThread().interrupt();
            System.err.println("\n❌ Simulación interrumpida por el usuario");
        } catch (Exception e) {
            logger.error("Error durante la ejecución de la simulación", e);
            System.err.println("\n❌ Error durante la simulación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra la configuración de la simulación.
     */
    private static void mostrarConfiguracion() {
        System.out.println("\n" + AnsiColors.BRIGHT_WHITE +
                "╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║" + AnsiColors.BOLD +
                "              CONFIGURACIÓN DE LA SIMULACIÓN               " +
                AnsiColors.RESET + AnsiColors.BRIGHT_WHITE + "     ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  %s🚑 Ambulancias:%s        %-38s║\n",
                AnsiColors.BRIGHT_YELLOW, AnsiColors.RESET + AnsiColors.BRIGHT_WHITE,
                NUM_AMBULANCIAS);
        System.out.printf("║  %s⚕️  Equipos Médicos:%s    %-38s║\n",
                AnsiColors.BRIGHT_GREEN, AnsiColors.RESET + AnsiColors.BRIGHT_WHITE,
                NUM_EQUIPOS_MEDICOS);
        System.out.printf("║  %s📞 Operadores:%s        %-38s║\n",
                AnsiColors.BRIGHT_CYAN, AnsiColors.RESET + AnsiColors.BRIGHT_WHITE,
                NUM_OPERADORES);
        System.out.printf("║  %s⏱️  Duración:%s          %-35s║\n",
                AnsiColors.BRIGHT_MAGENTA, AnsiColors.RESET + AnsiColors.BRIGHT_WHITE,
                DURACION_SEGUNDOS + " segundos");
        System.out.println("╚════════════════════════════════════════════════════════════════╝" +
                AnsiColors.RESET);
    }

    /**
     * Muestra el banner de inicio de la aplicación.
     */
    @SuppressWarnings("unused")
    private static void mostrarBanner() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                              ║");
        System.out.println("║      Sistema de Gestión de Emergencias Médicas v2.0.0       ║");
        System.out.println("║                                                              ║");
        System.out.println("║      🚑  Simulación de Coordinación de Recursos  🚑          ║");
        System.out.println("║                                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    /**
     * Espera a que la simulación finalice, verificando periódicamente el estado.
     */
    private static void esperarFinalizacion(SimulacionManager manager) throws InterruptedException {
        while (manager.isSimulacionActiva()) {
            java.util.concurrent.TimeUnit.SECONDS.sleep(1);
        }
    }
}
