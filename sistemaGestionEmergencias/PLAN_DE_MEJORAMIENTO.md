# Plan de Mejoramiento - Sistema de Gestión de Emergencias Médicas

## 📋 Análisis de la Situación Actual

### Fortalezas Identificadas

- ✅ Uso correcto de `BlockingQueue` para comunicación entre hilos
- ✅ Implementación de `Comparable` en `CasoEmergencia` para priorización
- ✅ Uso de `volatile` para flags de control de hilos
- ✅ Separación básica de responsabilidades con diferentes clases de componentes
- ✅ Sistema funcional de concurrencia básica

### Áreas de Mejora Críticas

#### 1. **Arquitectura y Patrones de Diseño**

❌ **Problemas actuales:**

- Clase `Main` con lógica de negocio (viola Single Responsibility Principle)
- No hay separación clara entre capas (presentación, negocio, datos)
- Falta de abstracción y uso de interfaces donde sería apropiado
- Acoplamiento fuerte entre componentes
- No hay uso de patrones de diseño empresariales

❌ **Código problemático:**

```java
// En Main.java - Demasiada responsabilidad
public static void main(String[] args) throws InterruptedException {
    List<Stoppable> todosLosComponentes = new ArrayList<>();
    BlockingQueue<CasoEmergencia> casoEmergencias = new PriorityBlockingQueue<>();
    // ... mucha lógica de inicialización
}
```

#### 2. **Manejo de Concurrencia**

❌ **Problemas actuales:**

- No se usa `ExecutorService` (mejor práctica que crear threads manualmente)
- Manejo inadecuado de interrupciones en algunos casos
- No hay shutdown ordenado con `awaitTermination`
- Falta de coordinación entre hilos con `CountDownLatch` o `CyclicBarrier`

❌ **Código problemático:**

```java
// Creación manual de threads
new Thread(ambulancia, "Ambulancia-" + i).start();
```

#### 3. **Sincronización y Race Conditions**

⚠️ **Problemas potenciales:**

- En `MonitorTiempoReal`: condición `while (!corriendo)` debe ser `while (corriendo)`
- Métodos sincronizados podrían mejorarse con locks específicos
- Falta de thread-safety en algunas operaciones

❌ **Bug crítico en MonitorTiempoReal.java:**

```java
while (!corriendo) {  // ❌ ESTO ESTÁ AL REVÉS
    // ... nunca ejecutará porque corriendo=true
}
```

#### 4. **Gestión de Recursos**

❌ **Problemas actuales:**

- No hay pool de recursos (ObjectPool pattern)
- No hay mecanismos de reciclaje eficientes
- Falta de métricas y monitoreo avanzado

#### 5. **Estructura de Paquetes**

❌ **Estructura actual confusa:**

```
concurrencias/          ← Mal nombre, mezcla conceptos
    Despachador.java
    MonitorTiempoReal.java
    OperadorLlamadas.java
org/iudigital/emergencias/  ← Todo mezclado
    Ambulancia.java
    CasoEmergencia.java
    ...
```

---

## 🎯 Plan de Mejoramiento Propuesto

### FASE 1: Refactorización de Arquitectura y Patrones de Diseño

#### 1.1 Nueva Estructura de Paquetes

