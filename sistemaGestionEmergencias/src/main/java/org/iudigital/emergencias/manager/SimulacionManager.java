package org.iudigital.emergencias.manager;

import org.iudigital.emergencias.domain.Ambulancia;
import org.iudigital.emergencias.domain.CasoEmergencia;
import org.iudigital.emergencias.domain.EquipoMedico;
import org.iudigital.emergencias.observer.EventPublisher;
import org.iudigital.emergencias.worker.Despachador;
import org.iudigital.emergencias.worker.MonitorTiempoReal;
import org.iudigital.emergencias.worker.MonitorVisual;
import org.iudigital.emergencias.worker.OperadorLlamadas;
import org.iudigital.emergencias.worker.Stoppable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Singleton Manager para controlar la simulación del sistema de emergencias.
 * Implementa patrón Singleton thread-safe con double-checked locking.
 * Gestiona el ciclo de vida de todos los recursos y workers.
 */
public class SimulacionManager {

    private static final Logger logger = LoggerFactory.getLogger(SimulacionManager.class);

    // Singleton instance con volatile para visibilidad entre threads
    private static volatile SimulacionManager instance;

    // Configuración de la simulación
    private static final int DEFAULT_NUM_AMBULANCIAS = 3;
    private static final int DEFAULT_NUM_EQUIPOS_MEDICOS = 2;
    private static final int DEFAULT_NUM_OPERADORES = 2;
    private static final int DEFAULT_DURACION_SEGUNDOS = 30;

    // Recursos del sistema
    private final BlockingQueue<CasoEmergencia> colaCasosEmergencia;
    private final List<Ambulancia> ambulancias;
    private final List<EquipoMedico> equiposMedicos;
    private final List<CasoEmergencia> casosCompletados;

    // Workers y executor service
    private final List<Stoppable> todosLosComponentes;
    private ExecutorService executorService;
    private EventPublisher eventPublisher;

    // Estado de la simulación
    private volatile boolean simulacionActiva = false;
    private ScheduledExecutorService shutdownExecutor;

    /**
     * Constructor privado para patrón Singleton.
     * Inicializa todas las estructuras de datos.
     */
    private SimulacionManager() {
        this.colaCasosEmergencia = new PriorityBlockingQueue<>();
        this.ambulancias = Collections.synchronizedList(new ArrayList<>());
        this.equiposMedicos = Collections.synchronizedList(new ArrayList<>());
        this.casosCompletados = Collections.synchronizedList(new ArrayList<>());
        this.todosLosComponentes = Collections.synchronizedList(new ArrayList<>());

        logger.info("SimulacionManager inicializado");
    }

