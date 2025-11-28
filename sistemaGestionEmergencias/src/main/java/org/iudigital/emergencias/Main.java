package org.iudigital.emergencias;

import concurrencias.MonitorTiempoReal;
import concurrencias.OperadorLlamadas;
import concurrencias.Despachador;

import org.iudigital.emergencias.CasoEmergencia;
import org.iudigital.emergencias.Ambulancia;
import org.iudigital.emergencias.Stoppable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.Collections;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        List<Stoppable> todosLosComponentes = new ArrayList<>();

        BlockingQueue<CasoEmergencia> casoEmergencias = new PriorityBlockingQueue<>();
        List<Ambulancia> recursosPool = new ArrayList<>();

        List<EquipoMedico> equipoMedicoPool = new ArrayList<>();

        List<CasoEmergencia> casosCompletados = new ArrayList<>();

        int numAmbulancias = 3;
        for (int i = 1; i <= numAmbulancias; i++) {
            Ambulancia ambulancia = new Ambulancia(100 + i, casosCompletados);
            recursosPool.add(ambulancia);
            new Thread(ambulancia, "Ambulancia-" + i).start();
            todosLosComponentes.add(ambulancia);
        }
        System.out.println("🚑 " + recursosPool.size() + " Ambulancias lanzadas y disponibles.");

        int numEquiposMedicos = 2;
        for (int i = 1; i <= numEquiposMedicos; i++) {
            EquipoMedico equipo = new EquipoMedico(200 + i);
            equipoMedicoPool.add(equipo);
            new Thread(equipo, "EquipoMedico-" + i).start();
            todosLosComponentes.add(equipo);
        }
        System.out.println("⚕️ " + numEquiposMedicos + " Equipos Médicos especializados en línea.");

        int numOperadores = 2;
        for (int i = 1; i <= numOperadores; i++) {
            OperadorLlamadas operadorLlamadas = new OperadorLlamadas(casoEmergencias, "OP-" + i);
            new Thread(operadorLlamadas, "Operador-" + i).start();
            todosLosComponentes.add(operadorLlamadas);
        }
        System.out.println("📞 " + numOperadores + " Operadores listos para recibir llamadas.");

        Despachador despachador = new Despachador(casoEmergencias, recursosPool, equipoMedicoPool);
        new Thread(despachador, "Despachador 1").start();
        System.out.println("1 Despachador en línea para coordinar recursos.");
        todosLosComponentes.add(despachador);

        MonitorTiempoReal monitor = new MonitorTiempoReal(casoEmergencias, recursosPool);
        new Thread(monitor, "RealTimeMonitor").start();
        System.out.println("Monitor de Tiempo Real iniciado.");
        todosLosComponentes.add(monitor);

        System.out.println("\n---------SIMULACION INICIADA------------");

        final int DURACION_SEGUNDOS = 20;

        System.out.printf("\n⏳ Simulación en curso. Detención total programada en %d segundos...\n", DURACION_SEGUNDOS);

        TimeUnit.SECONDS.sleep(DURACION_SEGUNDOS);

        System.out.println("🛑 TIEMPO FINALIZADO. Deteniendo Operadores.");

        for (Stoppable componente : todosLosComponentes) {
            componente.stop();
        }

        System.out.println("✅ Todos los componentes han recibido la señal de parada. Finalizando simulación.");

        try {
            TimeUnit.MILLISECONDS.sleep(2000);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        printFinalSummary(casosCompletados, casoEmergencias, recursosPool);
    }

    private static void printFinalSummary(List<CasoEmergencia> resultados, BlockingQueue<CasoEmergencia> colaPendiente,
                                          List<Ambulancia> poolRecursos) {

        int casosEnCola = colaPendiente.size();
        int casosAsignados = 0;

        for (Ambulancia ambulancia : poolRecursos) {

            if (ambulancia.getStatusAmbulancia() != Ambulancia.StatusAmbulancia.DISPONIBLE) {
                casosAsignados++;
            }
        }
        int casosEnProceso = casosEnCola + casosAsignados;

        System.out.println("\n\n==========================================================================================");
        System.out.println("                         TABLA DE RESULTADOS DE LA SIMULACIÓN");
        System.out.println("==========================================================================================");

        System.out.printf("Total de Casos Atendidos: %d\n", resultados.size());
        System.out.printf("Total de Casos Pendientes (en cola): %d\n", casosEnCola);
        System.out.printf("Total de Casos Asignados (en ruta/ocupados): %d\n", casosAsignados);
        System.out.printf("Total de Casos EN PROCESO (Pendientes + Asignados): %d\n\n", casosEnProceso);

        if (resultados.isEmpty()) {
            System.out.println("No se lograron atender casos en el tiempo de simulación.");
            return;
        }

        System.out.printf("| %-5s | %-10s | %-12s | %-12s | %-12s |\n",
                "ID", "Severidad", "Tiempo Espera", "Tiempo Servicio", "Total (ms)");
        System.out.println("|-------|------------|--------------|---------------|-------------|");

        long totalEspera = 0;
        long totalServicio = 0;

        for (CasoEmergencia caso : resultados) {
            long espera = caso.getTiempoEsperaMs();
            long total = caso.getTiempoTotalServicioMs();
            long servicio = total - espera;

            System.out.printf("| %-5d | %-10s | %-12d | %-12d | %-12d |\n",
                    caso.getCasoId(),
                    caso.getSeverity().toString(),
                    espera,
                    servicio,
                    total);

            totalEspera += espera;
            totalServicio += total;
        }

        System.out.println("|-------|------------|--------------|---------------|-------------|");

        long avgEspera = totalEspera / resultados.size();
        long avgTotal = totalServicio / resultados.size();
        long avgServicio = avgTotal - avgEspera;

        System.out.printf("| %-5s | %-10s | %-12d | %-12d | %-12d |\n",
                "AVG",
                "---",
                avgEspera,
                avgServicio,
                avgTotal);
        System.out.println("==========================================================================================");
    }
}