```
org.iudigital.emergencias/
├── domain/                     # Modelos de dominio
│   ├── CasoEmergencia.java
│   ├── Ambulancia.java
│   ├── EquipoMedico.java
│   ├── Ubicacion.java
│   └── enums/
│       ├── Severidad.java
│       ├── EstadoRecurso.java
│       └── TipoRecurso.java
│
├── service/                    # Lógica de negocio
│   ├── EmergenciaService.java
│   ├── RecursoService.java
│   ├── DespachoService.java
│   └── impl/
│       ├── EmergenciaServiceImpl.java
│       └── RecursoServiceImpl.java
│
├── repository/                 # Acceso a datos (preparado para DB)
│   ├── EmergenciaRepository.java
│   ├── RecursoRepository.java
│   └── impl/
│       ├── EmergenciaRepositoryImpl.java
│       └── RecursoRepositoryImpl.java
│
├── worker/                     # Hilos trabajadores
│   ├── OperadorLlamadasWorker.java
│   ├── DespachadorWorker.java
│   ├── AmbulanciaWorker.java
│   ├── EquipoMedicoWorker.java
│   └── MonitorWorker.java
│
├── manager/                    # Coordinadores de alto nivel
│   ├── EmergenciaManager.java
│   ├── RecursoManager.java
│   └── SimulacionManager.java
│
├── factory/                    # Factory Pattern
│   ├── RecursoFactory.java
│   ├── EmergenciaFactory.java
│   └── WorkerFactory.java
│
├── observer/                   # Observer Pattern
│   ├── EmergenciaObserver.java
│   ├── RecursoObserver.java
│   └── EventPublisher.java
│
├── strategy/                   # Strategy Pattern
│   ├── PriorizacionStrategy.java
│   ├── AsignacionStrategy.java
│   └── impl/
│       ├── PriorizacionPorSeveridadStrategy.java
│       └── AsignacionPorProximidadStrategy.java
│
├── config/                     # Configuración
│   ├── SimulacionConfig.java
│   └── DatabaseConfig.java
│
├── util/                       # Utilidades
│   ├── ThreadPoolManager.java
│   └── EstadisticasCalculator.java
│
└── ui/                         # Interfaz JavaFX
    ├── EmergenciasApp.java     # Aplicación principal
    ├── controller/
    │   ├── MainViewController.java
    │   ├── MonitorController.java
    │   └── ConfiguracionController.java
    ├── view/
    │   └── *.fxml
    └── component/
        ├── EmergenciaCard.java
        └── RecursoPanel.java
```

#### 1.2 Patrones de Diseño a Implementar

##### **1.2.1 Singleton Pattern** - SimulacionManager

```java
public class SimulacionManager {
    private static volatile SimulacionManager instance;
    private ExecutorService executorService;
    private final Object lock = new Object();

    private SimulacionManager() {}

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
}
```

##### **1.2.2 Factory Pattern** - Para crear recursos

```java
public class RecursoFactory {
    public static Ambulancia crearAmbulancia(int id, TipoAmbulancia tipo) {
        return new Ambulancia(id, tipo);
    }

    public static EquipoMedico crearEquipoMedico(int id, Especialidad especialidad) {
        return new EquipoMedico(id, especialidad);
    }
}
```

##### **1.2.3 Observer Pattern** - Para notificaciones

```java
public interface EmergenciaObserver {
    void onNuevaEmergencia(CasoEmergencia caso);
    void onEmergenciaAsignada(CasoEmergencia caso);
    void onEmergenciaCompletada(CasoEmergencia caso);
}

public class EventPublisher {
    private final List<EmergenciaObserver> observers = new CopyOnWriteArrayList<>();

    public void subscribe(EmergenciaObserver observer) {
        observers.add(observer);
    }

    public void notifyNuevaEmergencia(CasoEmergencia caso) {
        observers.forEach(o -> o.onNuevaEmergencia(caso));
    }
}
```

##### **1.2.4 Strategy Pattern** - Para priorización

```java
public interface PriorizacionStrategy {
    double calcularPrioridad(CasoEmergencia caso);
}

public class PriorizacionPorSeveridadStrategy implements PriorizacionStrategy {
    @Override
    public double calcularPrioridad(CasoEmergencia caso) {
        // Implementación específica
    }
}
```

##### **1.2.5 Object Pool Pattern** - Para recursos

```java
public class RecursoPool<T extends Recurso> {
    private final BlockingQueue<T> disponibles;
    private final Set<T> ocupados;
    private final Lock lock = new ReentrantLock();

    public T obtener() throws InterruptedException {
        T recurso = disponibles.take();
        lock.lock();
        try {
            ocupados.add(recurso);
        } finally {
            lock.unlock();
        }
        return recurso;
    }

    public void liberar(T recurso) {
        lock.lock();
        try {
            ocupados.remove(recurso);
            disponibles.offer(recurso);
        } finally {
            lock.unlock();
        }
    }
}
```

---

### FASE 2: Mejoras en Concurrencia

#### 2.1 Uso de ExecutorService