    /**
     * Obtiene la instancia única del SimulacionManager.
     * Implementa double-checked locking para thread-safety y rendimiento.
     * 
     * @return la instancia única de SimulacionManager
     */
    public static SimulacionManager getInstance() {
        if (instance == null) {
            synchronized (SimulacionManager.class) {
                if (instance == null) {
                    instance = new SimulacionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Inicializa la simulación con valores por defecto.
     */
    public void inicializarSimulacion() {
        inicializarSimulacion(DEFAULT_NUM_AMBULANCIAS, DEFAULT_NUM_EQUIPOS_MEDICOS,
                DEFAULT_NUM_OPERADORES, DEFAULT_DURACION_SEGUNDOS);
    }

    /**
     * Establece el EventPublisher para el sistema de eventos.
     * 
     * @param eventPublisher el publisher de eventos
     */
    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        logger.debug("EventPublisher configurado");
    }

    /**
     * Inicializa la simulación con parámetros personalizados.
     * 
     * @param numAmbulancias    número de ambulancias
     * @param numEquiposMedicos número de equipos médicos
     * @param numOperadores     número de operadores
     * @param duracionSegundos  duración de la simulación en segundos
     */
    public void inicializarSimulacion(int numAmbulancias, int numEquiposMedicos,
            int numOperadores, int duracionSegundos) {
        if (simulacionActiva) {
            logger.warn("La simulación ya está activa");
            return;
        }

        logger.info("Inicializando simulación: {} ambulancias, {} equipos médicos, {} operadores",
                numAmbulancias, numEquiposMedicos, numOperadores);

        // Crear thread pool (+3 para despachador, monitor tiempo real y monitor visual)
        int totalThreads = numAmbulancias + numEquiposMedicos + numOperadores + 3;
        executorService = Executors.newFixedThreadPool(totalThreads);

        // Inicializar recursos
        inicializarAmbulancias(numAmbulancias);
        inicializarEquiposMedicos(numEquiposMedicos);
        inicializarOperadores(numOperadores);
        inicializarDespachador();
        inicializarMonitor();
        inicializarMonitorVisual();

        // Programar apagado automático
        shutdownExecutor = Executors.newSingleThreadScheduledExecutor();
        shutdownExecutor.schedule(() -> {
            logger.info("Tiempo de simulación finalizado ({} segundos)", duracionSegundos);
            detenerSimulacion();
        }, duracionSegundos, TimeUnit.SECONDS);

        simulacionActiva = true;
        logger.info("Simulación iniciada exitosamente");
    }

    /**
     * Inicializa las ambulancias y las agrega al pool de recursos.
     */
    private void inicializarAmbulancias(int cantidad) {
        for (int i = 1; i <= cantidad; i++) {
            Ambulancia ambulancia = new Ambulancia(100 + i, casosCompletados);
            ambulancias.add(ambulancia);
            executorService.submit(ambulancia);
            todosLosComponentes.add(ambulancia);
        }
        logger.info("{} ambulancias lanzadas y disponibles", cantidad);
        System.out.println("🚑 " + cantidad + " Ambulancias lanzadas y disponibles.");
    }

    /**
     * Inicializa los equipos médicos especializados.
     */
    private void inicializarEquiposMedicos(int cantidad) {
        for (int i = 1; i <= cantidad; i++) {
            EquipoMedico equipo = new EquipoMedico(200 + i);
            equiposMedicos.add(equipo);
            executorService.submit(equipo);
            todosLosComponentes.add(equipo);
        }
        logger.info("{} equipos médicos especializados en línea", cantidad);
        System.out.println("⚕️ " + cantidad + " Equipos Médicos especializados en línea.");
    }

    /**
     * Inicializa los operadores de llamadas.
     */
    private void inicializarOperadores(int cantidad) {
        for (int i = 1; i <= cantidad; i++) {
            OperadorLlamadas operador = new OperadorLlamadas(colaCasosEmergencia, "OP-" + i);
            executorService.submit(operador);
            todosLosComponentes.add(operador);
        }
        logger.info("{} operadores listos para recibir llamadas", cantidad);
        System.out.println("📞 " + cantidad + " Operadores listos para recibir llamadas.");
    }

    /**
     * Inicializa el despachador de recursos.
     */
    private void inicializarDespachador() {
        Despachador despachador = new Despachador(colaCasosEmergencia, ambulancias, equiposMedicos);
        executorService.submit(despachador);
        todosLosComponentes.add(despachador);
        logger.info("Despachador en línea para coordinar recursos");
        System.out.println("🚨 1 Despachador en línea para coordinar recursos.");
    }

    /**
     * Inicializa el monitor de tiempo real.
     */
    private void inicializarMonitor() {
        MonitorTiempoReal monitor = new MonitorTiempoReal(ambulancias, equiposMedicos, casosCompletados);
        executorService.submit(monitor);
        todosLosComponentes.add(monitor);
        logger.info("Monitor de tiempo real iniciado");
        System.out.println("📊 Monitor de Tiempo Real iniciado.");
    }

    /**
     * Inicializa el monitor visual.
     */
    private void inicializarMonitorVisual() {
        MonitorVisual monitorVisual = new MonitorVisual(ambulancias, equiposMedicos,
                casosCompletados, colaCasosEmergencia, 8);
        executorService.submit(monitorVisual);
        todosLosComponentes.add(monitorVisual);
        logger.info("Monitor visual iniciado");
        System.out.println("🎨 Monitor Visual activado.");
    }

    /**
     * Detiene la simulación de forma ordenada.
     */
    public void detenerSimulacion() {
        if (!simulacionActiva) {
            logger.warn("La simulación no está activa");
            return;
        }

        logger.info("Deteniendo simulación...");
        System.out.println("\n🛑 TIEMPO FINALIZADO. Deteniendo todos los componentes.");

        // Detener todos los componentes
        for (Stoppable componente : todosLosComponentes) {
            componente.stop();
        }

        // Shutdown del executor service de forma limpia
        executorService.shutdown();
        try {
            // Esperar hasta 10 segundos para terminación ordenada
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("Algunos threads no terminaron a tiempo, forzando cierre...");
                executorService.shutdownNow();
                // Dar tiempo adicional para cierre forzado
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.error("No se pudo detener todos los threads");
                }
            } else {
                logger.info("Todos los threads terminaron correctamente");
            }
        } catch (InterruptedException e) {
            logger.warn("Interrupción durante cierre, finalizando inmediatamente");
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Shutdown del scheduled executor
        if (shutdownExecutor != null) {
            shutdownExecutor.shutdown();
        }

        simulacionActiva = false;
        logger.info("Simulación detenida exitosamente");
        System.out.println("✅ Todos los componentes han sido detenidos.");
    }

    /**
     * Genera un resumen final de la simulación.
     */
    public void generarResumenFinal() {
        int casosEnCola = colaCasosEmergencia.size();
        int casosAsignados = 0;

        for (Ambulancia ambulancia : ambulancias) {
            if (ambulancia.getStatusAmbulancia() != Ambulancia.StatusAmbulancia.DISPONIBLE) {
                casosAsignados++;
            }
        }

        int casosEnProceso = casosEnCola + casosAsignados;

        System.out.println(
                "\n\n==========================================================================================");
        System.out.println("                         TABLA DE RESULTADOS DE LA SIMULACIÓN");
        System.out
                .println("==========================================================================================");

        System.out.printf("Total de Casos Atendidos: %d\n", casosCompletados.size());
        System.out.printf("Total de Casos Pendientes (en cola): %d\n", casosEnCola);
        System.out.printf("Total de Casos Asignados (en ruta/ocupados): %d\n", casosAsignados);
        System.out.printf("Total de Casos EN PROCESO (Pendientes + Asignados): %d\n\n", casosEnProceso);

        if (casosCompletados.isEmpty()) {
            System.out.println("No se lograron atender casos en el tiempo de simulación.");
            return;
        }

        System.out.printf("| %-5s | %-10s | %-12s | %-12s | %-12s |\n",
                "ID", "Severidad", "Tiempo Espera", "Tiempo Servicio", "Total (ms)");
        System.out.println("|-------|------------|--------------|---------------|-------------|");

        long totalEspera = 0;
        long totalServicio = 0;

        synchronized (casosCompletados) {
            for (CasoEmergencia caso : casosCompletados) {
                long espera = caso.getTiempoEsperaMs();
                long total = caso.getTiempoTotalServicioMs();
                long servicio = total - espera;

                System.out.printf("| %-5d | %-10s | %-12d | %-12d | %-12d |\n",
                        caso.getCasoId(),
                        caso.getSeveridad().toString(),
                        espera,
                        servicio,
                        total);

                totalEspera += espera;
                totalServicio += total;
            }
        }

        System.out.println("|-------|------------|--------------|---------------|-------------|");

        long avgEspera = totalEspera / casosCompletados.size();
        long avgTotal = totalServicio / casosCompletados.size();
        long avgServicio = avgTotal - avgEspera;

        System.out.printf("| %-5s | %-10s | %-12d | %-12d | %-12d |\n",
                "AVG", "---", avgEspera, avgServicio, avgTotal);
        System.out
                .println("==========================================================================================");

        logger.info("Resumen final generado: {} casos atendidos, {} en cola, {} asignados",
                casosCompletados.size(), casosEnCola, casosAsignados);
    }

    // Getters para acceso a recursos (útil para UI y tests)

    public List<Ambulancia> getAmbulancias() {
        return Collections.unmodifiableList(ambulancias);
    }

    public List<EquipoMedico> getEquiposMedicos() {
        return Collections.unmodifiableList(equiposMedicos);
    }

    public List<CasoEmergencia> getCasosCompletados() {
        return Collections.unmodifiableList(casosCompletados);
    }

    public BlockingQueue<CasoEmergencia> getColaCasosEmergencia() {
        return colaCasosEmergencia;
    }

    public boolean isSimulacionActiva() {
        return simulacionActiva;
    }

    /**
     * Reinicia el manager (útil para tests).
     * ADVERTENCIA: Solo usar en entornos de testing.
     */
    public void reset() {
        if (simulacionActiva) {
            detenerSimulacion();
        }

        colaCasosEmergencia.clear();
        ambulancias.clear();
        equiposMedicos.clear();
        casosCompletados.clear();
        todosLosComponentes.clear();

        logger.info("SimulacionManager reseteado");
    }
}
