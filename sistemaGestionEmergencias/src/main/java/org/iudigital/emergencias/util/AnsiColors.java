package org.iudigital.emergencias.util;

/**
 * Utilidad para colores ANSI y estilos de texto en consola.
 * Proporciona una paleta completa de colores y efectos visuales.
 */
public class AnsiColors {

    // Reset
    public static final String RESET = "\u001B[0m";

    // Colores de texto básicos
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Colores brillantes
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    // Fondos
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_MAGENTA = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";

    // Estilos
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String BLINK = "\u001B[5m";
    public static final String REVERSE = "\u001B[7m";

    // Colores temáticos para la aplicación
    public static final String CRITICO = BRIGHT_RED + BOLD;
    public static final String GRAVE = RED;
    public static final String MODERADO = YELLOW;
    public static final String LEVE = GREEN;
    public static final String INFO = BRIGHT_CYAN;
    public static final String SUCCESS = BRIGHT_GREEN;
    public static final String WARNING = BRIGHT_YELLOW;
    public static final String ERROR = BRIGHT_RED;

    /**
     * Colorea un texto con el color especificado.
     */
    public static String colorize(String text, String color) {
        return color + text + RESET;
    }

    /**
     * Colorea texto según la severidad del caso.
     */
    public static String colorBySeverity(String text, String severity) {
        return switch (severity.toUpperCase()) {
            case "CRITICO" -> CRITICO + text + RESET;
            case "GRAVE" -> GRAVE + text + RESET;
            case "MODERADO" -> MODERADO + text + RESET;
            case "LEVE" -> LEVE + text + RESET;
            default -> text;
        };
    }

    /**
     * Aplica negrita al texto.
     */
    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    /**
     * Crea un texto con fondo de color.
     */
    public static String withBackground(String text, String bgColor, String fgColor) {
        return bgColor + fgColor + text + RESET;
    }
}
