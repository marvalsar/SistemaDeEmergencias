package concurrencias;

import org.iudigital.emergencias.Ambulancia;
import org.iudigital.emergencias.CasoEmergencia;
import org.iudigital.emergencias.EquipoMedico;
import org.iudigital.emergencias.Stoppable;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class Despachador implements Runnable, Stoppable {

    private final BlockingQueue<CasoEmergencia> casoEmergencias;
    private final List<Ambulancia> recursosPool;
    private final List<EquipoMedico> equipoMedicoPool;
    private final Lock recursosLock = new ReentrantLock();

    private volatile boolean corriendo = true;
    private Thread selfThread;

    public Despachador(BlockingQueue<CasoEmergencia> casoEmergencias, List<Ambulancia> recursosPool, List<EquipoMedico> equipoMedicoPool) {
        this.casoEmergencias = casoEmergencias;
        this.recursosPool = recursosPool;
        this.equipoMedicoPool = equipoMedicoPool;
    }

    @Override
    public void run() {
        selfThread = Thread.currentThread();
        try {
            while (corriendo) {
                CasoEmergencia concurrentCaso = casoEmergencias.take();

                if (!corriendo) return;

                recursosLock.lock();
                try {
                    Ambulancia ambulanciaAsignada = encontrarYAsignarAmbulancia(concurrentCaso);

                    if (ambulanciaAsignada != null) {

                        concurrentCaso.setRecursoAsignado(ambulanciaAsignada);
                        concurrentCaso.setHoraInicioServicio(System.currentTimeMillis());
                        ambulanciaAsignada.setOcupada(concurrentCaso);


                        if (concurrentCaso.getSeverity() == CasoEmergencia.Severity.CRITICO ||
                           concurrentCaso.getSeverity() == CasoEmergencia.Severity.GRAVE) {

                            EquipoMedico equipoAsignado = encontrarYAsignarEquipoMedico();

                            if (equipoAsignado != null) {
                                equipoAsignado.asignarCaso(concurrentCaso);
                                System.out.printf("   [ASIGNADO] Equipo %d asignado a caso #%d.\n",
                                        equipoAsignado.getIdEquipo(), concurrentCaso.getCasoId());
                            } else {
                                System.out.printf("   [ALERTA] No hay Equipo Médico disponible para caso #%d.\n",
                                        concurrentCaso.getCasoId());
                            }
                        }
                    } else {
                        casoEmergencias.put(concurrentCaso);
                        TimeUnit.MILLISECONDS.sleep(500);
                    }
                } finally {
                    recursosLock.unlock();
                }

            }
        } catch (InterruptedException e) {
            System.out.println("📢 Despachador interrumpido y detenido.");
        }
    }

    private Ambulancia encontrarYAsignarAmbulancia(CasoEmergencia casoRef) {
        for (Ambulancia ambulancia : recursosPool) {
            if (ambulancia.getStatusAmbulancia() == Ambulancia.StatusAmbulancia.DISPONIBLE) {
                return ambulancia;
            }
        }
        return null;
    }

    private EquipoMedico encontrarYAsignarEquipoMedico() {

        for (EquipoMedico equipo : equipoMedicoPool) {
            if (equipo.getStatusEquipo() == EquipoMedico.StatusEquipo.DISPONIBLE) {
                return equipo;
            }
        }
        return null;
    }

    public void stop () {
            this.corriendo = false;

            if (selfThread != null) {
                selfThread.interrupt();
            }
    }
}