```java
public class SimulacionManager {
    private ExecutorService mainExecutor;
    private ExecutorService workerExecutor;
    private ScheduledExecutorService monitorExecutor;

    public void iniciar(SimulacionConfig config) {
        // Pool para operadores y despachador
        mainExecutor = Executors.newFixedThreadPool(
            config.getNumOperadores() + 1,
            new ThreadFactoryBuilder()
                .setNameFormat("main-worker-%d")
                .build()
        );

        // Pool para ambulancias y equipos médicos
        workerExecutor = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                .setNameFormat("recurso-worker-%d")
                .build()
        );

        // Pool para monitor (programado)
        monitorExecutor = Executors.newScheduledThreadPool(1);
        monitorExecutor.scheduleAtFixedRate(
            new MonitorWorker(),
            0, 2, TimeUnit.SECONDS
        );
    }

    public void detener() {
        shutdownExecutor(mainExecutor, "Main");
        shutdownExecutor(workerExecutor, "Worker");
        shutdownExecutor(monitorExecutor, "Monitor");
    }

    private void shutdownExecutor(ExecutorService executor, String nombre) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

#### 2.2 Coordinación con CountDownLatch

```java
public class SimulacionManager {
    private CountDownLatch inicioPuerta;
    private CountDownLatch finPuerta;

    public void ejecutarSimulacion(int duracionSegundos) {
        int totalWorkers = numOperadores + numAmbulancias + numEquiposMedicos + 2;
        inicioPuerta = new CountDownLatch(totalWorkers);
        finPuerta = new CountDownLatch(totalWorkers);

        // Iniciar todos los workers
        // ...

        // Esperar que todos estén listos
        inicioPuerta.await();
        logger.info("Todos los componentes iniciados. Comenzando simulación...");

        // Esperar duración
        TimeUnit.SECONDS.sleep(duracionSegundos);

        // Señal de parada
        detener();

        // Esperar que todos terminen
        finPuerta.await(10, TimeUnit.SECONDS);
        logger.info("Simulación completada.");
    }
}
```

#### 2.3 Mejora en Manejo de Interrupciones

```java
public class AmbulanciaWorker implements Runnable {
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && corriendo) {
                // Lógica...
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Ambulancia {} interrumpida correctamente", id);
        } finally {
            // Limpieza
            limpiarRecursos();
        }
    }
}
```

---

### FASE 3: Interfaz Gráfica con JavaFX

#### 3.1 Estructura de UI

##### MainView.fxml - Vista Principal

```
┌─────────────────────────────────────────────────────────┐
│  Sistema de Gestión de Emergencias Médicas              │
├───────────────────┬─────────────────────────────────────┤
│                   │  PANEL DE CONTROL                    │
│  EMERGENCIAS      │  ┌─────────────────────────────────┐│
│  PENDIENTES       │  │ ▶ Iniciar Simulación           ││
│                   │  │ ⏸ Pausar                        ││
│  [CRITICO]        │  │ ⏹ Detener                       ││
│  #1001 - Lugar-12 │  │                                  ││
│  Tiempo: 00:23    │  │ Operadores:  [2] ▲▼             ││
│  ──────────       │  │ Ambulancias: [3] ▲▼             ││
│  [GRAVE]          │  │ Equipos Med: [2] ▲▼             ││
│  #1002 - Lugar-45 │  │                                  ││
│  Tiempo: 00:15    │  │ Duración: [30s] ▲▼              ││
│  ──────────       │  └─────────────────────────────────┘│
│  [MODERADO]       │                                      │
│  #1003 - Lugar-8  │  ESTADÍSTICAS EN TIEMPO REAL        │
│  ...              │  ┌─────────────────────────────────┐│
│                   │  │ Casos Totales:      15          ││
│                   │  │ Completados:         8          ││
│                   │  │ En Proceso:          4          ││
│                   │  │ Pendientes:          3          ││
│                   │  │                                  ││
│                   │  │ Tiempo Prom Espera:  45s        ││
│                   │  │ Tiempo Prom Total:   3m 20s     ││
├───────────────────┤  └─────────────────────────────────┘│
│  RECURSOS         │                                      │
│  DISPONIBLES      │  MAPA DE RECURSOS                    │
│                   │  ┌─────────────────────────────────┐│
│  🚑 Ambulancia-1  │  │                                  ││
│     DISPONIBLE    │  │    🏥 Hospital Central           ││
│                   │  │                                  ││
│  🚑 Ambulancia-2  │  │  🚑#1 ──→ 📍 (Caso #1001)       ││
│     EN_RUTA       │  │                                  ││
│     → Caso #1001  │  │        🚑#2 (Disponible)        ││
│                   │  │                                  ││
│  🚑 Ambulancia-3  │  │  📍 Emergencia #1003             ││
│     OCUPADA       │  │                                  ││
│     → Caso #1005  │  │           🚑#3 ──→ 🏥           ││
│                   │  │                                  ││
│  ⚕️ Equipo-1      │  └─────────────────────────────────┘│
│     DISPONIBLE    │                                      │
│                   │                                      │
│  ⚕️ Equipo-2      │                                      │
│     ASIGNADO      │                                      │
│     → Caso #1001  │                                      │
└───────────────────┴─────────────────────────────────────┘
│  📊 LOGS:  Se recibió llamada #1001 - CRITICO          │
│            Despachador asigna Ambulancia-2 a caso #1001│
└─────────────────────────────────────────────────────────┘
```

#### 3.2 Componentes JavaFX

##### EmergenciasApp.java - Aplicación Principal

```java
public class EmergenciasApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/MainView.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(
            getClass().getResource("/css/styles.css").toExternalForm()
        );

        primaryStage.setTitle("Sistema de Gestión de Emergencias Médicas");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

