package org.iudigital.emergencias.ui.view;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.iudigital.emergencias.ui.model.SimulacionConfig;

/**
 * Vista del menú principal con animaciones y diseño moderno.
 */
public class MainMenuView {

    private final Stage stage;
    private final SimulacionConfig config;

    public MainMenuView(Stage stage) {
        this.stage = stage;
        this.config = new SimulacionConfig();
    }

    public void show() {
        StackPane root = new StackPane();

        // Fondo animado con gradiente
        Rectangle background = createAnimatedBackground();

        // Contenedor principal
        VBox mainContainer = createMainContainer();

        root.getChildren().addAll(background, mainContainer);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

        stage.setScene(scene);
        stage.show();

        // Animación de entrada
        playIntroAnimation(mainContainer);
    }

    private Rectangle createAnimatedBackground() {
        Rectangle bg = new Rectangle(1200, 800);

        // Gradiente animado
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(26, 35, 126)),
                new Stop(0.5, Color.rgb(13, 71, 161)),
                new Stop(1, Color.rgb(1, 87, 155)));

        bg.setFill(gradient);

        // Animación de pulso en el fondo
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(bg.opacityProperty(), 0.9)),
                new KeyFrame(Duration.seconds(2), new KeyValue(bg.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(4), new KeyValue(bg.opacityProperty(), 0.9)));
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();

        return bg;
    }

    private VBox createMainContainer() {
        VBox container = new VBox(40);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50));

        // Título con efecto
        Text title = createAnimatedTitle();

        // Subtítulo
        Label subtitle = new Label("Sistema de Coordinación de Recursos Médicos");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        subtitle.setTextFill(Color.rgb(200, 230, 255));

        // Panel de configuración
        VBox configPanel = createConfigPanel();

        // Botones de acción
        HBox buttons = createActionButtons();

        container.getChildren().addAll(title, subtitle, configPanel, buttons);

        return container;
    }

    private Text createAnimatedTitle() {
        Text title = new Text("🚑 SISTEMA DE EMERGENCIAS 🚑");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setFill(Color.WHITE);

        // Efecto de sombra
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 150, 255, 0.8));
        shadow.setRadius(20);
        title.setEffect(shadow);

        // Animación de brillo
        Timeline glow = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(shadow.radiusProperty(), 15)),
                new KeyFrame(Duration.seconds(1), new KeyValue(shadow.radiusProperty(), 25)),
                new KeyFrame(Duration.seconds(2), new KeyValue(shadow.radiusProperty(), 15)));
        glow.setCycleCount(Timeline.INDEFINITE);
        glow.play();

        return title;
    }

    private VBox createConfigPanel() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(600);
        panel.setPadding(new Insets(30));
        panel.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.25);" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.5);" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-width: 2;");

        // Efecto de cristal MUY sutil para mejor legibilidad
        GaussianBlur blur = new GaussianBlur(0.5);
        panel.setEffect(blur);

        Label configTitle = new Label("⚙️ CONFIGURACIÓN DE SIMULACIÓN");
        configTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        configTitle.setTextFill(Color.WHITE);

        // Controles de configuración con animación
        VBox ambulanciasControl = createSliderControl(
                "🚑 Ambulancias", 1, 10, config.getNumAmbulancias(),
                value -> config.setNumAmbulancias((int) value));

        VBox equiposControl = createSliderControl(
                "⚕️ Equipos Médicos", 1, 5, config.getNumEquiposMedicos(),
                value -> config.setNumEquiposMedicos((int) value));

        VBox operadoresControl = createSliderControl(
                "📞 Operadores", 1, 5, config.getNumOperadores(),
                value -> config.setNumOperadores((int) value));

        VBox duracionControl = createSliderControl(
                "⏱️ Duración (segundos)", 30, 300, config.getDuracionSegundos(),
                value -> config.setDuracionSegundos((int) value));

        // CheckBox para modo turbo
        CheckBox turboCheck = new CheckBox("⚡ Modo Turbo (Velocidad 2x)");
        turboCheck.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        turboCheck.setTextFill(Color.rgb(255, 215, 0));
        turboCheck.selectedProperty().addListener((obs, old, selected) -> config.setModoTurbo(selected));

        panel.getChildren().addAll(
                configTitle,
                ambulanciasControl,
                equiposControl,
                operadoresControl,
                duracionControl,
                turboCheck);

        return panel;
    }

    private VBox createSliderControl(String label, double min, double max,
            double initial, SliderValueConsumer consumer) {
        VBox control = new VBox(10);
        control.setAlignment(Pos.CENTER_LEFT);

        HBox labelBox = new HBox(10);
        labelBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(label);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.WHITE);

        Label valueLabel = new Label(String.format("%.0f", initial));
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.rgb(100, 255, 100));

        labelBox.getChildren().addAll(nameLabel, valueLabel);

        Slider slider = new Slider(min, max, initial);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit((max - min) / 5);
        slider.setBlockIncrement(1);
        slider.setSnapToTicks(true);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            valueLabel.setText(String.format("%.0f", newVal.doubleValue()));
            consumer.accept(newVal.doubleValue());

            // Animación de feedback
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), valueLabel);
            scale.setToX(1.2);
            scale.setToY(1.2);
            scale.setAutoReverse(true);
            scale.setCycleCount(2);
            scale.play();
        });

        control.getChildren().addAll(labelBox, slider);

        return control;
    }

    private HBox createActionButtons() {
        HBox buttons = new HBox(30);
        buttons.setAlignment(Pos.CENTER);

        Button startButton = createStyledButton("▶️ INICIAR SIMULACIÓN", Color.rgb(76, 175, 80));
        Button exitButton = createStyledButton("❌ SALIR", Color.rgb(244, 67, 54));

        startButton.setOnAction(e -> startSimulation());
        exitButton.setOnAction(e -> stage.close());

        buttons.getChildren().addAll(startButton, exitButton);

        return buttons;
    }

    private Button createStyledButton(String text, Color color) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        button.setPrefWidth(300);
        button.setPrefHeight(60);
        button.setTextFill(Color.WHITE);
        button.setStyle(String.format(
                "-fx-background-color: rgb(%d, %d, %d);" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)));

        // Efecto hover
        button.setOnMouseEntered(e -> {
            button.setStyle(button.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);");
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    button.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);", ""));
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return button;
    }

    private void playIntroAnimation(VBox container) {
        container.setOpacity(0);
        container.setTranslateY(50);

        FadeTransition fade = new FadeTransition(Duration.seconds(1), container);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition translate = new TranslateTransition(Duration.seconds(1), container);
        translate.setFromY(50);
        translate.setToY(0);

        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.play();
    }

    private void startSimulation() {
        // Transición a la vista de simulación
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), stage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            SimulacionView simulacionView = new SimulacionView(stage, config);
            simulacionView.show();
        });
        fadeOut.play();
    }

    @FunctionalInterface
    interface SliderValueConsumer {
        void accept(double value);
    }
}
