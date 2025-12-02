# Patrones de Diseño Aplicados al Sistema de Emergencias

Este documento detalla los patrones de diseño que se deben implementar en el sistema y cómo aplicarlos específicamente a
este caso de uso.

---

## 📐 Patrones Creacionales

### 1. Singleton Pattern

**Aplicación**: `SimulacionManager`, `EventPublisher`, `DatabaseConfig`

**Problema que resuelve**:

- Garantizar una única instancia del gestor de simulación
- Evitar múltiples configuraciones de base de datos
- Punto central de coordinación

**Implementación Thread-Safe (Double-Checked Locking)**:

```java
public class SimulacionManager {
    // volatile asegura visibilidad entre threads
    private static volatile SimulacionManager instance;

    // Constructor privado previene instanciación externa
    private SimulacionManager() {
        if (instance != null) {
            throw new IllegalStateException("Ya existe una instancia");
        }
    }

    public static SimulacionManager getInstance() {
        // Primera verificación sin lock (performance)
        if (instance == null) {
            synchronized (SimulacionManager.class) {
                // Segunda verificación con lock (thread-safety)
                if (instance == null) {
                    instance = new SimulacionManager();
                }
            }
        }
        return instance;
    }

    // Métodos de negocio
    public void iniciarSimulacion(SimulacionConfig config) { /* ... */ }
    public void detenerSimulacion() { /* ... */ }
}
```

**Alternativa moderna (Holder Pattern - Initialization-on-demand)**:

```java
public class SimulacionManager {
    private SimulacionManager() {}

    // Clase interna estática - lazy initialization thread-safe
    private static class Holder {
        private static final SimulacionManager INSTANCE = new SimulacionManager();
    }

    public static SimulacionManager getInstance() {
        return Holder.INSTANCE;
    }
}
```

**Cuándo usar**: Cuando necesitas exactamente una instancia global accesible desde cualquier parte.

---

### 2. Factory Pattern

**Aplicación**: Creación de recursos (ambulancias, equipos médicos, emergencias)

**Problema que resuelve**:

- Centralizar lógica de creación
- Facilitar cambios en el proceso de construcción
- Permitir extensibilidad (nuevos tipos de recursos)

**Implementación**:

```java
// Factory simple
public class RecursoFactory {

    public static Ambulancia crearAmbulancia(int id, TipoAmbulancia tipo) {
        Ambulancia ambulancia = switch (tipo) {
            case BASICA -> new AmbulanciaBasica(id);
            case AVANZADA -> new AmbulanciaAvanzada(id);
            case UCI_MOVIL -> new AmbulanciaUCI(id);
        };

        // Configuración común
        ambulancia.setEstado(EstadoRecurso.DISPONIBLE);
        ambulancia.inicializar();

        return ambulancia;
    }

    public static EquipoMedico crearEquipoMedico(int id, Especialidad especialidad) {
        EquipoMedico equipo = new EquipoMedico(id, especialidad);
        equipo.setEstado(EstadoRecurso.DISPONIBLE);
        return equipo;
    }
}

// Uso
Ambulancia ambulancia = RecursoFactory.crearAmbulancia(101, TipoAmbulancia.AVANZADA);
EquipoMedico equipo = RecursoFactory.crearEquipoMedico(201, Especialidad.TRAUMA);
```

**Factory Abstracto (para múltiples familias)**:

```java
// Abstract Factory - para cuando tengas diferentes "familias" de objetos
public interface RecursoAbstractFactory {
    Ambulancia crearAmbulancia(int id);
    EquipoMedico crearEquipoMedico(int id);
}

public class RecursoUrbanoFactory implements RecursoAbstractFactory {
    @Override
    public Ambulancia crearAmbulancia(int id) {
        return new AmbulanciaUrbana(id);
    }

    @Override
    public EquipoMedico crearEquipoMedico(int id) {
        return new EquipoMedicoUrbano(id);
    }
}

public class RecursoRuralFactory implements RecursoAbstractFactory {
    @Override
    public Ambulancia crearAmbulancia(int id) {
        return new AmbulanciaRural(id); // Con tracción 4x4, más autonomía
    }

    @Override
    public EquipoMedico crearEquipoMedico(int id) {
        return new EquipoMedicoRural(id); // Con equipo para áreas remotas
    }
}
```