##### MainViewController.java - Controlador

```java
public class MainViewController implements EmergenciaObserver, RecursoObserver {
    @FXML private ListView<CasoEmergencia> emergenciasListView;
    @FXML private ListView<Recurso> recursosListView;
    @FXML private Label casosCompletadosLabel;
    @FXML private Label casosEnProcesoLabel;
    @FXML private TextArea logsTextArea;
    @FXML private Button iniciarBtn;
    @FXML private Button detenerBtn;

    private SimulacionManager simulacionManager;
    private EventPublisher eventPublisher;

    @FXML
    public void initialize() {
        simulacionManager = SimulacionManager.getInstance();
        eventPublisher = EventPublisher.getInstance();
        eventPublisher.subscribe(this);

        configurarListViews();
    }

    @FXML
    private void onIniciarSimulacion() {
        SimulacionConfig config = obtenerConfiguracion();

        Task<Void> simulacionTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                simulacionManager.ejecutarSimulacion(config);
                return null;
            }
        };

        new Thread(simulacionTask).start();

        iniciarBtn.setDisable(true);
        detenerBtn.setDisable(false);
    }

    @Override
    public void onNuevaEmergencia(CasoEmergencia caso) {
        Platform.runLater(() -> {
            emergenciasListView.getItems().add(caso);
            agregarLog("📞 Nueva emergencia: " + caso);
        });
    }

    @Override
    public void onEmergenciaCompletada(CasoEmergencia caso) {
        Platform.runLater(() -> {
            emergenciasListView.getItems().remove(caso);
            actualizarEstadisticas();
            agregarLog("✅ Emergencia completada: " + caso.getCasoId());
        });
    }

    private void agregarLog(String mensaje) {
        String timestamp = LocalTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss")
        );
        logsTextArea.appendText(
            String.format("[%s] %s\n", timestamp, mensaje)
        );
    }
}
```

#### 3.3 Visualización Avanzada

##### Gráficos con JavaFX Charts

```java
public class EstadisticasChartController {
    @FXML private LineChart<String, Number> tiempoEsperaChart;
    @FXML private PieChart severidadDistribucionChart;
    @FXML private BarChart<String, Number> recursosUtilizacionChart;

    public void actualizarGraficos(List<CasoEmergencia> casos) {
        actualizarLineChart(casos);
        actualizarPieChart(casos);
        actualizarBarChart();
    }

    private void actualizarPieChart(List<CasoEmergencia> casos) {
        Map<Severidad, Long> distribucion = casos.stream()
            .collect(Collectors.groupingBy(
                CasoEmergencia::getSeveridad,
                Collectors.counting()
            ));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        distribucion.forEach((severidad, count) -> {
            pieData.add(new PieChart.Data(
                severidad.name(), count
            ));
        });

        severidadDistribucionChart.setData(pieData);
    }
}
```

---

### FASE 4: Base de Datos

#### 4.1 Configuración de H2 (Database embebida)

##### DatabaseConfig.java

