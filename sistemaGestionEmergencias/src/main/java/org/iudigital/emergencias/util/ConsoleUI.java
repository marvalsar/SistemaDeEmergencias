package org.iudigital.emergencias.util;

import org.iudigital.emergencias.domain.Ambulancia;
import org.iudigital.emergencias.domain.CasoEmergencia;
import org.iudigital.emergencias.domain.EquipoMedico;

import java.util.List;

/**
 * Utilidad para crear interfaces gráficas avanzadas en consola.
 * Usa caracteres Unicode box-drawing y colores ANSI.
 */
public class ConsoleUI {

    // Caracteres Unicode para dibujo de cajas
    private static final String TOP_LEFT = "╔";
    private static final String TOP_RIGHT = "╗";
    private static final String BOTTOM_LEFT = "╚";
    private static final String BOTTOM_RIGHT = "╝";
    private static final String HORIZONTAL = "═";
    private static final String VERTICAL = "║";
    private static final String T_DOWN = "╦";
    private static final String T_UP = "╩";
    private static final String T_RIGHT = "╠";
    private static final String T_LEFT = "╣";
    private static final String CROSS = "╬";

    // Símbolos especiales
    private static final String ARROW_RIGHT = "→";
    private static final String ARROW_UP = "↑";
    private static final String BULLET = "●";
    private static final String CIRCLE = "○";
    private static final String SQUARE = "■";
    private static final String DIAMOND = "◆";
    private static final String STAR = "★";
    private static final String CHECK = "✓";
    private static final String CROSS_MARK = "✗";
    private static final String CLOCK = "⏱";
    private static final String LOCATION = "📍";

    /**
     * Limpia la pantalla de la consola.
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Mueve el cursor a una posición específica.
     */
    public static void moveCursor(int row, int col) {
        System.out.print(String.format("\033[%d;%dH", row, col));
    }

