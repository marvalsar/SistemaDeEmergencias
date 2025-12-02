package org.iudigital.emergencias.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.iudigital.emergencias.ui.view.MainMenuView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aplicación principal JavaFX del Sistema de Gestión de Emergencias.
 * Punto de entrada de la interfaz gráfica.
 */
public class EmergenciasApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(EmergenciasApp.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Iniciando aplicación JavaFX");

            // Configurar stage principal
            primaryStage.setTitle("Sistema de Gestión de Emergencias v2.0");
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);

            // Cargar vista del menú principal
            MainMenuView mainMenuView = new MainMenuView(primaryStage);
            mainMenuView.show();

            // Manejar cierre de aplicación
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Cerrando aplicación");
                Platform.exit();
                System.exit(0);
            });

        } catch (Exception e) {
            logger.error("Error iniciando aplicación JavaFX", e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