```java
public class DatabaseConfig {
    private static final String JDBC_URL = "jdbc:h2:./data/emergencias;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static HikariDataSource dataSource;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(JDBC_URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000);

            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    public static void inicializarEsquema() throws SQLException {
        try (Connection conn = getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            // Crear tablas
            stmt.execute(SQL_CREATE_TABLES);
        }
    }

    private static final String SQL_CREATE_TABLES = """
        CREATE TABLE IF NOT EXISTS emergencias (
            id BIGINT PRIMARY KEY,
            severidad VARCHAR(20) NOT NULL,
            lugar VARCHAR(255) NOT NULL,
            hora_recibido TIMESTAMP NOT NULL,
            hora_inicio_servicio TIMESTAMP,
            hora_atendido TIMESTAMP,
            recurso_asignado_id INT,
            estado VARCHAR(20) NOT NULL
        );

        CREATE TABLE IF NOT EXISTS recursos (
            id INT PRIMARY KEY,
            tipo VARCHAR(20) NOT NULL,
            estado VARCHAR(20) NOT NULL,
            ubicacion_actual VARCHAR(255)
        );

        CREATE TABLE IF NOT EXISTS simulaciones (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            fecha_inicio TIMESTAMP NOT NULL,
            fecha_fin TIMESTAMP,
            duracion_segundos INT,
            num_casos_totales INT,
            num_casos_completados INT,
            tiempo_promedio_espera DECIMAL(10,2),
            tiempo_promedio_servicio DECIMAL(10,2)
        );

        CREATE INDEX idx_emergencias_severidad ON emergencias(severidad);
        CREATE INDEX idx_emergencias_estado ON emergencias(estado);
        CREATE INDEX idx_recursos_estado ON recursos(estado);
    """;
}
```

#### 4.2 Capa de Repositorio

##### EmergenciaRepository.java

```java
public interface EmergenciaRepository {
    void guardar(CasoEmergencia caso);
    void actualizar(CasoEmergencia caso);
    Optional<CasoEmergencia> buscarPorId(long id);
    List<CasoEmergencia> buscarPorEstado(String estado);
    List<CasoEmergencia> buscarTodos();
}

public class EmergenciaRepositoryImpl implements EmergenciaRepository {
    private final DataSource dataSource;

    public EmergenciaRepositoryImpl() {
        this.dataSource = DatabaseConfig.getDataSource();
    }

    @Override
    public void guardar(CasoEmergencia caso) {
        String sql = """
            INSERT INTO emergencias
            (id, severidad, lugar, hora_recibido, estado)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, caso.getCasoId());
            pstmt.setString(2, caso.getSeveridad().name());
            pstmt.setString(3, caso.getLugar());
            pstmt.setTimestamp(4, new Timestamp(caso.getHoraRecibido()));
            pstmt.setString(5, "PENDIENTE");

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar emergencia", e);
        }
    }

    @Override
    public void actualizar(CasoEmergencia caso) {
        String sql = """
            UPDATE emergencias SET
                hora_inicio_servicio = ?,
                hora_atendido = ?,
                recurso_asignado_id = ?,
                estado = ?
            WHERE id = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1,
                caso.getHoraInicioServicio() > 0
                    ? new Timestamp(caso.getHoraInicioServicio())
                    : null
            );
            pstmt.setTimestamp(2,
                caso.getHoraAtendido() > 0
                    ? new Timestamp(caso.getHoraAtendido())
                    : null
            );
            pstmt.setObject(3,
                caso.getRecursoAsignado() != null
                    ? caso.getRecursoAsignado().getIdAmbulancia()
                    : null
            );
            pstmt.setString(4, determinarEstado(caso));
            pstmt.setLong(5, caso.getCasoId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar emergencia", e);
        }
    }
}
```

---

### FASE 5: Testing

#### 5.1 Tests Unitarios

##### CasoEmergenciaTest.java

```java
@ExtendWith(MockitoExtension.class)
class CasoEmergenciaTest {

    @Test
    void deberiaCompararCorrectamentePorPrioridad() {
        CasoEmergencia critico = new CasoEmergencia(Severidad.CRITICO, "Lugar-1");
        CasoEmergencia leve = new CasoEmergencia(Severidad.LEVE, "Lugar-2");

        // El caso crítico debe tener mayor prioridad (compareTo < 0)
        assertTrue(critico.compareTo(leve) < 0);
    }

    @Test
    void deberiaCalcularTiempoEsperaCorrectamente() throws InterruptedException {
        CasoEmergencia caso = new CasoEmergencia(Severidad.MODERADO, "Lugar-3");

        Thread.sleep(100);

        long tiempoEspera = System.currentTimeMillis() - caso.getHoraRecibido();

        assertTrue(tiempoEspera >= 100);
    }
}
```

