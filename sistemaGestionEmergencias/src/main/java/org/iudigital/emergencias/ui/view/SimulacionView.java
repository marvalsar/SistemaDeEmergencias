package org.iudigital.emergencias.ui.view;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.iudigital.emergencias.domain.Ambulancia;
import org.iudigital.emergencias.domain.CasoEmergencia;
import org.iudigital.emergencias.domain.EquipoMedico;
import org.iudigital.emergencias.manager.SimulacionManager;
import org.iudigital.emergencias.observer.EventPublisher;
import org.iudigital.emergencias.ui.model.SimulacionConfig;
import org.iudigital.emergencias.ui.observer.JavaFXObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Vista principal de la simulación con animaciones en tiempo real.
 * Incluye mapa animado, estadísticas con pestañas y controles.
 */
public class SimulacionView {

    private final Stage stage;
    private final SimulacionConfig config;
    private final SimulacionManager manager;
    private final JavaFXObserver observer;
    private final EventPublisher eventPublisher;

    // Componentes UI
    private Canvas mapCanvas;
    private GraphicsContext gc;
    private TabPane tabPane;
    private VBox recursosPanel;
    private VBox hilosPanel;
    private ListView<String> eventsLog;
    private ProgressBar progressBar;
    private Label timeLabel;
    private Label casosLabel;
    private Label hilosActivosLabel;

    // Animación
    private AnimationTimer animationTimer;
    private Timeline uiUpdater;
    private long startTime;
    private boolean simulacionTerminada = false;
    private final Random random = new Random();

    // Entidades visuales
    private final List<AmbulanciaVisual> ambulanciasVisuales = new ArrayList<>();
    private final List<CasoVisual> casosVisuales = new ArrayList<>();
    private final List<HospitalVisual> hospitales = new ArrayList<>();

    public SimulacionView(Stage stage, SimulacionConfig config) {
        this.stage = stage;
        this.config = config;
        this.manager = SimulacionManager.getInstance();
        this.eventPublisher = new EventPublisher();
        this.observer = new JavaFXObserver(this);
        this.eventPublisher.registrarObserver(observer);
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a237e;");

        // Panel superior con información
        HBox topPanel = createTopPanel();

        // Panel central con mapa
        StackPane centerPanel = createMapPanel();

        // Panel derecho con estadísticas
        VBox rightPanel = createStatsPanel();

        // Panel inferior con controles
        HBox bottomPanel = createControlPanel();

        root.setTop(topPanel);
        root.setCenter(centerPanel);
        root.setRight(rightPanel);
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/styles/simulation.css").toExternalForm());

        stage.setScene(scene);
        stage.show();

        // Iniciar simulación
        iniciarSimulacion();
    }