---

### 3. Builder Pattern (Opcional - para configuraciones complejas)

**Aplicación**: Construcción de `SimulacionConfig`, `CasoEmergencia` con muchos parámetros

**Problema que resuelve**:

- Evitar constructores con muchos parámetros
- Permitir construcción paso a paso
- Hacer código más legible

**Implementación**:

```java
public class SimulacionConfig {
    private final int numOperadores;
    private final int numAmbulancias;
    private final int numEquiposMedicos;
    private final int duracionSegundos;
    private final boolean usarBaseDatos;
    private final boolean modoGrafico;

    // Constructor privado - solo accesible desde Builder
    private SimulacionConfig(Builder builder) {
        this.numOperadores = builder.numOperadores;
        this.numAmbulancias = builder.numAmbulancias;
        this.numEquiposMedicos = builder.numEquiposMedicos;
        this.duracionSegundos = builder.duracionSegundos;
        this.usarBaseDatos = builder.usarBaseDatos;
        this.modoGrafico = builder.modoGrafico;
    }

    // Getters
    public int getNumOperadores() { return numOperadores; }
    // ... otros getters

    // Builder estático interno
    public static class Builder {
        // Valores por defecto
        private int numOperadores = 2;
        private int numAmbulancias = 3;
        private int numEquiposMedicos = 2;
        private int duracionSegundos = 30;
        private boolean usarBaseDatos = false;
        private boolean modoGrafico = true;

        public Builder numOperadores(int val) {
            this.numOperadores = val;
            return this;
        }

        public Builder numAmbulancias(int val) {
            this.numAmbulancias = val;
            return this;
        }

        public Builder numEquiposMedicos(int val) {
            this.numEquiposMedicos = val;
            return this;
        }

        public Builder duracionSegundos(int val) {
            this.duracionSegundos = val;
            return this;
        }

        public Builder usarBaseDatos(boolean val) {
            this.usarBaseDatos = val;
            return this;
        }

        public Builder modoGrafico(boolean val) {
            this.modoGrafico = val;
            return this;
        }

        public SimulacionConfig build() {
            // Validaciones
            if (numOperadores < 1) {
                throw new IllegalStateException("Debe haber al menos 1 operador");
            }
            if (numAmbulancias < 1) {
                throw new IllegalStateException("Debe haber al menos 1 ambulancia");
            }

            return new SimulacionConfig(this);
        }
    }
}

// Uso - ¡Muy legible!
SimulacionConfig config = new SimulacionConfig.Builder()
    .numOperadores(3)
    .numAmbulancias(5)
    .numEquiposMedicos(3)
    .duracionSegundos(60)
    .usarBaseDatos(true)
    .modoGrafico(true)
    .build();
```

---

## 📊 Patrones Estructurales

### 4. Adapter Pattern

**Aplicación**: Adaptar la interfaz legacy de consola para trabajar con JavaFX

**Problema que resuelve**:

- Integrar código existente con nueva UI
- Mantener compatibilidad hacia atrás

**Implementación**:

```java
// Interfaz que espera JavaFX
public interface LogObserver {
    void onLog(String mensaje);
}

// Clase legacy que escribe en consola
public class ConsoleLogger {
    public void println(String mensaje) {
        System.out.println(mensaje);
    }
}

// Adapter - convierte llamadas de Console a eventos observables
public class ConsoleLoggerAdapter implements LogObserver {
    private final ConsoleLogger consoleLogger;

    public ConsoleLoggerAdapter(ConsoleLogger consoleLogger) {
        this.consoleLogger = consoleLogger;
    }

    @Override
    public void onLog(String mensaje) {
        // Adapta el método esperado al método disponible
        consoleLogger.println("[ADAPTED] " + mensaje);
    }
}

// Uso
LogObserver logger = new ConsoleLoggerAdapter(new ConsoleLogger());
logger.onLog("Nueva emergencia recibida");
```

---

### 5. Facade Pattern

**Aplicación**: Simplificar la interacción con el sistema de simulación

**Problema que resuelve**:

- Ocultar complejidad interna
- Proporcionar interfaz simple para operaciones comunes

**Implementación**:

```java
public class EmergenciasFacade {
    private final SimulacionManager simulacionManager;
    private final RecursoService recursoService;
    private final EmergenciaService emergenciaService;
    private final EventPublisher eventPublisher;

    public EmergenciasFacade() {
        this.simulacionManager = SimulacionManager.getInstance();
        this.recursoService = new RecursoService();
        this.emergenciaService = new EmergenciaService();
        this.eventPublisher = EventPublisher.getInstance();
    }

    // Método facade - oculta toda la complejidad
    public void iniciarSimulacionCompleta(int duracionSegundos) {
        // 1. Crear configuración
        SimulacionConfig config = new SimulacionConfig.Builder()
            .duracionSegundos(duracionSegundos)
            .build();

        // 2. Inicializar recursos
        recursoService.inicializarRecursos(
            config.getNumAmbulancias(),
            config.getNumEquiposMedicos()
        );

        // 3. Iniciar simulación
        simulacionManager.iniciar(config);

        // 4. Configurar monitoreo
        eventPublisher.iniciarMonitoreo();
    }

    public void detenerSimulacion() {
        simulacionManager.detener();
        eventPublisher.detenerMonitoreo();
    }

    public EstadoSimulacion obtenerEstadoActual() {
        return new EstadoSimulacion(
            emergenciaService.contarPendientes(),
            recursoService.contarDisponibles(),
            emergenciaService.contarCompletadas()
        );
    }
}

// Uso desde UI - ¡Mucho más simple!
EmergenciasFacade facade = new EmergenciasFacade();
facade.iniciarSimulacionCompleta(60);
// ... después
EstadoSimulacion estado = facade.obtenerEstadoActual();
```

---

## 🔄 Patrones de Comportamiento

### 6. Observer Pattern (MUY IMPORTANTE para este proyecto)

**Aplicación**: Notificaciones en tiempo real a la UI

**Problema que resuelve**:

- Desacoplar lógica de negocio de la UI
- Permitir múltiples observadores (UI, logger, DB)
- Actualizar UI sin bloquear workers

**Implementación completa**:

```java
// 1. Definir eventos
public interface EmergenciaObserver {
    void onNuevaEmergencia(CasoEmergencia caso);
    void onEmergenciaAsignada(CasoEmergencia caso, Recurso recurso);
    void onEmergenciaEnProceso(CasoEmergencia caso);
    void onEmergenciaCompletada(CasoEmergencia caso);
}

public interface RecursoObserver {
    void onRecursoDisponible(Recurso recurso);
    void onRecursoAsignado(Recurso recurso, CasoEmergencia caso);
    void onRecursoOcupado(Recurso recurso);
    void onRecursoRetornando(Recurso recurso);
}

// 2. Publisher (Subject)
public class EventPublisher {
    private static volatile EventPublisher instance;

    // Thread-safe collections para observadores
    private final List<EmergenciaObserver> emergenciaObservers =
        new CopyOnWriteArrayList<>();
    private final List<RecursoObserver> recursoObservers =
        new CopyOnWriteArrayList<>();

    private EventPublisher() {}

    public static EventPublisher getInstance() {
        if (instance == null) {
            synchronized (EventPublisher.class) {
                if (instance == null) {
                    instance = new EventPublisher();
                }
            }
        }
        return instance;
    }

    // Suscripción
    public void subscribe(EmergenciaObserver observer) {
        emergenciaObservers.add(observer);
    }

    public void subscribe(RecursoObserver observer) {
        recursoObservers.add(observer);
    }

    // Desuscripción
    public void unsubscribe(EmergenciaObserver observer) {
        emergenciaObservers.remove(observer);
    }

    public void unsubscribe(RecursoObserver observer) {
        recursoObservers.remove(observer);
    }

    // Notificaciones - ejecutadas en thread pool para no bloquear
    private final ExecutorService notificationExecutor =
        Executors.newFixedThreadPool(2);

    public void notifyNuevaEmergencia(CasoEmergencia caso) {
        notificationExecutor.submit(() -> {
            for (EmergenciaObserver observer : emergenciaObservers) {
                try {
                    observer.onNuevaEmergencia(caso);
                } catch (Exception e) {
                    // Log error pero continúa notificando otros observers
                    System.err.println("Error notificando observer: " + e.getMessage());
                }
            }
        });
    }

    public void notifyEmergenciaAsignada(CasoEmergencia caso, Recurso recurso) {
        notificationExecutor.submit(() -> {
            for (EmergenciaObserver observer : emergenciaObservers) {
                try {
                    observer.onEmergenciaAsignada(caso, recurso);
                } catch (Exception e) {
                    System.err.println("Error notificando observer: " + e.getMessage());
                }
            }
        });
    }

    // ... más métodos de notificación
}

// 3. Uso en Workers - Publicar eventos
public class OperadorLlamadasWorker implements Runnable {
    private final BlockingQueue<CasoEmergencia> cola;
    private final EventPublisher publisher = EventPublisher.getInstance();

    @Override
    public void run() {
        while (corriendo) {
            CasoEmergencia caso = generarNuevoCaso();
            cola.put(caso);

            // Notificar a todos los observadores
            publisher.notifyNuevaEmergencia(caso);
        }
    }
}

// 4. Uso en UI - Suscribirse a eventos
public class MainViewController implements EmergenciaObserver, RecursoObserver {
    @FXML private ListView<CasoEmergencia> emergenciasListView;
    @FXML private TextArea logsArea;

    @FXML
    public void initialize() {
        EventPublisher publisher = EventPublisher.getInstance();
        publisher.subscribe((EmergenciaObserver) this);
        publisher.subscribe((RecursoObserver) this);
    }

    @Override
    public void onNuevaEmergencia(CasoEmergencia caso) {
        // CRÍTICO: Actualizar UI en el thread de JavaFX
        Platform.runLater(() -> {
            emergenciasListView.getItems().add(caso);
            logsArea.appendText(String.format(
                "[%s] 📞 Nueva emergencia #%d: %s en %s\n",
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                caso.getCasoId(),
                caso.getSeveridad(),
                caso.getLugar()
            ));
        });
    }

    @Override
    public void onEmergenciaAsignada(CasoEmergencia caso, Recurso recurso) {
        Platform.runLater(() -> {
            logsArea.appendText(String.format(
                "[%s] ✅ Emergencia #%d asignada a %s\n",
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                caso.getCasoId(),
                recurso.getIdentificador()
            ));
        });
    }

    // ... otros métodos observer
}
```

