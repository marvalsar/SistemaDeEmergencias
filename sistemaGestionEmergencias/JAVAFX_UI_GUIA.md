# Implementación de UI con JavaFX - Guía Práctica

Este documento te guiará en la creación de una interfaz gráfica moderna y funcional para el sistema de gestión de
emergencias.

---

## 🎨 Arquitectura de la UI

```
ui/
├── EmergenciasApp.java              # Aplicación principal JavaFX
├── controller/
│   ├── MainViewController.java       # Controlador de vista principal
│   ├── MonitorController.java        # Controlador de monitor
│   └── ConfiguracionController.java  # Controlador de configuración
├── view/
│   ├── MainView.fxml                 # Vista principal
│   ├── Monitor.fxml                  # Panel de monitoreo
│   └── Configuracion.fxml            # Panel de configuración
├── component/
│   ├── EmergenciaCard.java          # Componente custom para mostrar emergencias
│   └── RecursoIndicador.java        # Indicador de estado de recursos
└── util/
    └── UIUtils.java                  # Utilidades para UI
```

---

## 📱 Paso 1: Crear Aplicación Principal

### EmergenciasApp.java

```java
package org.iudigital.emergencias.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.iudigital.emergencias.manager.SimulacionManager;

public class EmergenciasApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Cargar vista principal
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/MainView.fxml")
        );
        Parent root = loader.load();

        // Crear escena
        Scene scene = new Scene(root, 1400, 900);

        // Cargar CSS
        scene.getStylesheets().add(
            getClass().getResource("/css/styles.css").toExternalForm()
        );

        // Configurar stage
        primaryStage.setTitle("Sistema de Gestión de Emergencias Médicas");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);

        // Icono (opcional)
        try {
            primaryStage.getIcons().add(
                new Image(getClass().getResourceAsStream("/images/ambulance-icon.png"))
            );
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono");
        }

        // Manejar cierre
        primaryStage.setOnCloseRequest(event -> {
            handleClose();
        });

        primaryStage.show();
    }

    private void handleClose() {
        // Detener simulación si está corriendo
        SimulacionManager manager = SimulacionManager.getInstance();
        if (manager.estaEnEjecucion()) {
            manager.detener();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## 🖼️ Paso 2: Crear Vista Principal con Scene Builder o FXML

### MainView.fxml

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.text.Font?>

<BorderPane xmlns="http://javafx.com/javafx/17"
            xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="org.iudigital.emergencias.ui.controller.MainViewController"
            stylesheets="@../css/styles.css">

    <!-- HEADER -->
    <top>
        <VBox styleClass="header">
            <HBox alignment="CENTER" spacing="10">
                <Label text="🚑" style="-fx-font-size: 36px;"/>
                <Label text="Sistema de Gestión de Emergencias Médicas" styleClass="title">
                    <font>
                        <Font name="System Bold" size="24"/>
                    </font>
                </Label>
            </HBox>

            <!-- Panel de Control -->
            <HBox alignment="CENTER" spacing="20" styleClass="control-panel">
                <padding>
                    <Insets top="15" right="20" bottom="15" left="20"/>
                </padding>

                <Button fx:id="iniciarBtn" text="▶ Iniciar Simulación"
                        onAction="#onIniciarSimulacion" styleClass="btn-success"/>

                <Button fx:id="pausarBtn" text="⏸ Pausar"
                        onAction="#onPausarSimulacion" styleClass="btn-warning" disable="true"/>

                <Button fx:id="detenerBtn" text="⏹ Detener"
                        onAction="#onDetenerSimulacion" styleClass="btn-danger" disable="true"/>

                <Separator orientation="VERTICAL"/>

                <Label text="Operadores:"/>
                <Spinner fx:id="operadoresSpinner" min="1" max="10" initialValue="2"
                         prefWidth="80" editable="true"/>

                <Label text="Ambulancias:"/>
                <Spinner fx:id="ambulanciasSpinner" min="1" max="20" initialValue="3"
                         prefWidth="80" editable="true"/>

                <Label text="Equipos Médicos:"/>
                <Spinner fx:id="equiposSpinner" min="1" max="10" initialValue="2"
                         prefWidth="80" editable="true"/>

                <Separator orientation="VERTICAL"/>

                <Label text="Duración:"/>
                <Spinner fx:id="duracionSpinner" min="10" max="300" initialValue="30"
                         prefWidth="80" editable="true"/>
                <Label text="segundos"/>
            </HBox>
        </VBox>
    </top>

    <!-- MAIN CONTENT -->
    <center>
        <SplitPane dividerPositions="0.25, 0.75">

            <!-- LEFT PANEL - EMERGENCIAS -->
            <VBox styleClass="panel">
                <padding>
                    <Insets top="10" right="10" bottom="10" left="10"/>
                </padding>

                <Label text="📋 Emergencias Pendientes" styleClass="panel-title">
                    <font>
                        <Font size="18" name="System Bold"/>
                    </font>
                </Label>

                <Label fx:id="emergenciasPendientesLabel" text="0 en cola"
                       styleClass="subtitle"/>

                <ListView fx:id="emergenciasListView" VBox.vgrow="ALWAYS"
                          styleClass="emergencias-list"/>

                <HBox spacing="10" alignment="CENTER" styleClass="filter-box">
                    <padding>
                        <Insets top="10"/>
                    </padding>
                    <Label text="Filtrar:"/>
                    <ToggleButton text="Todos" selected="true"/>
                    <ToggleButton text="Crítico"/>
                    <ToggleButton text="Grave"/>
                    <ToggleButton text="Moderado"/>
                </HBox>
            </VBox>

            <!-- CENTER PANEL - ESTADÍSTICAS Y MONITOR -->
            <VBox styleClass="panel">
                <padding>
                    <Insets top="10" right="10" bottom="10" left="10"/>
                </padding>

                <!-- Estadísticas -->
                <GridPane hgap="20" vgap="15" styleClass="stats-grid">
                    <padding>
                        <Insets top="10" bottom="20"/>
                    </padding>

                    <!-- Fila 1 -->
                    <VBox styleClass="stat-card" GridPane.columnIndex="0" GridPane.rowIndex="0">
                        <Label text="Casos Totales" styleClass="stat-label"/>
                        <Label fx:id="casosTotalesLabel" text="0" styleClass="stat-value"/>
                    </VBox>

                    <VBox styleClass="stat-card" GridPane.columnIndex="1" GridPane.rowIndex="0">
                        <Label text="Completados" styleClass="stat-label"/>
                        <Label fx:id="casosCompletadosLabel" text="0" styleClass="stat-value-success"/>
                    </VBox>

                    <VBox styleClass="stat-card" GridPane.columnIndex="2" GridPane.rowIndex="0">
                        <Label text="En Proceso" styleClass="stat-label"/>
                        <Label fx:id="casosEnProcesoLabel" text="0" styleClass="stat-value-warning"/>
                    </VBox>

                    <VBox styleClass="stat-card" GridPane.columnIndex="3" GridPane.rowIndex="0">
                        <Label text="Pendientes" styleClass="stat-label"/>
                        <Label fx:id="casosPendientesLabel" text="0" styleClass="stat-value-danger"/>
                    </VBox>

                    <!-- Fila 2 -->
                    <VBox styleClass="stat-card" GridPane.columnIndex="0" GridPane.columnSpan="2" GridPane.rowIndex="1">
                        <Label text="Tiempo Promedio de Espera" styleClass="stat-label"/>
                        <Label fx:id="tiempoPromedioEsperaLabel" text="0s" styleClass="stat-value-info"/>
                    </VBox>

                    <VBox styleClass="stat-card" GridPane.columnIndex="2" GridPane.columnSpan="2" GridPane.rowIndex="1">
                        <Label text="Tiempo Promedio Total" styleClass="stat-label"/>
                        <Label fx:id="tiempoPromedioTotalLabel" text="0s" styleClass="stat-value-info"/>
                    </VBox>
                </GridPane>

                <!-- Gráficos -->
                <TabPane VBox.vgrow="ALWAYS" tabClosingPolicy="UNAVAILABLE">
                    <Tab text="📊 Distribución por Severidad">
                        <PieChart fx:id="severidadPieChart"/>
                    </Tab>

                    <Tab text="📈 Tiempos de Espera">
                        <LineChart fx:id="tiemposLineChart">
                            <xAxis>
                                <CategoryAxis label="Tiempo"/>
                            </xAxis>
                            <yAxis>
                                <NumberAxis label="Segundos"/>
                            </yAxis>
                        </LineChart>
                    </Tab>

                    <Tab text="📊 Utilización de Recursos">
                        <BarChart fx:id="recursosBarChart">
                            <xAxis>
                                <CategoryAxis label="Recurso"/>
                            </xAxis>
                            <yAxis>
                                <NumberAxis label="Porcentaje"/>
                            </yAxis>
                        </BarChart>
                    </Tab>
                </TabPane>
            </VBox>

            <!-- RIGHT PANEL - RECURSOS -->
            <VBox styleClass="panel">
                <padding>
                    <Insets top="10" right="10" bottom="10" left="10"/>
                </padding>

                <Label text="🚑 Recursos Disponibles" styleClass="panel-title">
                    <font>
                        <Font size="18" name="System Bold"/>
                    </font>
                </Label>

                <Label fx:id="recursosDisponiblesLabel" text="0 disponibles / 0 total"
                       styleClass="subtitle"/>

                <ListView fx:id="recursosListView" VBox.vgrow="ALWAYS"
                          styleClass="recursos-list"/>
            </VBox>

        </SplitPane>
    </center>

    <!-- BOTTOM - LOGS -->
    <bottom>
        <VBox styleClass="logs-panel">
            <padding>
                <Insets top="10" right="10" bottom="10" left="10"/>
            </padding>

            <HBox alignment="CENTER_LEFT" spacing="10">
                <Label text="📝 Registro de Eventos" styleClass="panel-title">
                    <font>
                        <Font size="16" name="System Bold"/>
                    </font>
                </Label>

                <Region HBox.hgrow="ALWAYS"/>

                <Button text="Limpiar" onAction="#onLimpiarLogs" styleClass="btn-secondary"/>
            </HBox>

            <TextArea fx:id="logsTextArea" editable="false" wrapText="true"
                      prefHeight="150" styleClass="logs-area"/>
        </VBox>
    </bottom>

</BorderPane>
```

