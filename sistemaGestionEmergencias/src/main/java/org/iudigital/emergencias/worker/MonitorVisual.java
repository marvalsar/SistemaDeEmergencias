package org.iudigital.emergencias.worker;

import org.iudigital.emergencias.domain.Ambulancia;
import org.iudigital.emergencias.domain.CasoEmergencia;
import org.iudigital.emergencias.domain.EquipoMedico;
import org.iudigital.emergencias.util.ConsoleUI;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Monitor visual mejorado que muestra el estado del sistema en tiempo real
 * con gráficos avanzados en consola.
 */
public class MonitorVisual implements Runnable, Stoppable {

    private final List<Ambulancia> ambulancias;
    private final List<EquipoMedico> equiposMedicos;
    private final List<CasoEmergencia> casosCompletados;
    private final BlockingQueue<CasoEmergencia> colaCasos;
    private volatile boolean corriendo = true;
    private final int intervaloSegundos;

    public MonitorVisual(List<Ambulancia> ambulancias,
            List<EquipoMedico> equiposMedicos,
            List<CasoEmergencia> casosCompletados,
            BlockingQueue<CasoEmergencia> colaCasos,
            int intervaloSegundos) {
        this.ambulancias = ambulancias;
        this.equiposMedicos = equiposMedicos;
        this.casosCompletados = casosCompletados;
        this.colaCasos = colaCasos;
        this.intervaloSegundos = intervaloSegundos;
    }

    @Override
    public void run() {
        // Nombrar el hilo del monitor visual
        Thread.currentThread().setName("Monitor-Visual");

        try {
            while (corriendo) {
                TimeUnit.SECONDS.sleep(intervaloSegundos);

                if (!corriendo)
                    break;

                mostrarEstadoVisual();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Muestra el estado visual completo del sistema.
     */
    private void mostrarEstadoVisual() {
        int casosEnCola = colaCasos.size();
        int completados = casosCompletados.size();

        // Separador visual
        System.out.println("\n" + "═".repeat(80));

        // Mostrar panel de recursos con gráficos
        ConsoleUI.mostrarPanelRecursos(ambulancias, equiposMedicos, casosEnCola, completados);

        System.out.println("═".repeat(80) + "\n");
    }

    @Override
    public void stop() {
        this.corriendo = false;
    }
}