---

### 7. Strategy Pattern

**Aplicación**: Diferentes algoritmos de priorización y asignación

**Problema que resuelve**:

- Permitir cambiar algoritmo en runtime
- Facilitar testing de diferentes estrategias
- Cumplir Open/Closed Principle

**Implementación**:

```java
// 1. Interfaz Strategy
public interface PriorizacionStrategy {
    double calcularPrioridad(CasoEmergencia caso);
}

// 2. Implementaciones concretas
public class PriorizacionPorSeveridadStrategy implements PriorizacionStrategy {
    private static final double PESO_GRAVEDAD = 4.0;
    private static final double PESO_TIEMPO = 0.5;

    @Override
    public double calcularPrioridad(CasoEmergencia caso) {
        long tiempoEspera = (System.currentTimeMillis() - caso.getHoraRecibido()) / 1000;
        int valorSeveridad = caso.getSeveridad().getValor();

        return (valorSeveridad * PESO_GRAVEDAD) + (tiempoEspera * PESO_TIEMPO);
    }
}

public class PriorizacionPorProximidadStrategy implements PriorizacionStrategy {
    private final Ubicacion centralHospital;

    public PriorizacionPorProximidadStrategy(Ubicacion centralHospital) {
        this.centralHospital = centralHospital;
    }

    @Override
    public double calcularPrioridad(CasoEmergencia caso) {
        // Priorizar casos más cercanos al hospital
        double distancia = calcularDistancia(caso.getUbicacion(), centralHospital);
        int valorSeveridad = caso.getSeveridad().getValor();

        // A menor distancia, mayor prioridad
        return (valorSeveridad * 10.0) / (distancia + 1.0);
    }

    private double calcularDistancia(Ubicacion a, Ubicacion b) {
        // Fórmula de distancia euclidiana
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}

// 3. Context - usa la estrategia
public class CasoEmergencia implements Comparable<CasoEmergencia> {
    private static PriorizacionStrategy estrategia =
        new PriorizacionPorSeveridadStrategy();

    // Permitir cambiar estrategia globalmente
    public static void setEstrategiaPriorizacion(PriorizacionStrategy nuevaEstrategia) {
        estrategia = nuevaEstrategia;
    }

    @Override
    public int compareTo(CasoEmergencia other) {
        double miPrioridad = estrategia.calcularPrioridad(this);
        double otraPrioridad = estrategia.calcularPrioridad(other);

        return Double.compare(otraPrioridad, miPrioridad); // Orden descendente
    }
}

// 4. Uso - cambiar estrategia dinámicamente
// En la configuración o UI
CasoEmergencia.setEstrategiaPriorizacion(
    new PriorizacionPorProximidadStrategy(hospitalCentral)
);

// O crear un selector
public class PriorizacionSelector {
    public static PriorizacionStrategy obtenerEstrategia(String tipo) {
        return switch (tipo.toUpperCase()) {
            case "SEVERIDAD" -> new PriorizacionPorSeveridadStrategy();
            case "PROXIMIDAD" -> new PriorizacionPorProximidadStrategy(
                new Ubicacion(0, 0)
            );
            case "HIBRIDA" -> new PriorizacionHibridaStrategy();
            default -> throw new IllegalArgumentException("Estrategia desconocida: " + tipo);
        };
    }
}
```

