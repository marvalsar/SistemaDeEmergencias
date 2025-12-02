package org.iudigital.emergencias.domain;

import org.iudigital.emergencias.worker.Stoppable;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Ambulancia implements Runnable, Stoppable {

    private final List<CasoEmergencia> casosCompletados;

    public enum StatusAmbulancia {
        DISPONIBLE,
        EN_RUTA,
        OCUPADA,
        RETORNANDO
    }

    private final int idAmbulancia;
    private volatile StatusAmbulancia statusAmbulancia;
    private CasoEmergencia casoEmergencia;
    private volatile boolean corriendo = true;

    public Ambulancia(int idAmbulancia, List<CasoEmergencia> casosCompletados) {
        this.idAmbulancia = idAmbulancia;
        this.statusAmbulancia = StatusAmbulancia.DISPONIBLE;
        this.casosCompletados = casosCompletados;
    }

    @Override
    public void run() {
        // Nombrar el hilo según la ambulancia
        Thread.currentThread().setName("Ambulancia-" + idAmbulancia);

        try {
            while (corriendo && !Thread.currentThread().isInterrupted()) {
                if (statusAmbulancia == StatusAmbulancia.EN_RUTA) {
                    System.out.println("Ambulancia " + idAmbulancia + " en ruta a " + casoEmergencia.getLugar());
                    TimeUnit.SECONDS.sleep(3);

                    this.statusAmbulancia = StatusAmbulancia.OCUPADA;
                    System.out.println("Ambulancia " + idAmbulancia + " ha llegado. Atendiendo caso #"
                            + casoEmergencia.getCasoId());

                } else if (statusAmbulancia == StatusAmbulancia.OCUPADA) {
                    TimeUnit.SECONDS.sleep(5 + (long) (Math.random() * 5));

                    System.out.println("Ambulancia " + idAmbulancia + " finaliza atención y regresa");
                    this.statusAmbulancia = StatusAmbulancia.RETORNANDO;

                } else if (statusAmbulancia == StatusAmbulancia.RETORNANDO) {
                    TimeUnit.SECONDS.sleep(3);

                    if (casoEmergencia != null) {
                        casoEmergencia.setHoraAtendido(System.currentTimeMillis());

                        synchronized (casosCompletados) {
                            casosCompletados.add(casoEmergencia);
                        }
                    }
                    this.statusAmbulancia = StatusAmbulancia.DISPONIBLE;
                    this.casoEmergencia = null;
                    System.out.println("Ambulancia " + idAmbulancia + " ahora DISPONIBLE");

                } else {
                    TimeUnit.MILLISECONDS.sleep(100);
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Ambulancia " + idAmbulancia + " fue interrumpida.");
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void setOcupada(CasoEmergencia casoEmergencia) {
        this.casoEmergencia = casoEmergencia;
        this.statusAmbulancia = StatusAmbulancia.EN_RUTA;
    }

    public synchronized void setEnRuta() {
        this.statusAmbulancia = StatusAmbulancia.EN_RUTA;
    }

    public int getIdAmbulancia() {
        return idAmbulancia;
    }

    public synchronized StatusAmbulancia getStatusAmbulancia() {
        return statusAmbulancia;
    }

    public long getCasoActualId() {
        return (casoEmergencia != null) ? casoEmergencia.getCasoId() : -1;
    }

    @Override
    public void stop() {
        this.corriendo = false;
        Thread.currentThread().interrupt();
    }
}
