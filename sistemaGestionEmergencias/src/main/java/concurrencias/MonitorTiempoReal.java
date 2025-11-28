package concurrencias;

import org.iudigital.emergencias.Ambulancia;
import org.iudigital.emergencias.CasoEmergencia;
import org.iudigital.emergencias.Stoppable;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MonitorTiempoReal implements Runnable, Stoppable {

    private final BlockingQueue<CasoEmergencia> casoEmergencias;
    private final List<Ambulancia> recursosPool;
    private volatile boolean corriendo = true;

    public MonitorTiempoReal(BlockingQueue<CasoEmergencia> casoEmergencias, List<Ambulancia> ambulancias) {
        this.casoEmergencias = casoEmergencias;
        this.recursosPool = ambulancias;
    }

    @Override
    public void run() {
        try {
            while (!corriendo) {
                TimeUnit.SECONDS.sleep(2);

                printEmergencyStatus();

                printRecursosStatus();

                System.out.println("--------------------------------------------------------------------------");
            }
        } catch (InterruptedException e) {
            System.out.println("Monitor Interrumpido y detenido");
            Thread.currentThread().interrupt();
        }
    }

    private void printEmergencyStatus() {
        System.out.println("\n***MONITOR DE EMERGENCIAS***");
        int pendingCount = casoEmergencias.size();

        if (pendingCount == 0) {
            System.out.println("No hay emergencias pendientes en la cola.");
            return;
        }

        System.out.printf(" %d Emergencias Pendientes (La más alta prioridad primero):\n", pendingCount);

        casoEmergencias.stream().limit(5)
                .forEach(caso -> {
                    String assigned = (caso.getRecursoAsignado() != null) ?
                            String.valueOf(caso.getRecursoAsignado().getIdAmbulancia()) : "N/A";
                    System.out.printf("  - ID: %d | Prioridad: %s | Loc: %s | Asignado: %s\n",
                            caso.getCasoId(), caso.getSeverity(), caso.getLugar(), assigned);
                });
    }

    private void printRecursosStatus() {
        System.out.println("\n*** MONITOR DE RECURSOS ***");

        long available = recursosPool.stream()
                .filter(ambulancia -> ambulancia.getStatusAmbulancia() == Ambulancia.StatusAmbulancia.DISPONIBLE)
                .count();

        long occupied = recursosPool.stream()
                .filter(ambulancia -> ambulancia.getStatusAmbulancia() == Ambulancia.StatusAmbulancia.OCUPADA || ambulancia.getStatusAmbulancia() == Ambulancia.StatusAmbulancia.EN_RUTA)
                .count();

        System.out.printf("Sumario: Disponibles: %d | Ocupados/En Ruta: %d\n", available, occupied);


        recursosPool.forEach(ambulancia -> {
            String detail = (ambulancia.getStatusAmbulancia() == Ambulancia.StatusAmbulancia.OCUPADA) ?
                    " (Caso: " + ambulancia.getCasoActualId() + ")" : "";
            System.out.printf("  - %s | Estado: %s%s\n", ambulancia.getIdAmbulancia(), ambulancia.getStatusAmbulancia(), detail);
        });

    }

    public void stop() {
        this.corriendo = false;
    }
}
