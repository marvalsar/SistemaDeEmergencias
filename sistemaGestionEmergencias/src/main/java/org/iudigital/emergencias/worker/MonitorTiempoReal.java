package org.iudigital.emergencias.worker;

import org.iudigital.emergencias.domain.Ambulancia;
import org.iudigital.emergencias.domain.CasoEmergencia;
import org.iudigital.emergencias.domain.EquipoMedico;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MonitorTiempoReal implements Runnable, Stoppable {

    private final List<Ambulancia> recursoPool;
    private final List<EquipoMedico> equipoMedicoPool;
    private final List<CasoEmergencia> casosCompletados;
    private volatile boolean corriendo = true;

    public MonitorTiempoReal(List<Ambulancia> recursoPool, List<EquipoMedico> equipoMedicoPool,
            List<CasoEmergencia> casosCompletados) {
        this.recursoPool = recursoPool;
        this.equipoMedicoPool = equipoMedicoPool;
        this.casosCompletados = casosCompletados;
    }

    @Override
    public void run() {
        // Nombrar el hilo del monitor
        Thread.currentThread().setName("Monitor-TiempoReal");

        try {
            while (corriendo) {
                TimeUnit.SECONDS.sleep(5);

                System.out.println("\n═══════════════════════════════════════════════════");
                System.out.println("📊 Monitoreo en Tiempo Real");
                System.out.println("═══════════════════════════════════════════════════");

                mostrarEstadoAmbulancia();
                mostrarEstadoEquipoMedico();
                mostrarResumenEstadistico();

                System.out.println("═══════════════════════════════════════════════════\n");
            }
        } catch (InterruptedException e) {
            System.out.println("📊 Monitor interrumpido y detenido.");
        }
    }

    private void mostrarEstadoAmbulancia() {
        System.out.println("\n🚑 Estado de Ambulancias:");
        int disponibles = 0;
        int ocupadas = 0;
        for (Ambulancia ambulancia : recursoPool) {
            Ambulancia.StatusAmbulancia status = ambulancia.getStatusAmbulancia();
            long casoId = ambulancia.getCasoActualId();

            if (status == Ambulancia.StatusAmbulancia.DISPONIBLE) {
                disponibles++;
            } else {
                ocupadas++;
            }

            System.out.printf("   Ambulancia %d: %s%s\n",
                    ambulancia.getIdAmbulancia(),
                    status,
                    (casoId != -1 ? " [Caso #" + casoId + "]" : ""));
        }
        System.out.printf("   Total: %d disponibles, %d ocupadas\n", disponibles, ocupadas);
    }

    private void mostrarEstadoEquipoMedico() {
        System.out.println("\n⚕️ Estado de Equipos Médicos:");
        int disponibles = 0;
        int ocupados = 0;
        for (EquipoMedico equipo : equipoMedicoPool) {
            EquipoMedico.StatusEquipo status = equipo.getStatusEquipo();

            if (status == EquipoMedico.StatusEquipo.DISPONIBLE) {
                disponibles++;
            } else {
                ocupados++;
            }

            System.out.printf("   Equipo Médico %d: %s\n",
                    equipo.getIdEquipo(),
                    status);
        }
        System.out.printf("   Total: %d disponibles, %d ocupados\n", disponibles, ocupados);
    }

    private void mostrarResumenEstadistico() {
        System.out.println("\n📈 Resumen Estadístico de Casos Completados:");

        synchronized (casosCompletados) {
            if (casosCompletados.isEmpty()) {
                System.out.println("   (No hay casos completados aún)");
                return;
            }

            long sumaEspera = 0;
            long sumaServicio = 0;

            for (CasoEmergencia caso : casosCompletados) {
                sumaEspera += caso.getTiempoEsperaMs();
                sumaServicio += caso.getTiempoTotalServicioMs();
            }

            int total = casosCompletados.size();
            double promedioEsperaSeg = (sumaEspera / 1000.0) / total;
            double promedioServicioSeg = (sumaServicio / 1000.0) / total;

            System.out.printf("   Casos completados: %d\n", total);
            System.out.printf("   Tiempo promedio de espera: %.2f s\n", promedioEsperaSeg);
            System.out.printf("   Tiempo promedio de servicio: %.2f s\n", promedioServicioSeg);
        }
    }

    @Override
    public void stop() {
        this.corriendo = false;
    }
}