    private HBox createTopPanel() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.3);" +
                        "-fx-border-color: rgba(255, 255, 255, 0.2);" +
                        "-fx-border-width: 0 0 2 0;");

        Label title = new Label("🚨 SIMULACIÓN EN VIVO");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        timeLabel = new Label("Tiempo: 0s / " + config.getDuracionSegundos() + "s");
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        timeLabel.setTextFill(Color.rgb(100, 255, 100));

        casosLabel = new Label("Casos: 0 atendidos | 0 en cola");
        casosLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        casosLabel.setTextFill(Color.WHITE);

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: #4caf50;");

        panel.getChildren().addAll(title, timeLabel, progressBar, casosLabel);

        return panel;
    }

    private StackPane createMapPanel() {
        StackPane panel = new StackPane();

        // Canvas para el mapa animado
        mapCanvas = new Canvas(900, 700);
        gc = mapCanvas.getGraphicsContext2D();

        panel.getChildren().add(mapCanvas);
        panel.setPadding(new Insets(10));

        // Inicializar hospitales
        initHospitales();

        return panel;
    }

    private VBox createStatsPanel() {
        VBox panel = new VBox(15);
        panel.setPrefWidth(350);
        panel.setPadding(new Insets(20));
        panel.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.4);" +
                        "-fx-border-color: rgba(255, 255, 255, 0.2);" +
                        "-fx-border-width: 0 0 0 2;");

        Label statsTitle = new Label("📊 ESTADÍSTICAS");
        statsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        statsTitle.setTextFill(Color.WHITE);

        // TabPane con pestañas
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setPrefHeight(450);

        // Pestaña 1: Recursos
        Tab recursosTab = new Tab("🚑 Recursos");
        recursosPanel = new VBox(8);
        recursosPanel.setPadding(new Insets(10));
        recursosPanel.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
        ScrollPane recursosScroll = new ScrollPane(recursosPanel);
        recursosScroll.setFitToWidth(true);
        recursosScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        recursosTab.setContent(recursosScroll);

        // Pestaña 2: Hilos
        Tab hilosTab = new Tab("⚙️ Hilos");
        hilosPanel = new VBox(10);
        hilosPanel.setPadding(new Insets(10));
        hilosPanel.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
        ScrollPane hilosScroll = new ScrollPane(hilosPanel);
        hilosScroll.setFitToWidth(true);
        hilosScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        hilosTab.setContent(hilosScroll);

        // Pestaña 3: Eventos
        Tab eventosTab = new Tab("📝 Eventos");
        eventsLog = new ListView<>();
        eventsLog.setPrefHeight(400);
        eventsLog.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.5);" +
                        "-fx-text-fill: white;");
        eventosTab.setContent(eventsLog);

        tabPane.getTabs().addAll(recursosTab, hilosTab, eventosTab);

        // Label de hilos activos
        hilosActivosLabel = new Label("Hilos activos: 0");
        hilosActivosLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        hilosActivosLabel.setTextFill(Color.LIGHTGREEN);

        panel.getChildren().addAll(statsTitle, hilosActivosLabel, tabPane);

        return panel;
    }

    private HBox createControlPanel() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.3);" +
                        "-fx-border-color: rgba(255, 255, 255, 0.2);" +
                        "-fx-border-width: 2 0 0 0;");

        Button pauseButton = createControlButton("⏸️ Pausar", Color.rgb(255, 193, 7));
        Button stopButton = createControlButton("⏹️ Detener", Color.rgb(244, 67, 54));
        Button menuButton = createControlButton("🏠 Menú", Color.rgb(96, 125, 139));

        pauseButton.setOnAction(e -> togglePause());
        stopButton.setOnAction(e -> detenerSimulacion());
        menuButton.setOnAction(e -> volverAlMenu());

        panel.getChildren().addAll(pauseButton, stopButton, menuButton);

        return panel;
    }

    private Button createControlButton(String text, Color color) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setPrefWidth(150);
        button.setPrefHeight(45);
        button.setTextFill(Color.WHITE);
        button.setStyle(String.format(
                "-fx-background-color: rgb(%d, %d, %d);" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)));

        return button;
    }

    private void initHospitales() {
        // Crear 3 hospitales en posiciones estratégicas con IDs únicos
        hospitales.add(new HospitalVisual(150, 150, "Hospital Central", 0));
        hospitales.add(new HospitalVisual(650, 150, "Hospital Norte", 1));
        hospitales.add(new HospitalVisual(450, 550, "Hospital Sur", 2));
    }

    private void iniciarSimulacion() {
        startTime = System.currentTimeMillis();

        // Configurar manager
        manager.setEventPublisher(eventPublisher);

        // Inicializar ambulancias visuales
        List<Ambulancia> ambulancias = manager.getAmbulancias();
        if (ambulancias.isEmpty()) {
            manager.inicializarSimulacion(
                    config.getNumAmbulancias(),
                    config.getNumEquiposMedicos(),
                    config.getNumOperadores(),
                    config.getDuracionSegundos());
            ambulancias = manager.getAmbulancias();
        }

        for (Ambulancia amb : ambulancias) {
            HospitalVisual hospital = hospitales.get(random.nextInt(hospitales.size()));
            ambulanciasVisuales.add(new AmbulanciaVisual(amb, hospital.x, hospital.y));
        }

        // Iniciar animación
        startAnimation();

        // Actualizar UI periódicamente
        uiUpdater = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> updateUI()));
        uiUpdater.setCycleCount(Timeline.INDEFINITE);
        uiUpdater.play();
    }

    private void startAnimation() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderFrame();
            }
        };
        animationTimer.start();
    }

    private void renderFrame() {
        // Limpiar canvas
        gc.setFill(Color.rgb(30, 40, 60));
        gc.fillRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        // Dibujar grid
        drawGrid();

        // Dibujar hospitales
        for (HospitalVisual hospital : hospitales) {
            hospital.draw(gc);
        }

        // Dibujar casos
        for (CasoVisual caso : casosVisuales) {
            caso.draw(gc);
        }

        // Dibujar ambulancias
        for (AmbulanciaVisual amb : ambulanciasVisuales) {
            amb.update();
            amb.draw(gc);
        }
    }

    private void drawGrid() {
        gc.setStroke(Color.rgb(255, 255, 255, 0.05));
        gc.setLineWidth(1);

        for (int x = 0; x < mapCanvas.getWidth(); x += 50) {
            gc.strokeLine(x, 0, x, mapCanvas.getHeight());
        }

        for (int y = 0; y < mapCanvas.getHeight(); y += 50) {
            gc.strokeLine(0, y, mapCanvas.getWidth(), y);
        }
    }

    private void updateUI() {
        Platform.runLater(() -> {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            double progress = Math.min(1.0, (double) elapsed / config.getDuracionSegundos());

            timeLabel.setText(String.format("Tiempo: %ds / %ds", elapsed, config.getDuracionSegundos()));
            progressBar.setProgress(progress);

            int completados = manager.getCasosCompletados().size();
            int enCola = manager.getColaCasosEmergencia().size();
            casosLabel.setText(String.format("Casos: %d atendidos | %d en cola", completados, enCola));

            // Detectar si el tiempo terminó (solo una vez)
            if (elapsed >= config.getDuracionSegundos() && !simulacionTerminada && animationTimer != null) {
                simulacionTerminada = true;
                detenerSimulacion();
            }

            updateStatsPanel();
        });
    }

    private void updateStatsPanel() {
        recursosPanel.getChildren().clear();

        // Actualizar recursos
        for (Ambulancia amb : manager.getAmbulancias()) {
            HBox stat = createStatRow(
                    "🚑 AMB-" + amb.getIdAmbulancia(),
                    amb.getStatusAmbulancia().toString(),
                    getColorForStatus(amb.getStatusAmbulancia()));
            recursosPanel.getChildren().add(stat);
        }

        for (EquipoMedico eq : manager.getEquiposMedicos()) {
            HBox stat = createStatRow(
                    "⚕️ EQM-" + eq.getIdEquipo(),
                    eq.getStatusEquipo().toString(),
                    getColorForEquipoStatus(eq.getStatusEquipo()));
            recursosPanel.getChildren().add(stat);
        }

        // Actualizar panel de hilos
        updateHilosPanel();
    }

    private void updateHilosPanel() {
        hilosPanel.getChildren().clear();

        // Información de hilos del sistema
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parentGroup;
        while ((parentGroup = rootGroup.getParent()) != null) {
            rootGroup = parentGroup;
        }

        int activeThreads = rootGroup.activeCount();
        Thread[] threads = new Thread[activeThreads * 2];
        int count = rootGroup.enumerate(threads, true);

        hilosActivosLabel.setText(String.format("⚙️ Hilos activos: %d", count));

        // Contador por tipo de hilo
        int ambulancias = 0, equipos = 0, operadores = 0, monitores = 0, otros = 0;

        for (int i = 0; i < count; i++) {
            Thread t = threads[i];
            if (t != null) {
                String name = t.getName();
                String estado = t.getState().toString();
                boolean daemon = t.isDaemon();

                // Clasificar hilo
                String tipo = "OTRO";
                Color color = Color.GRAY;

                if (name.startsWith("Ambulancia-")) {
                    tipo = "🚑 AMBULANCIA";
                    color = Color.LIGHTGREEN;
                    ambulancias++;
                } else if (name.startsWith("EquipoMedico-")) {
                    tipo = "⚕️ EQUIPO MED";
                    color = Color.LIGHTBLUE;
                    equipos++;
                } else if (name.startsWith("Operador-")) {
                    tipo = "📞 OPERADOR";
                    color = Color.ORANGE;
                    operadores++;
                } else if (name.startsWith("Monitor-") || name.equals("Despachador")) {
                    tipo = "📊 MONITOR";
                    color = Color.YELLOW;
                    monitores++;
                } else if (name.contains("JavaFX") || name.contains("Quantum")) {
                    continue; // Omitir hilos de JavaFX para claridad
                } else {
                    otros++;
                }

                VBox hiloInfo = createHiloInfo(tipo, name, estado, daemon, color);
                hilosPanel.getChildren().add(hiloInfo);
            }
        }

        // Resumen
        VBox resumen = new VBox(5);
        resumen.setPadding(new Insets(10));
        resumen.setStyle(
                "-fx-background-color: rgba(76, 175, 80, 0.2);" +
                        "-fx-border-color: rgba(76, 175, 80, 0.5);" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;");

        Label resumenTitle = new Label("📈 RESUMEN DE HILOS");
        resumenTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        resumenTitle.setTextFill(Color.WHITE);

        Label ambLabel = new Label(String.format("🚑 Ambulancias: %d", ambulancias));
        ambLabel.setTextFill(Color.LIGHTGREEN);

        Label eqLabel = new Label(String.format("⚕️ Equipos Médicos: %d", equipos));
        eqLabel.setTextFill(Color.LIGHTBLUE);

        Label opLabel = new Label(String.format("📞 Operadores: %d", operadores));
        opLabel.setTextFill(Color.ORANGE);

        Label monLabel = new Label(String.format("📊 Monitores: %d", monitores));
        monLabel.setTextFill(Color.YELLOW);

        Label otrosLabel = new Label(String.format("⚙️ Otros: %d", otros));
        otrosLabel.setTextFill(Color.GRAY);

        resumen.getChildren().addAll(resumenTitle, ambLabel, eqLabel, opLabel, monLabel, otrosLabel);

        hilosPanel.getChildren().add(0, resumen);
    }

    private VBox createHiloInfo(String tipo, String nombre, String estado, boolean daemon, Color color) {
        VBox info = new VBox(3);
        info.setPadding(new Insets(8));
        info.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.05);" +
                        "-fx-border-color: rgba(255, 255, 255, 0.1);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;");

        Label tipoLabel = new Label(tipo);
        tipoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        tipoLabel.setTextFill(color);

        Label nombreLabel = new Label("📌 " + nombre);
        nombreLabel.setFont(Font.font("Courier New", 10));
        nombreLabel.setTextFill(Color.LIGHTGRAY);

        Label estadoLabel = new Label("⚡ Estado: " + estado + (daemon ? " [DAEMON]" : ""));
        estadoLabel.setFont(Font.font("Arial", 10));
        estadoLabel.setTextFill(Color.WHITE);

        info.getChildren().addAll(tipoLabel, nombreLabel, estadoLabel);

        return info;
    }

    private HBox createStatRow(String name, String status, Color color) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setPrefWidth(100);

        Circle indicator = new Circle(6);
        indicator.setFill(color);

        Label statusLabel = new Label(status);
        statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        statusLabel.setTextFill(color);

        row.getChildren().addAll(nameLabel, indicator, statusLabel);

        return row;
    }

    private Color getColorForStatus(Ambulancia.StatusAmbulancia status) {
        return switch (status) {
            case DISPONIBLE -> Color.rgb(76, 175, 80);
            case EN_RUTA -> Color.rgb(255, 193, 7);
            case OCUPADA -> Color.rgb(244, 67, 54);
            case RETORNANDO -> Color.rgb(33, 150, 243);
        };
    }

    private Color getColorForEquipoStatus(EquipoMedico.StatusEquipo status) {
        return switch (status) {
            case DISPONIBLE -> Color.rgb(76, 175, 80);
            case ASIGNADO -> Color.rgb(255, 193, 7);
            case OCUPADO -> Color.rgb(244, 67, 54);
            case RETORNANDO -> Color.rgb(33, 150, 243);
        };
    }

    public void addEventLog(String event) {
        Platform.runLater(() -> {
            eventsLog.getItems().add(0, event);
            if (eventsLog.getItems().size() > 50) {
                eventsLog.getItems().remove(50, eventsLog.getItems().size());
            }
        });
    }

    public void addCasoVisual(CasoEmergencia caso) {
        Platform.runLater(() -> {
            double x = random.nextDouble() * (mapCanvas.getWidth() - 100) + 50;
            double y = random.nextDouble() * (mapCanvas.getHeight() - 100) + 50;
            casosVisuales.add(new CasoVisual(caso, x, y));
        });
    }

    private void togglePause() {
        // Implementar pausa
    }

    private void detenerSimulacion() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        manager.detenerSimulacion();

        // Mostrar modal con estadísticas detalladas
        mostrarModalEstadisticas();
    }

    private void mostrarModalEstadisticas() {
        // Crear ventana modal
        Stage modalStage = new Stage();
        modalStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        modalStage.initOwner(stage);
        modalStage.setTitle("📊 Estadísticas Finales de la Simulación");

        VBox content = new VBox(15);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #1a237e;");

        // Título
        Label title = new Label("🎯 RESUMEN COMPLETO DE LA SIMULACIÓN");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        // ScrollPane para contenido
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefHeight(500);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox statsContent = new VBox(10);
        statsContent.setPadding(new Insets(10));

        // Estadísticas Generales
        statsContent.getChildren().add(createStatSection("⏱️ TIEMPO Y RENDIMIENTO",
                String.format("⌚ Duración total: %d segundos\n" +
                        "📋 Casos recibidos: %d\n" +
                        "✅ Casos completados: %d\n" +
                        "⏳ Casos en cola: %d\n" +
                        "📊 Eficiencia: %.1f%%",
                        (System.currentTimeMillis() - startTime) / 1000,
                        manager.getCasosCompletados().size() + manager.getColaCasosEmergencia().size(),
                        manager.getCasosCompletados().size(),
                        manager.getColaCasosEmergencia().size(),
                        manager.getCasosCompletados().isEmpty() ? 0
                                : (manager.getCasosCompletados().size() * 100.0) /
                                        (manager.getCasosCompletados().size()
                                                + manager.getColaCasosEmergencia().size()))));

        // Ambulancias
        StringBuilder ambStats = new StringBuilder();
        int ambDisponibles = 0, ambEnRuta = 0, ambOcupadas = 0, ambRetornando = 0;
        for (Ambulancia amb : manager.getAmbulancias()) {
            ambStats.append(String.format("🚑 Ambulancia %d: %s\n",
                    amb.getIdAmbulancia(), amb.getStatusAmbulancia()));
            switch (amb.getStatusAmbulancia()) {
                case DISPONIBLE -> ambDisponibles++;
                case EN_RUTA -> ambEnRuta++;
                case OCUPADA -> ambOcupadas++;
                case RETORNANDO -> ambRetornando++;
            }
        }
        ambStats.append(String.format("\n📈 Resumen: %d disponibles | %d en ruta | %d ocupadas | %d retornando",
                ambDisponibles, ambEnRuta, ambOcupadas, ambRetornando));
        statsContent.getChildren().add(createStatSection("🚑 AMBULANCIAS (" +
                manager.getAmbulancias().size() + ")", ambStats.toString()));

        // Equipos Médicos
        StringBuilder eqStats = new StringBuilder();
        int eqDisponibles = 0, eqAsignados = 0, eqOcupados = 0, eqRetornando = 0;
        for (EquipoMedico eq : manager.getEquiposMedicos()) {
            eqStats.append(String.format("⚕️ Equipo Médico %d: %s\n",
                    eq.getIdEquipo(), eq.getStatusEquipo()));
            switch (eq.getStatusEquipo()) {
                case DISPONIBLE -> eqDisponibles++;
                case ASIGNADO -> eqAsignados++;
                case OCUPADO -> eqOcupados++;
                case RETORNANDO -> eqRetornando++;
            }
        }
        eqStats.append(String.format("\n📈 Resumen: %d disponibles | %d asignados | %d ocupados | %d retornando",
                eqDisponibles, eqAsignados, eqOcupados, eqRetornando));
        statsContent.getChildren().add(createStatSection("⚕️ EQUIPOS MÉDICOS (" +
                manager.getEquiposMedicos().size() + ")", eqStats.toString()));

        // Casos por Severidad
        int criticos = 0, graves = 0, moderados = 0, leves = 0;
        for (CasoEmergencia caso : manager.getCasosCompletados()) {
            switch (caso.getSeveridad()) {
                case CRITICO -> criticos++;
                case GRAVE -> graves++;
                case MODERADO -> moderados++;
                case LEVE -> leves++;
            }
        }
        statsContent.getChildren().add(createStatSection("🚨 CASOS POR SEVERIDAD",
                String.format("🔴 Críticos: %d\n🟠 Graves: %d\n🟡 Moderados: %d\n🟢 Leves: %d",
                        criticos, graves, moderados, leves)));

        // Hilos del Sistema
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parentGroup;
        while ((parentGroup = rootGroup.getParent()) != null) {
            rootGroup = parentGroup;
        }
        int activeThreads = rootGroup.activeCount();
        Thread[] threads = new Thread[activeThreads * 2];
        int count = rootGroup.enumerate(threads, true);

        int ambulanciasThread = 0, equiposThread = 0, operadoresThread = 0, monitoresThread = 0;
        StringBuilder hilosInfo = new StringBuilder();
        for (int i = 0; i < count && threads[i] != null; i++) {
            String name = threads[i].getName();
            if (name.startsWith("Ambulancia"))
                ambulanciasThread++;
            else if (name.startsWith("EquipoMedico"))
                equiposThread++;
            else if (name.startsWith("Operador"))
                operadoresThread++;
            else if (name.startsWith("Monitor") || name.equals("Despachador"))
                monitoresThread++;
        }
        hilosInfo.append(String.format("🚑 Hilos Ambulancias: %d\n", ambulanciasThread));
        hilosInfo.append(String.format("⚕️ Hilos Equipos Médicos: %d\n", equiposThread));
        hilosInfo.append(String.format("📞 Hilos Operadores: %d\n", operadoresThread));
        hilosInfo.append(String.format("📊 Hilos Monitores/Despachador: %d\n", monitoresThread));
        hilosInfo.append(String.format("⚙️ Total hilos activos: %d", count));
        statsContent.getChildren().add(createStatSection("🔧 HILOS DEL SISTEMA", hilosInfo.toString()));

        // Tiempos Promedio
        if (!manager.getCasosCompletados().isEmpty()) {
            long tiempoTotal = 0;
            for (CasoEmergencia caso : manager.getCasosCompletados()) {
                tiempoTotal += (caso.getHoraAtendido() - caso.getHoraRecibido());
            }
            long promedio = tiempoTotal / manager.getCasosCompletados().size() / 1000;
            statsContent.getChildren().add(createStatSection("⏱️ TIEMPOS PROMEDIO",
                    String.format("📊 Tiempo promedio de atención: %d segundos\n" +
                            "🎯 Casos atendidos: %d",
                            promedio, manager.getCasosCompletados().size())));
        }

        scrollPane.setContent(statsContent);

        // Botones
        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);

        Button closeBtn = new Button("✅ Volver al Menú");
        closeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        closeBtn.setPrefWidth(200);
        closeBtn.setStyle(
                "-fx-background-color: #4caf50;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;");
        closeBtn.setOnAction(e -> {
            modalStage.close();

            // Reiniciar completamente todos los recursos
            manager.detenerSimulacion();

            // Limpiar estados de UI
            if (uiUpdater != null) {
                uiUpdater.stop();
                uiUpdater = null;
            }
            if (animationTimer != null) {
                animationTimer.stop();
                animationTimer = null;
            }

            casosVisuales.clear();
            ambulanciasVisuales.clear();
            recursosPanel.getChildren().clear();
            hilosPanel.getChildren().clear();
            eventsLog.getItems().clear();

            // Resetear flag para permitir nueva simulación
            simulacionTerminada = false;

            // Volver al menú principal
            volverAlMenu();
        });

        buttons.getChildren().add(closeBtn);

        content.getChildren().addAll(title, scrollPane, buttons);

        Scene modalScene = new Scene(content, 700, 650);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    private VBox createStatSection(String titulo, String contenido) {
        VBox section = new VBox(8);
        section.setPadding(new Insets(15));
        section.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.1);" +
                        "-fx-border-color: rgba(255, 255, 255, 0.3);" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;");

        Label titleLabel = new Label(titulo);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.rgb(100, 255, 100));

        Label contentLabel = new Label(contenido);
        contentLabel.setFont(Font.font("Courier New", 14));
        contentLabel.setTextFill(Color.WHITE);
        contentLabel.setWrapText(true);

        section.getChildren().addAll(titleLabel, contentLabel);

        return section;
    }

    private void volverAlMenu() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        MainMenuView menuView = new MainMenuView(stage);
        menuView.show();
    }

    // Clases internas para entidades visuales

    private class AmbulanciaVisual {
        final Ambulancia ambulancia;
        double x, y;
        double targetX, targetY;
        double baseX, baseY;
        double rotation = 0;

        AmbulanciaVisual(Ambulancia ambulancia, double baseX, double baseY) {
            this.ambulancia = ambulancia;
            this.baseX = baseX;
            this.baseY = baseY;
            this.x = baseX;
            this.y = baseY;
            this.targetX = baseX;
            this.targetY = baseY;
        }

        void update() {
            // Mover hacia el objetivo
            double dx = targetX - x;
            double dy = targetY - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 2) {
                double speed = 3.0;
                x += (dx / dist) * speed;
                y += (dy / dist) * speed;
                rotation = Math.atan2(dy, dx);
            }
        }

        void draw(GraphicsContext gc) {
            gc.save();
            gc.translate(x, y);
            gc.rotate(Math.toDegrees(rotation));

            // Dibujar ambulancia MÁS GRANDE Y VISIBLE
            Color color = getColorForStatus(ambulancia.getStatusAmbulancia());

            // Sombra para profundidad
            gc.setFill(Color.rgb(0, 0, 0, 0.3));
            gc.fillRoundRect(-22, -14, 44, 28, 8, 8);

            // Cuerpo principal más grande
            gc.setFill(Color.WHITE);
            gc.fillRoundRect(-20, -12, 40, 24, 6, 6);

            // Franja de color según estado
            gc.setFill(color);
            gc.fillRoundRect(-20, -3, 40, 6, 3, 3);

            // Ventanas delanteras
            gc.setFill(Color.rgb(100, 150, 200));
            gc.fillRoundRect(8, -8, 10, 7, 2, 2);
            gc.fillRoundRect(8, 1, 10, 7, 2, 2);

            // Cruz roja grande y visible
            gc.setFill(Color.RED);
            gc.fillRect(-3, -10, 6, 20); // Vertical
            gc.fillRect(-10, -3, 20, 6); // Horizontal

            // Borde de la cruz para más contraste
            gc.setStroke(Color.DARKRED);
            gc.setLineWidth(1);
            gc.strokeRect(-3, -10, 6, 20);
            gc.strokeRect(-10, -3, 20, 6);

            // Luces intermitentes si está activa
            if (ambulancia.getStatusAmbulancia() != Ambulancia.StatusAmbulancia.DISPONIBLE) {
                double pulse = Math.sin(System.currentTimeMillis() / 150.0);
                if (pulse > 0) {
                    gc.setFill(Color.rgb(255, 0, 0, 0.8));
                    gc.fillOval(-18, -10, 4, 4);
                    gc.fillOval(14, -10, 4, 4);
                }
            }

            gc.restore();

            // Halo pulsante más visible
            if (ambulancia.getStatusAmbulancia() != Ambulancia.StatusAmbulancia.DISPONIBLE) {
                double pulse = Math.sin(System.currentTimeMillis() / 200.0) * 8 + 35;
                gc.setFill(Color.rgb((int) (color.getRed() * 255),
                        (int) (color.getGreen() * 255),
                        (int) (color.getBlue() * 255), 0.2));
                gc.fillOval(x - pulse, y - pulse, pulse * 2, pulse * 2);
            }

            // ID visible sobre la ambulancia
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            String idText = String.valueOf(ambulancia.getIdAmbulancia());
            gc.strokeText(idText, x - 8, y - 20);
            gc.fillText(idText, x - 8, y - 20);
        }
    }

    private class CasoVisual {
        final CasoEmergencia caso;
        final double x, y;
        double pulse = 0;

        CasoVisual(CasoEmergencia caso, double x, double y) {
            this.caso = caso;
            this.x = x;
            this.y = y;
        }

        void draw(GraphicsContext gc) {
            pulse += 0.1;
            double size = 20 + Math.sin(pulse) * 5;

            Color color = switch (caso.getSeveridad()) {
                case CRITICO -> Color.RED;
                case GRAVE -> Color.ORANGE;
                case MODERADO -> Color.YELLOW;
                case LEVE -> Color.LIGHTGREEN;
            };

            gc.setFill(color);
            gc.fillOval(x - size / 2, y - size / 2, size, size);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.fillText("!", x - 3, y + 5);
        }
    }

    private class HospitalVisual {
        final double x, y;
        final String name;
        final int hospitalId;
        private int contadorLlegadas = 0;

        HospitalVisual(double x, double y, String name, int hospitalId) {
            this.x = x;
            this.y = y;
            this.name = name;
            this.hospitalId = hospitalId;
        }

        void draw(GraphicsContext gc) {
            // Edificio del hospital
            gc.setFill(Color.rgb(230, 230, 230));
            gc.fillRect(x - 40, y - 40, 80, 80);

            // Cruz roja grande
            gc.setFill(Color.RED);
            gc.fillRect(x - 5, y - 30, 10, 60);
            gc.fillRect(x - 30, y - 5, 60, 10);

            // Nombre
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.fillText(name, x - 40, y + 55);

            // Panel lateral con lista de ambulancias
            List<Ambulancia> todasAmbulancias = manager.getAmbulancias();
            int disponibles = 0, enRuta = 0, ocupadas = 0, retornando = 0;

            // Calcular llegadas específicas de este hospital
            // Distribuir casos completados entre los 3 hospitales según el ID del caso
            List<CasoEmergencia> casosCompletados = manager.getCasosCompletados();
            contadorLlegadas = 0;
            for (CasoEmergencia caso : casosCompletados) {
                // Asignar caso a hospital según el módulo de su ID
                int hospitalAsignado = (int) (caso.getCasoId() % 3);
                if (hospitalAsignado == hospitalId) {
                    contadorLlegadas++;
                }
            }

            for (Ambulancia amb : todasAmbulancias) {
                switch (amb.getStatusAmbulancia()) {
                    case DISPONIBLE -> disponibles++;
                    case EN_RUTA -> enRuta++;
                    case OCUPADA -> ocupadas++;
                    case RETORNANDO -> retornando++;
                }
            }

            // Panel con información detallada
            double panelX = x + 50;
            double panelY = y - 50;
            double panelWidth = 180;
            double panelHeight = 130;

            // Fondo del panel
            gc.setFill(Color.rgb(0, 0, 0, 0.85));
            gc.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 8, 8);

            // Borde del panel
            gc.setStroke(Color.rgb(76, 175, 80));
            gc.setLineWidth(2);
            gc.strokeRoundRect(panelX, panelY, panelWidth, panelHeight, 8, 8);

            // Título del panel
            gc.setFill(Color.rgb(100, 255, 100));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.fillText("🚑 AMBULANCIAS", panelX + 8, panelY + 18);

            // Línea separadora
            gc.setStroke(Color.rgb(100, 255, 100, 0.5));
            gc.setLineWidth(1);
            gc.strokeLine(panelX + 8, panelY + 22, panelX + panelWidth - 8, panelY + 22);

            // Detalles con iconos
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
            double lineY = panelY + 36;

            // Disponibles
            gc.setFill(Color.LIGHTGREEN);
            gc.fillText("✓ Disponibles: " + disponibles, panelX + 10, lineY);

            // En Ruta
            lineY += 16;
            gc.setFill(Color.LIGHTYELLOW);
            gc.fillText("→ En Ruta: " + enRuta, panelX + 10, lineY);

            // Ocupadas
            lineY += 16;
            gc.setFill(Color.ORANGE);
            gc.fillText("◉ Ocupadas: " + ocupadas, panelX + 10, lineY);

            // Retornando
            lineY += 16;
            gc.setFill(Color.CYAN);
            gc.fillText("↩ Retornando: " + retornando, panelX + 10, lineY);

            // Línea separadora
            lineY += 8;
            gc.setStroke(Color.rgb(255, 255, 255, 0.3));
            gc.setLineWidth(1);
            gc.strokeLine(panelX + 8, lineY, panelX + panelWidth - 8, lineY);

            // Total de ambulancias
            lineY += 14;
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            gc.fillText("TOTAL: " + todasAmbulancias.size(), panelX + 10, lineY);

            // CONTADOR DE LLEGADAS AL HOSPITAL
            lineY += 18;
            gc.setFill(Color.rgb(255, 215, 0)); // Dorado
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.fillText("🏥 LLEGADAS: " + contadorLlegadas, panelX + 10, lineY);

            // Brillo pulsante
            double pulse = Math.sin(System.currentTimeMillis() / 1000.0) * 20 + 60;
            gc.setStroke(Color.rgb(255, 0, 0, 0.3));
            gc.setLineWidth(3);
            gc.strokeOval(x - pulse, y - pulse, pulse * 2, pulse * 2);
        }
    }
}