**Strategy para Asignación de Recursos**:

```java
public interface AsignacionStrategy {
    Optional<Ambulancia> seleccionarAmbulancia(
        List<Ambulancia> disponibles,
        CasoEmergencia caso
    );
}

public class AsignacionMasCercanaStrategy implements AsignacionStrategy {
    @Override
    public Optional<Ambulancia> seleccionarAmbulancia(
        List<Ambulancia> disponibles,
        CasoEmergencia caso
    ) {
        return disponibles.stream()
            .min(Comparator.comparingDouble(
                ambulancia -> calcularDistancia(ambulancia.getUbicacion(), caso.getUbicacion())
            ));
    }
}

public class AsignacionRoundRobinStrategy implements AsignacionStrategy {
    private int ultimoIndice = 0;

    @Override
    public synchronized Optional<Ambulancia> seleccionarAmbulancia(
        List<Ambulancia> disponibles,
        CasoEmergencia caso
    ) {
        if (disponibles.isEmpty()) {
            return Optional.empty();
        }

        Ambulancia seleccionada = disponibles.get(ultimoIndice % disponibles.size());
        ultimoIndice++;

        return Optional.of(seleccionada);
    }
}
```

---

### 8. Template Method Pattern

**Aplicación**: Workflow común de atención de emergencias

**Problema que resuelve**:

- Define esqueleto de algoritmo
- Subclases personalizan pasos específicos
- Evita duplicación de código

**Implementación**:

```java
public abstract class RecursoEmergencia implements Runnable {
    protected volatile boolean corriendo = true;
    protected CasoEmergencia casoActual;

    // Template Method - define el flujo completo
    @Override
    public final void run() {
        try {
            inicializar();

            while (corriendo && !Thread.currentThread().isInterrupted()) {
                if (tieneAsignacion()) {
                    ejecutarCicloAtencion();
                } else {
                    esperarAsignacion();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            limpiar();
        }
    }

    // Pasos del template - algunos con implementación por defecto
    protected void inicializar() {
        System.out.println(getIdentificador() + " inicializado y listo");
    }

    protected abstract boolean tieneAsignacion();

    // Template Method interno para el ciclo de atención
    private void ejecutarCicloAtencion() throws InterruptedException {
        desplazarseAlLugar();
        atenderEmergencia();
        retornarABase();
        finalizarCaso();
    }

    // Pasos que las subclases DEBEN implementar
    protected abstract void desplazarseAlLugar() throws InterruptedException;
    protected abstract void atenderEmergencia() throws InterruptedException;
    protected abstract void retornarABase() throws InterruptedException;

    protected void finalizarCaso() {
        casoActual = null;
        System.out.println(getIdentificador() + " finaliza caso y queda disponible");
    }

    protected void esperarAsignacion() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }

    protected void limpiar() {
        System.out.println(getIdentificador() + " detenido y limpio");
    }

    protected abstract String getIdentificador();

    public void stop() {
        corriendo = false;
    }
}

// Implementación concreta - Ambulancia
public class Ambulancia extends RecursoEmergencia {
    private final int id;
    private EstadoRecurso estado = EstadoRecurso.DISPONIBLE;

    public Ambulancia(int id) {
        this.id = id;
    }

    @Override
    protected boolean tieneAsignacion() {
        return casoActual != null;
    }

    @Override
    protected void desplazarseAlLugar() throws InterruptedException {
        estado = EstadoRecurso.EN_RUTA;
        System.out.println("Ambulancia " + id + " en ruta a " + casoActual.getLugar());
        TimeUnit.SECONDS.sleep(3); // Simula desplazamiento
    }

    @Override
    protected void atenderEmergencia() throws InterruptedException {
        estado = EstadoRecurso.OCUPADA;
        System.out.println("Ambulancia " + id + " atendiendo caso #" + casoActual.getCasoId());
        TimeUnit.SECONDS.sleep(5 + (int)(Math.random() * 5));
    }

    @Override
    protected void retornarABase() throws InterruptedException {
        estado = EstadoRecurso.RETORNANDO;
        System.out.println("Ambulancia " + id + " retornando a base");
        TimeUnit.SECONDS.sleep(3);
        estado = EstadoRecurso.DISPONIBLE;
    }

    @Override
    protected String getIdentificador() {
        return "Ambulancia-" + id;
    }
}

// Otra implementación - EquipoMedico (mismo template, diferentes detalles)
public class EquipoMedico extends RecursoEmergencia {
    private final int id;
    private final Especialidad especialidad;

    // ... implementaciones similares pero con tiempos y lógica específica
}
```