##### DespachadorTest.java

```java
@ExtendWith(MockitoExtension.class)
class DespachadorTest {

    @Mock
    private BlockingQueue<CasoEmergencia> cola;

    @Mock
    private RecursoService recursoService;

    @InjectMocks
    private DespachadorWorker despachador;

    @Test
    void deberiaAsignarAmbulanciaDisponible() throws InterruptedException {
        CasoEmergencia caso = new CasoEmergencia(Severidad.GRAVE, "Lugar-5");
        Ambulancia ambulancia = new Ambulancia(101);

        when(cola.take()).thenReturn(caso);
        when(recursoService.obtenerAmbulanciaDisponible())
            .thenReturn(Optional.of(ambulancia));

        // Ejecutar
        // ... (lógica de test)

        verify(recursoService).asignarRecurso(ambulancia, caso);
    }
}
```

#### 5.2 Tests de Concurrencia

##### ConcurrenciaTest.java

```java
class ConcurrenciaTest {

    @Test
    void deberiaManejarMultiplesOperadoresSimultaneamente() throws InterruptedException {
        BlockingQueue<CasoEmergencia> cola = new PriorityBlockingQueue<>();
        CountDownLatch latch = new CountDownLatch(10);

        // Crear 10 operadores concurrentes
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    CasoEmergencia caso = new CasoEmergencia(
                        Severidad.MODERADO, "Lugar-" + Math.random()
                    );
                    cola.put(caso);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        assertEquals(10, cola.size());

        executor.shutdown();
    }

    @Test
    void noDeberiaHaberRaceConditionsEnAsignacion() throws InterruptedException {
        List<Ambulancia> ambulancias = Arrays.asList(
            new Ambulancia(1),
            new Ambulancia(2)
        );

        RecursoPool<Ambulancia> pool = new RecursoPool<>(ambulancias);

        // Intentar obtener recursos concurrentemente
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<Ambulancia>> futures = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            futures.add(executor.submit(() -> pool.obtener()));
        }

        // Solo 2 deben tener éxito (las 2 ambulancias disponibles)
        int exitosos = 0;
        for (Future<Ambulancia> future : futures) {
            try {
                Ambulancia a = future.get(1, TimeUnit.SECONDS);
                if (a != null) exitosos++;
            } catch (TimeoutException e) {
                // Expected para algunos
            }
        }

        assertEquals(2, exitosos);
        executor.shutdown();
    }
}
```

---

### FASE 6: Configuración y Logging

#### 6.1 Archivo de Propiedades

##### application.properties

```properties
# Configuración de Simulación
simulacion.duracion.segundos=30
simulacion.num.operadores=2
simulacion.num.ambulancias=3
simulacion.num.equipos.medicos=2

# Configuración de Generación de Emergencias
emergencia.probabilidad.critico=0.2
emergencia.probabilidad.grave=0.3
emergencia.probabilidad.moderado=0.3
emergencia.probabilidad.leve=0.2

# Configuración de Tiempos (en segundos)
ambulancia.tiempo.ruta=3
ambulancia.tiempo.atencion.min=5
ambulancia.tiempo.atencion.max=10
ambulancia.tiempo.retorno=3

equipo.medico.tiempo.ruta=2
equipo.medico.tiempo.atencion.min=4
equipo.medico.tiempo.atencion.max=8

# Configuración de Base de Datos
db.url=jdbc:h2:./data/emergencias
db.username=sa
db.password=
db.pool.size=10

# Configuración de Logging
logging.level.root=INFO
logging.level.org.iudigital.emergencias=DEBUG
```

#### 6.2 Logback Configuration

##### logback.xml

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/emergencias.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/emergencias-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="org.iudigital.emergencias" level="DEBUG"/>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

---