    /**
     * Muestra un banner animado de inicio.
     */
    public static void mostrarBannerAnimado() {
        clearScreen();
        String[] banner = {
                "╔══════════════════════════════════════════════════════════════════════════╗",
                "║                                                                          ║",
                "║   ███████╗███╗   ███╗███████╗██████╗  ██████╗ ███████╗███╗   ██╗ ██████╗║",
                "║   ██╔════╝████╗ ████║██╔════╝██╔══██╗██╔════╝ ██╔════╝████╗  ██║██╔════╝║",
                "║   █████╗  ██╔████╔██║█████╗  ██████╔╝██║  ███╗█████╗  ██╔██╗ ██║██║     ║",
                "║   ██╔══╝  ██║╚██╔╝██║██╔══╝  ██╔══██╗██║   ██║██╔══╝  ██║╚██╗██║██║     ║",
                "║   ███████╗██║ ╚═╝ ██║███████╗██║  ██║╚██████╔╝███████╗██║ ╚████║╚██████╗║",
                "║   ╚══════╝╚═╝     ╚═╝╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═══╝ ╚═════╝║",
                "║                                                                          ║",
                "║              🚑  SISTEMA DE GESTIÓN DE EMERGENCIAS  🚑                   ║",
                "║                        Version 2.0.0                                     ║",
                "║                                                                          ║",
                "╚══════════════════════════════════════════════════════════════════════════╝"
        };

        for (String line : banner) {
            System.out.println(AnsiColors.BRIGHT_CYAN + line + AnsiColors.RESET);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Crea un panel visual para mostrar el estado de recursos.
     */
    public static void mostrarPanelRecursos(List<Ambulancia> ambulancias,
            List<EquipoMedico> equipos,
            int casosEnCola,
            int casosCompletados) {
        System.out.println("\n" + AnsiColors.BRIGHT_WHITE + TOP_LEFT +
                repetir(HORIZONTAL, 76) + TOP_RIGHT + AnsiColors.RESET);
        System.out.println(AnsiColors.BRIGHT_WHITE + VERTICAL +
                centrar(" ESTADO DEL SISTEMA EN TIEMPO REAL ", 76) +
                VERTICAL + AnsiColors.RESET);
        System.out.println(AnsiColors.BRIGHT_WHITE + T_RIGHT +
                repetir(HORIZONTAL, 76) + T_LEFT + AnsiColors.RESET);

        // Sección de Ambulancias
        System.out.println(AnsiColors.BRIGHT_WHITE + VERTICAL +
                AnsiColors.BRIGHT_YELLOW + " 🚑 AMBULANCIAS " +
                AnsiColors.BRIGHT_WHITE + repetir(" ", 61) + VERTICAL + AnsiColors.RESET);

        for (Ambulancia amb : ambulancias) {
            String status = dibujarBarraEstado(amb);
            System.out.println(AnsiColors.BRIGHT_WHITE + VERTICAL + "  " + status +
                    repetir(" ", 74 - status.replaceAll("\u001B\\[[;\\d]*m", "").length()) +
                    VERTICAL + AnsiColors.RESET);
        }

        System.out.println(AnsiColors.BRIGHT_WHITE + T_RIGHT +
                repetir(HORIZONTAL, 76) + T_LEFT + AnsiColors.RESET);

        // Sección de Equipos Médicos
        System.out.println(AnsiColors.BRIGHT_WHITE + VERTICAL +
                AnsiColors.BRIGHT_GREEN + " ⚕️  EQUIPOS MÉDICOS " +
                AnsiColors.BRIGHT_WHITE + repetir(" ", 56) + VERTICAL + AnsiColors.RESET);

        for (EquipoMedico eq : equipos) {
            String status = dibujarBarraEstadoEquipo(eq);
            System.out.println(AnsiColors.BRIGHT_WHITE + VERTICAL + "  " + status +
                    repetir(" ", 74 - status.replaceAll("\u001B\\[[;\\d]*m", "").length()) +
                    VERTICAL + AnsiColors.RESET);
        }

        System.out.println(AnsiColors.BRIGHT_WHITE + T_RIGHT +
                repetir(HORIZONTAL, 76) + T_LEFT + AnsiColors.RESET);

        // Estadísticas
        String stats = String.format(" 📊 Cola: %s%d%s | Completados: %s%d%s ",
                AnsiColors.BRIGHT_YELLOW, casosEnCola, AnsiColors.RESET,
                AnsiColors.BRIGHT_GREEN, casosCompletados, AnsiColors.RESET);
        System.out.println(AnsiColors.BRIGHT_WHITE + VERTICAL + stats +
                repetir(" ", 76 - stats.replaceAll("\u001B\\[[;\\d]*m", "").length()) +
                VERTICAL + AnsiColors.RESET);

        System.out.println(AnsiColors.BRIGHT_WHITE + BOTTOM_LEFT +
                repetir(HORIZONTAL, 76) + BOTTOM_RIGHT + AnsiColors.RESET);
    }

    /**
     * Dibuja una barra de estado visual para una ambulancia.
     */
    private static String dibujarBarraEstado(Ambulancia ambulancia) {
        String id = String.format("AMB-%d", ambulancia.getIdAmbulancia());
        String statusText;
        String color;
        String icon;

        switch (ambulancia.getStatusAmbulancia()) {
            case DISPONIBLE -> {
                statusText = "DISPONIBLE";
                color = AnsiColors.BRIGHT_GREEN;
                icon = "○";
            }
            case EN_RUTA -> {
                statusText = "EN RUTA   ";
                color = AnsiColors.BRIGHT_YELLOW;
                icon = "→";
            }
            case OCUPADA -> {
                statusText = "OCUPADA   ";
                color = AnsiColors.BRIGHT_RED;
                icon = "●";
            }
            case RETORNANDO -> {
                statusText = "RETORNANDO";
                color = AnsiColors.BRIGHT_CYAN;
                icon = "←";
            }
            default -> {
                statusText = "DESCONOCIDO";
                color = AnsiColors.WHITE;
                icon = "?";
            }
        }

        long casoId = ambulancia.getCasoActualId();
        String casoInfo = (casoId != -1) ? String.format("Caso #%d", casoId) : "";

        return String.format("%s%s %s [%s%s%s] %s",
                color, icon, id, color, statusText, AnsiColors.RESET, casoInfo);
    }

    /**
     * Dibuja una barra de estado visual para un equipo médico.
     */
    private static String dibujarBarraEstadoEquipo(EquipoMedico equipo) {
        String id = String.format("EQM-%d", equipo.getIdEquipo());
        String statusText;
        String color;
        String icon;

        switch (equipo.getStatusEquipo()) {
            case DISPONIBLE -> {
                statusText = "DISPONIBLE";
                color = AnsiColors.BRIGHT_GREEN;
                icon = "○";
            }
            case ASIGNADO -> {
                statusText = "ASIGNADO  ";
                color = AnsiColors.BRIGHT_YELLOW;
                icon = "→";
            }
            case OCUPADO -> {
                statusText = "OCUPADO   ";
                color = AnsiColors.BRIGHT_RED;
                icon = "●";
            }
            case RETORNANDO -> {
                statusText = "RETORNANDO";
                color = AnsiColors.BRIGHT_CYAN;
                icon = "←";
            }
            default -> {
                statusText = "DESCONOCIDO";
                color = AnsiColors.WHITE;
                icon = "?";
            }
        }

        return String.format("%s%s %s [%s%s%s]",
                color, icon, id, color, statusText, AnsiColors.RESET);
    }

    /**
     * Muestra un evento con animación y color.
     */
    public static void mostrarEvento(String tipo, String mensaje, String severidad) {
        String icon = switch (tipo.toUpperCase()) {
            case "NUEVO" -> "📞";
            case "ASIGNADO" -> "✓";
            case "COMPLETADO" -> "✔";
            case "ALERTA" -> "⚠";
            case "ERROR" -> "✗";
            default -> "•";
        };

        String color = switch (tipo.toUpperCase()) {
            case "NUEVO" -> AnsiColors.BRIGHT_CYAN;
            case "ASIGNADO" -> AnsiColors.BRIGHT_YELLOW;
            case "COMPLETADO" -> AnsiColors.BRIGHT_GREEN;
            case "ALERTA" -> AnsiColors.BRIGHT_MAGENTA;
            case "ERROR" -> AnsiColors.BRIGHT_RED;
            default -> AnsiColors.WHITE;
        };

        String severityColor = severidad != null ? AnsiColors.colorBySeverity(severidad, severidad) : "";

        System.out.println(String.format("%s%s %s%s %s",
                color, icon, mensaje, severityColor, AnsiColors.RESET));
    }

    /**
     * Muestra una barra de progreso animada.
     */
    public static void mostrarBarraProgreso(int actual, int total, String etiqueta) {
        int ancho = 50;
        int progreso = (int) ((double) actual / total * ancho);

        StringBuilder barra = new StringBuilder();
        barra.append("[");
        for (int i = 0; i < ancho; i++) {
            if (i < progreso) {
                barra.append(AnsiColors.BRIGHT_GREEN).append("█").append(AnsiColors.RESET);
            } else {
                barra.append(AnsiColors.DIM).append("░").append(AnsiColors.RESET);
            }
        }
        barra.append("]");

        System.out.printf("%s %s %d/%d (%.1f%%)\r",
                etiqueta, barra, actual, total, ((double) actual / total * 100));
    }

    /**
     * Centra un texto en un ancho dado.
     */
    private static String centrar(String text, int width) {
        int padding = (width - text.length()) / 2;
        return repetir(" ", padding) + text + repetir(" ", width - text.length() - padding);
    }

    /**
     * Repite un string n veces.
     */
    private static String repetir(String str, int times) {
        return str.repeat(Math.max(0, times));
    }

    /**
     * Muestra un mensaje de loading con animación.
     */
    public static void mostrarLoading(String mensaje) {
        String[] frames = { "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏" };
        for (String frame : frames) {
            System.out.print("\r" + AnsiColors.BRIGHT_CYAN + frame + " " +
                    mensaje + AnsiColors.RESET);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println();
    }
}