---

## 🎮 Paso 3: Crear Controlador

### MainViewController.java

```java
package org.iudigital.emergencias.ui.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.concurrent.Task;
import org.iudigital.emergencias.domain.CasoEmergencia;
import org.iudigital.emergencias.domain.Recurso;
import org.iudigital.emergencias.manager.SimulacionManager;
import org.iudigital.emergencias.observer.EmergenciaObserver;
import org.iudigital.emergencias.observer.RecursoObserver;
import org.iudigital.emergencias.observer.EventPublisher;
import org.iudigital.emergencias.config.SimulacionConfig;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MainViewController implements EmergenciaObserver, RecursoObserver {

    // === CONTROLES DE UI ===
    @FXML private Button iniciarBtn;
    @FXML private Button pausarBtn;
    @FXML private Button detenerBtn;

    @FXML private Spinner<Integer> operadoresSpinner;
    @FXML private Spinner<Integer> ambulanciasSpinner;
    @FXML private Spinner<Integer> equiposSpinner;
    @FXML private Spinner<Integer> duracionSpinner;

    @FXML private ListView<CasoEmergencia> emergenciasListView;
    @FXML private ListView<Recurso> recursosListView;

    @FXML private Label emergenciasPendientesLabel;
    @FXML private Label recursosDisponiblesLabel;
    @FXML private Label casosTotalesLabel;
    @FXML private Label casosCompletadosLabel;
    @FXML private Label casosEnProcesoLabel;
    @FXML private Label casosPendientesLabel;
    @FXML private Label tiempoPromedioEsperaLabel;
    @FXML private Label tiempoPromedioTotalLabel;

    @FXML private TextArea logsTextArea;

    @FXML private PieChart severidadPieChart;
    @FXML private LineChart<String, Number> tiemposLineChart;
    @FXML private BarChart<String, Number> recursosBarChart;

    // === DATOS ===
    private SimulacionManager simulacionManager;
    private EventPublisher eventPublisher;

    private ObservableList<CasoEmergencia> emergencias;
    private ObservableList<Recurso> recursos;

    private int casosTotales = 0;
    private int casosCompletados = 0;

    // === INICIALIZACIÓN ===
    @FXML
    public void initialize() {
        simulacionManager = SimulacionManager.getInstance();
        eventPublisher = EventPublisher.getInstance();

        // Suscribirse a eventos
        eventPublisher.subscribe((EmergenciaObserver) this);
        eventPublisher.subscribe((RecursoObserver) this);

        // Inicializar listas
        emergencias = FXCollections.observableArrayList();
        recursos = FXCollections.observableArrayList();

        emergenciasListView.setItems(emergencias);
        recursosListView.setItems(recursos);

        // Configurar spinners
        configurarSpinners();

        // Configurar cell factories para mejor visualización
        configurarCellFactories();

        // Configurar gráficos
        configurarGraficos();

        agregarLog("✅ Sistema iniciado y listo");
    }

    private void configurarSpinners() {
        operadoresSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2)
        );
        ambulanciasSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 3)
        );
        equiposSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2)
        );
        duracionSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 300, 30)
        );
    }

    private void configurarCellFactories() {
        // Custom cell para emergencias
        emergenciasListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(CasoEmergencia caso, boolean empty) {
                super.updateItem(caso, empty);

                if (empty || caso == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(String.format(
                        "[%s] #%d - %s\nTiempo: %ds",
                        obtenerEmojiSeveridad(caso.getSeveridad()),
                        caso.getCasoId(),
                        caso.getLugar(),
                        (System.currentTimeMillis() - caso.getHoraRecibido()) / 1000
                    ));

                    // Estilo según severidad
                    String styleClass = switch (caso.getSeveridad()) {
                        case CRITICO -> "emergencia-critica";
                        case GRAVE -> "emergencia-grave";
                        case MODERADO -> "emergencia-moderada";
                        case LEVE -> "emergencia-leve";
                    };
                    setStyle("-fx-background-color: " + obtenerColorSeveridad(caso.getSeveridad()) + ";");
                }
            }
        });

        // Custom cell para recursos
        recursosListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Recurso recurso, boolean empty) {
                super.updateItem(recurso, empty);

                if (empty || recurso == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String emoji = recurso instanceof Ambulancia ? "🚑" : "⚕️";
                    String detalle = recurso.getCasoActualId() > 0
                        ? " → Caso #" + recurso.getCasoActualId()
                        : "";

                    setText(String.format(
                        "%s %s\n%s%s",
                        emoji,
                        recurso.getIdentificador(),
                        recurso.getEstado(),
                        detalle
                    ));
                }
            }
        });
    }

    private void configurarGraficos() {
        // Configurar PieChart
        severidadPieChart.setTitle("Distribución por Severidad");
        severidadPieChart.setLegendVisible(true);

        // Configurar LineChart
        tiemposLineChart.setTitle("Evolución de Tiempos de Espera");
        tiemposLineChart.setCreateSymbols(false);

        // Configurar BarChart
        recursosBarChart.setTitle("Utilización de Recursos");
    }

    // === HANDLERS DE EVENTOS UI ===
    @FXML
    private void onIniciarSimulacion() {
        agregarLog("⏳ Iniciando simulación...");

        // Obtener configuración de UI
        SimulacionConfig config = new SimulacionConfig.Builder()
            .numOperadores(operadoresSpinner.getValue())
            .numAmbulancias(ambulanciasSpinner.getValue())
            .numEquiposMedicos(equiposSpinner.getValue())
            .duracionSegundos(duracionSpinner.getValue())
            .build();

        // Deshabilitar controles
        iniciarBtn.setDisable(true);
        pausarBtn.setDisable(false);
        detenerBtn.setDisable(false);
        deshabilitarSpinners(true);

        // Reiniciar contadores
        casosTotales = 0;
        casosCompletados = 0;
        emergencias.clear();
        recursos.clear();

        // Ejecutar simulación en thread separado
        Task<Void> simulacionTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                simulacionManager.ejecutarSimulacion(config);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    agregarLog("✅ Simulación completada exitosamente");
                    habilitarBotones(true, false, false);
                    deshabilitarSpinners(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    agregarLog("❌ Error en simulación: " + getException().getMessage());
                    habilitarBotones(true, false, false);
                    deshabilitarSpinners(false);
                });
            }
        };

        new Thread(simulacionTask).start();
    }

    @FXML
    private void onPausarSimulacion() {
        agregarLog("⏸ Simulación pausada");
        // TODO: Implementar pausa
    }

    @FXML
    private void onDetenerSimulacion() {
        agregarLog("🛑 Deteniendo simulación...");
        simulacionManager.detener();
        habilitarBotones(true, false, false);
        deshabilitarSpinners(false);
    }

    @FXML
    private void onLimpiarLogs() {
        logsTextArea.clear();
    }

    // === IMPLEMENTACIÓN DE OBSERVERS ===
    @Override
    public void onNuevaEmergencia(CasoEmergencia caso) {
        Platform.runLater(() -> {
            casosTotales++;
            emergencias.add(caso);

            agregarLog(String.format(
                "📞 Nueva emergencia #%d: %s en %s",
                caso.getCasoId(),
                caso.getSeveridad(),
                caso.getLugar()
            ));

            actualizarEstadisticas();
            actualizarGraficoSeveridad();
        });
    }

    @Override
    public void onEmergenciaAsignada(CasoEmergencia caso, Recurso recurso) {
        Platform.runLater(() -> {
            agregarLog(String.format(
                "✅ Emergencia #%d asignada a %s",
                caso.getCasoId(),
                recurso.getIdentificador()
            ));
        });
    }

    @Override
    public void onEmergenciaCompletada(CasoEmergencia caso) {
        Platform.runLater(() -> {
            casosCompletados++;
            emergencias.remove(caso);

            long tiempoTotal = caso.getTiempoTotalServicioMs() / 1000;
            agregarLog(String.format(
                "✔️ Emergencia #%d completada en %ds",
                caso.getCasoId(),
                tiempoTotal
            ));

            actualizarEstadisticas();
        });
    }

    @Override
    public void onRecursoDisponible(Recurso recurso) {
        Platform.runLater(() -> {
            actualizarRecurso(recurso);
        });
    }

    @Override
    public void onRecursoAsignado(Recurso recurso, CasoEmergencia caso) {
        Platform.runLater(() -> {
            actualizarRecurso(recurso);
        });
    }

    // === MÉTODOS AUXILIARES ===
    private void actualizarEstadisticas() {
        casosTotalesLabel.setText(String.valueOf(casosTotales));
        casosCompletadosLabel.setText(String.valueOf(casosCompletados));
        casosEnProcesoLabel.setText(String.valueOf(
            casosTotales - casosCompletados - emergencias.size()
        ));
        casosPendientesLabel.setText(String.valueOf(emergencias.size()));

        emergenciasPendientesLabel.setText(emergencias.size() + " en cola");

        // Calcular tiempos promedio
        // TODO: Implementar cálculo de promedios
    }

    private void actualizarGraficoSeveridad() {
        Map<CasoEmergencia.Severidad, Long> distribucion =
            emergencias.stream()
                .collect(Collectors.groupingBy(
                    CasoEmergencia::getSeveridad,
                    Collectors.counting()
                ));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        distribucion.forEach((severidad, count) -> {
            pieData.add(new PieChart.Data(
                severidad.name() + " (" + count + ")",
                count
            ));
        });

        severidadPieChart.setData(pieData);
    }

    private void actualizarRecurso(Recurso recurso) {
        // Actualizar o agregar recurso en la lista
        int index = recursos.indexOf(recurso);
        if (index >= 0) {
            recursos.set(index, recurso);
        } else {
            recursos.add(recurso);
        }

        long disponibles = recursos.stream()
            .filter(r -> r.estaDisponible())
            .count();

        recursosDisponiblesLabel.setText(
            disponibles + " disponibles / " + recursos.size() + " total"
        );
    }

    private void agregarLog(String mensaje) {
        String timestamp = LocalTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss")
        );

        Platform.runLater(() -> {
            logsTextArea.appendText(
                String.format("[%s] %s\n", timestamp, mensaje)
            );
        });
    }

    private void habilitarBotones(boolean iniciar, boolean pausar, boolean detener) {
        iniciarBtn.setDisable(!iniciar);
        pausarBtn.setDisable(!pausar);
        detenerBtn.setDisable(!detener);
    }

    private void deshabilitarSpinners(boolean deshabilitar) {
        operadoresSpinner.setDisable(deshabilitar);
        ambulanciasSpinner.setDisable(deshabilitar);
        equiposSpinner.setDisable(deshabilitar);
        duracionSpinner.setDisable(deshabilitar);
    }

    private String obtenerEmojiSeveridad(CasoEmergencia.Severidad severidad) {
        return switch (severidad) {
            case CRITICO -> "🔴";
            case GRAVE -> "🟠";
            case MODERADO -> "🟡";
            case LEVE -> "🟢";
        };
    }

    private String obtenerColorSeveridad(CasoEmergencia.Severidad severidad) {
        return switch (severidad) {
            case CRITICO -> "#ffebee";
            case GRAVE -> "#fff3e0";
            case MODERADO -> "#fffde7";
            case LEVE -> "#e8f5e9";
        };
    }
}
```