### FASE 7: Mejoras en pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.iudigital</groupId>
    <artifactId>sistema-gestion-emergencias</artifactId>
    <version>2.0.0</version>
    <packaging>jar</packaging>

    <name>Sistema de Gestión de Emergencias Médicas</name>
    <description>Sistema concurrente para gestión de emergencias médicas con JavaFX</description>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <!-- Versiones de dependencias -->
        <javafx.version>21.0.1</javafx.version>
        <junit.version>5.10.1</junit.version>
        <mockito.version>5.8.0</mockito.version>
        <slf4j.version>2.0.9</slf4j.version>
        <logback.version>1.4.14</logback.version>
        <h2.version>2.2.224</h2.version>
        <hikari.version>5.1.0</hikari.version>
    </properties>

    <dependencies>
        <!-- JavaFX -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-graphics</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>${logback.version}</version>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>${h2.version}</version>
        </dependency>
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>${hikari.version}</version>
        </dependency>

        <!-- Utilidades -->
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>33.0.0-jre</version>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>${mockito.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <version>${mockito.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.awaitility</groupId>
            <artifactId>awaitility</artifactId>
            <version>4.2.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Compiler Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>

            <!-- JavaFX Plugin -->
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>org.iudigital.emergencias.ui.EmergenciasApp</mainClass>
                </configuration>
            </plugin>

            <!-- Surefire Plugin para tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.3</version>
            </plugin>

            <!-- Assembly Plugin para JAR ejecutable -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.6.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>org.iudigital.emergencias.ui.EmergenciasApp</mainClass>
                        </manifest>
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 📊 Resumen de Mejoras

### Antes vs Después

| Aspecto               | Antes ❌                 | Después ✅                                             |
| --------------------- | ------------------------ | ------------------------------------------------------ |
| **Arquitectura**      | Monolítica, todo en Main | Capas separadas (domain, service, repository, UI)      |
| **Patrones**          | Ninguno                  | Singleton, Factory, Observer, Strategy, Object Pool    |
| **Concurrencia**      | Threads manuales         | ExecutorService, thread pools configurables            |
| **Sincronización**    | synchronized básico      | ReentrantLock, CountDownLatch, thread-safe collections |
| **UI**                | Console logging          | JavaFX con gráficos en tiempo real                     |
| **Persistencia**      | Ninguna                  | H2 database con HikariCP                               |
| **Testing**           | Ninguno                  | JUnit 5 + Mockito + tests de concurrencia              |
| **Logging**           | System.out               | SLF4J + Logback con archivos rotados                   |
| **Configuración**     | Hardcoded                | application.properties externalizable                  |
| **Manejo de errores** | Básico                   | Try-catch estructurado con logging                     |

---

## 🎯 Próximos Pasos (Implementación)

1. **Semana 1-2**: Refactorización de arquitectura y patrones

   - Reorganizar paquetes
   - Implementar patrones de diseño
   - Mejorar manejo de concurrencia

2. **Semana 3**: Desarrollo de UI JavaFX

   - Crear vistas FXML
   - Implementar controladores
   - Agregar gráficos y visualizaciones

3. **Semana 4**: Base de datos y persistencia

   - Configurar H2
   - Implementar repositorios
   - Agregar funcionalidad de guardado/carga

4. **Semana 5**: Testing y documentación
   - Escribir tests unitarios
   - Tests de integración y concurrencia
   - Documentar código y crear README

---

## 📚 Recursos Recomendados

- **JavaFX**: https://openjfx.io/
- **Java Concurrency**: "Java Concurrency in Practice" - Brian Goetz
- **Design Patterns**: "Head First Design Patterns"
- **H2 Database**: https://www.h2database.com/
- **Maven**: https://maven.apache.org/guides/

---

## ✅ Checklist de Implementación

- [ ] Actualizar pom.xml con todas las dependencias
- [ ] Reorganizar estructura de paquetes
- [ ] Implementar patrones de diseño (Factory, Observer, Strategy)
- [ ] Refactorizar con ExecutorService
- [ ] Crear componentes JavaFX
- [ ] Implementar configuración de base de datos
- [ ] Crear repositorios y DAOs
- [ ] Escribir tests unitarios
- [ ] Implementar logging estructurado
- [ ] Crear documentación técnica
- [ ] Preparar video demostrativo

---

**Nota**: Este es un plan exhaustivo. Se puede implementar de forma incremental, priorizando las mejoras más críticas
primero (arquitectura y concurrencia) y luego avanzando a UI y database.
