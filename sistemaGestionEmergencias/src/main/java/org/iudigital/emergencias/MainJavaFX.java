package org.iudigital.emergencias;

import org.iudigital.emergencias.ui.EmergenciasApp;

/**
 * Punto de entrada de la aplicación JavaFX.
 * Lanza EmergenciasApp que extiende Application.
 */
public class MainJavaFX {

    public static void main(String[] args) {
        // Configurar propiedades del sistema para JavaFX
        System.setProperty("javafx.animation.fullspeed", "true");
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");

        // Lanzar aplicación JavaFX
        EmergenciasApp.launch(EmergenciasApp.class, args);
    }
}