---

## 🎨 Paso 4: Crear Estilos CSS

### styles.css (en src/main/resources/css/)

```css
/* ============================================
   VARIABLES Y COLORES
   ============================================ */
.root {
  -fx-font-family: "Segoe UI", Arial, sans-serif;
  -fx-font-size: 14px;

  /* Colores principales */
  -color-primary: #2196f3;
  -color-success: #4caf50;
  -color-warning: #ff9800;
  -color-danger: #f44336;
  -color-info: #00bcd4;

  /* Colores de fondo */
  -bg-primary: #f5f5f5;
  -bg-secondary: #ffffff;
  -bg-dark: #263238;

  /* Severidades */
  -color-critico: #f44336;
  -color-grave: #ff9800;
  -color-moderado: #ffc107;
  -color-leve: #4caf50;
}

/* ============================================
   COMPONENTES GENERALES
   ============================================ */
.header {
  -fx-background-color: -color-primary;
  -fx-padding: 20;
}

.header .title {
  -fx-text-fill: white;
  -fx-font-weight: bold;
}

.control-panel {
  -fx-background-color: derive(-color-primary, 20%);
  -fx-padding: 15;
}

.panel {
  -fx-background-color: -bg-secondary;
  -fx-border-color: #e0e0e0;
  -fx-border-width: 1;
}

.panel-title {
  -fx-font-weight: bold;
  -fx-padding: 0 0 10 0;
}

.subtitle {
  -fx-text-fill: #757575;
  -fx-padding: 5 0 10 0;
}

/* ============================================
   BOTONES
   ============================================ */
.button {
  -fx-background-color: -color-primary;
  -fx-text-fill: white;
  -fx-padding: 10 20;
  -fx-background-radius: 5;
  -fx-cursor: hand;
  -fx-font-weight: bold;
}

.button:hover {
  -fx-background-color: derive(-color-primary, -10%);
}

.button:pressed {
  -fx-background-color: derive(-color-primary, -20%);
}

.btn-success {
  -fx-background-color: -color-success;
}

.btn-success:hover {
  -fx-background-color: derive(-color-success, -10%);
}

.btn-warning {
  -fx-background-color: -color-warning;
}

.btn-warning:hover {
  -fx-background-color: derive(-color-warning, -10%);
}

.btn-danger {
  -fx-background-color: -color-danger;
}

.btn-danger:hover {
  -fx-background-color: derive(-color-danger, -10%);
}

.btn-secondary {
  -fx-background-color: #757575;
}

.btn-secondary:hover {
  -fx-background-color: #616161;
}

/* ============================================
   ESTADÍSTICAS
   ============================================ */
.stats-grid {
  -fx-background-color: #fafafa;
  -fx-padding: 15;
  -fx-hgap: 15;
  -fx-vgap: 15;
}

.stat-card {
  -fx-background-color: white;
  -fx-padding: 20;
  -fx-background-radius: 8;
  -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.1), 5, 0, 0, 2);
  -fx-alignment: center;
}

.stat-label {
  -fx-font-size: 12px;
  -fx-text-fill: #757575;
  -fx-padding: 0 0 5 0;
}

.stat-value {
  -fx-font-size: 32px;
  -fx-font-weight: bold;
  -fx-text-fill: -color-primary;
}

.stat-value-success {
  -fx-font-size: 32px;
  -fx-font-weight: bold;
  -fx-text-fill: -color-success;
}

.stat-value-warning {
  -fx-font-size: 32px;
  -fx-font-weight: bold;
  -fx-text-fill: -color-warning;
}

.stat-value-danger {
  -fx-font-size: 32px;
  -fx-font-weight: bold;
  -fx-text-fill: -color-danger;
}

.stat-value-info {
  -fx-font-size: 28px;
  -fx-font-weight: bold;
  -fx-text-fill: -color-info;
}

/* ============================================
   LISTAS
   ============================================ */
.list-view {
  -fx-background-color: transparent;
  -fx-border-width: 0;
}

.list-cell {
  -fx-padding: 10;
  -fx-background-color: white;
  -fx-border-color: #e0e0e0;
  -fx-border-width: 0 0 1 0;
}

.list-cell:hover {
  -fx-background-color: #f5f5f5;
}

.list-cell:selected {
  -fx-background-color: #e3f2fd;
}

.emergencias-list .list-cell {
  -fx-font-family: monospace;
  -fx-font-size: 12px;
}

.recursos-list .list-cell {
  -fx-font-size: 13px;
}

/* ============================================
   LOGS
   ============================================ */
.logs-panel {
  -fx-background-color: #263238;
}

.logs-panel .panel-title {
  -fx-text-fill: white;
}

.logs-area {
  -fx-background-color: #1e272e;
  -fx-text-fill: #00ff00;
  -fx-font-family: "Consolas", "Courier New", monospace;
  -fx-font-size: 12px;
  -fx-background-radius: 5;
  -fx-padding: 10;
}

.logs-area .content {
  -fx-background-color: #1e272e;
}

/* ============================================
   GRÁFICOS
   ============================================ */
.chart {
  -fx-padding: 10;
}

.chart-legend {
  -fx-background-color: white;
  -fx-padding: 10;
}

.pie-chart {
  -fx-pie-color-1: -color-critico;
  -fx-pie-color-2: -color-grave;
  -fx-pie-color-3: -color-moderado;
  -fx-pie-color-4: -color-leve;
}

/* ============================================
   SPINNERS
   ============================================ */
.spinner {
  -fx-background-color: white;
}

.spinner .text-field {
  -fx-background-color: white;
  -fx-text-fill: #263238;
}

/* ============================================
   TABS
   ============================================ */
.tab-pane {
  -fx-background-color: white;
}

.tab {
  -fx-background-color: #f5f5f5;
}

.tab:selected {
  -fx-background-color: white;
}

/* ============================================
   SCROLLBARS
   ============================================ */
.scroll-bar {
  -fx-background-color: transparent;
}

.scroll-bar .thumb {
  -fx-background-color: #bdbdbd;
  -fx-background-radius: 5;
}

.scroll-bar .thumb:hover {
  -fx-background-color: #9e9e9e;
}
```

---

## 🚀 Paso 5: Ejecutar la Aplicación

### Ejecutar con Maven:

```bash
mvn clean javafx:run
```

### Ejecutar desde IDE:

1. Asegúrate de que las opciones de VM incluyan:

   ```
   --module-path "ruta/a/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml
   ```

2. Ejecuta `EmergenciasApp.main()`

---

## 📝 Resumen

Has creado:

1. ✅ Aplicación JavaFX principal
2. ✅ Vista FXML con layout completo
3. ✅ Controlador con lógica de negocio
4. ✅ Estilos CSS profesionales
5. ✅ Integración con sistema de observadores
6. ✅ Actualización en tiempo real con Platform.runLater()
7. ✅ Gráficos y visualizaciones

**Próximos pasos:**

- Mejorar visualización de recursos con mapas
- Agregar animaciones para transiciones
- Implementar configuración persistente
- Agregar exportación de reportes