---

### 9. State Pattern (Avanzado - Opcional)

**Aplicación**: Gestión de estados de recursos

**Problema que resuelve**:

- Simplificar lógica de transiciones de estado
- Cada estado maneja su propia lógica
- Facilita agregar nuevos estados

**Implementación básica**:

```java
// 1. Interfaz de estado
public interface EstadoRecurso {
    void asignarCaso(Recurso recurso, CasoEmergencia caso);
    void comenzarDesplazamiento(Recurso recurso);
    void iniciarAtencion(Recurso recurso);
    void completarAtencion(Recurso recurso);
    String getNombre();
}

// 2. Estados concretos
public class EstadoDisponible implements EstadoRecurso {
    @Override
    public void asignarCaso(Recurso recurso, CasoEmergencia caso) {
        recurso.setCasoActual(caso);
        recurso.setEstado(new EstadoEnRuta());
        recurso.notificarCambioEstado();
    }

    @Override
    public void comenzarDesplazamiento(Recurso recurso) {
        throw new IllegalStateException("No se puede desplazar sin caso asignado");
    }

    @Override
    public String getNombre() {
        return "DISPONIBLE";
    }

    // ... otros métodos lanzan excepciones (transiciones no válidas)
}

public class EstadoEnRuta implements EstadoRecurso {
    @Override
    public void iniciarAtencion(Recurso recurso) {
        recurso.setEstado(new EstadoAtendiendo());
        recurso.notificarCambioEstado();
    }

    @Override
    public void asignarCaso(Recurso recurso, CasoEmergencia caso) {
        throw new IllegalStateException("Ya tiene un caso asignado");
    }

    @Override
    public String getNombre() {
        return "EN_RUTA";
    }
}

// 3. Context - Recurso que usa el estado
public abstract class Recurso {
    private EstadoRecurso estadoActual = new EstadoDisponible();
    private CasoEmergencia casoActual;

    // Delegación al estado actual
    public void asignarCaso(CasoEmergencia caso) {
        estadoActual.asignarCaso(this, caso);
    }

    public void comenzarDesplazamiento() {
        estadoActual.comenzarDesplazamiento(this);
    }

    // ... otros métodos delegados

    // Métodos internos para que los estados modifiquen el contexto
    void setEstado(EstadoRecurso nuevoEstado) {
        this.estadoActual = nuevoEstado;
    }

    void setCasoActual(CasoEmergencia caso) {
        this.casoActual = caso;
    }

    public String getEstadoNombre() {
        return estadoActual.getNombre();
    }
}
```

---

## 📦 Object Pool Pattern (Específico para Concurrencia)

**Aplicación**: Pool de recursos reutilizables (ambulancias, equipos médicos)

**Problema que resuelve**:

- Gestión eficiente de recursos limitados
- Reutilización en lugar de creación/destrucción constante
- Thread-safe access

**Implementación**:

```java
public class RecursoPool<T extends Recurso> {
    private final BlockingQueue<T> disponibles;
    private final Set<T> ocupados;
    private final Lock lock = new ReentrantLock();
    private final int capacidadMaxima;

    public RecursoPool(int capacidad) {
        this.capacidadMaxima = capacidad;
        this.disponibles = new ArrayBlockingQueue<>(capacidad);
        this.ocupados = new HashSet<>(capacidad);
    }

    public void agregar(T recurso) {
        if (disponibles.size() + ocupados.size() >= capacidadMaxima) {
            throw new IllegalStateException("Pool lleno");
        }
        disponibles.offer(recurso);
    }

    // Obtener recurso (bloqueante)
    public T obtener() throws InterruptedException {
        T recurso = disponibles.take(); // Bloquea si no hay disponibles

        lock.lock();
        try {
            ocupados.add(recurso);
        } finally {
            lock.unlock();
        }

        return recurso;
    }

    // Obtener recurso con timeout
    public Optional<T> obtener(long timeout, TimeUnit unit) throws InterruptedException {
        T recurso = disponibles.poll(timeout, unit);

        if (recurso != null) {
            lock.lock();
            try {
                ocupados.add(recurso);
            } finally {
                lock.unlock();
            }
        }

        return Optional.ofNullable(recurso);
    }

    // Liberar recurso
    public void liberar(T recurso) {
        lock.lock();
        try {
            if (ocupados.remove(recurso)) {
                disponibles.offer(recurso);
            }
        } finally {
            lock.unlock();
        }
    }

    // Estadísticas
    public int disponiblesCount() {
        return disponibles.size();
    }

    public int ocupadosCount() {
        lock.lock();
        try {
            return ocupados.size();
        } finally {
            lock.unlock();
        }
    }
}

// Uso
RecursoPool<Ambulancia> ambulanciaPool = new RecursoPool<>(5);

// Agregar ambulancias al pool
for (int i = 1; i <= 5; i++) {
    ambulanciaPool.agregar(new Ambulancia(i));
}

// En el despachador
Ambulancia ambulancia = ambulanciaPool.obtener(); // Bloquea si no hay
ambulancia.asignarCaso(caso);

// ... después de completar
ambulanciaPool.liberar(ambulancia);
```

---

## 🎯 Resumen: Cuándo Usar Cada Patrón

| Patrón              | Usar Cuando                                  | No Usar Cuando                           |
| ------------------- | -------------------------------------------- | ---------------------------------------- |
| **Singleton**       | Necesitas exactamente 1 instancia global     | Necesitas múltiples instancias o testing |
| **Factory**         | Lógica de creación compleja o variable       | Construcción trivial                     |
| **Builder**         | Muchos parámetros opcionales                 | Pocos parámetros obligatorios            |
| **Observer**        | Múltiples objetos deben reaccionar a eventos | Relación 1-1 simple                      |
| **Strategy**        | Múltiples algoritmos intercambiables         | Un solo algoritmo fijo                   |
| **Template Method** | Workflow común con pasos variables           | Workflows completamente diferentes       |
| **State**           | Comportamiento varía según estado interno    | Lógica de estado simple                  |
| **Facade**          | Sistema complejo necesita interfaz simple    | Sistema ya es simple                     |
| **Object Pool**     | Recursos limitados y costosos de crear       | Objetos baratos o ilimitados             |

---

## ✅ Checklist de Implementación

- [ ] Implementar Singleton para `SimulacionManager`
- [ ] Implementar Factory para crear recursos
- [ ] Implementar Observer para eventos UI
- [ ] Implementar Strategy para priorización
- [ ] Implementar Template Method para workflow de recursos
- [ ] Implementar Object Pool para gestión de recursos
- [ ] Documentar cada patrón con JavaDoc
- [ ] Crear tests unitarios para cada patrón
- [ ] Crear diagrama UML de patrones implementados

---

**Nota**: No implementes todos los patrones a la vez. Prioriza:

1. Observer (crítico para UI)
2. Singleton (crítico para coordinación)
3. Factory (facilita extensibilidad)
4. Strategy (mejora flexibilidad)
5. Los demás según necesidad
