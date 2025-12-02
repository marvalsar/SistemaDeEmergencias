package org.iudigital.emergencias.worker;

import org.iudigital.emergencias.domain.CasoEmergencia;
import org.iudigital.emergencias.domain.CasoEmergencia.Severity;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class OperadorLlamadas implements Runnable, Stoppable {

    private final BlockingQueue<CasoEmergencia> emergencias;
    private final String idOperador;
    private volatile boolean corriendo = true;

    public OperadorLlamadas(BlockingQueue<CasoEmergencia> emergencias, String idOperador) {
        this.emergencias = emergencias;
        this.idOperador = idOperador;
    }

    @Override
    public void run() {
        // Nombrar el hilo según el operador
        Thread.currentThread().setName("Operador-" + idOperador);

        try {
            while (corriendo) {
                TimeUnit.MILLISECONDS.sleep(500 + (long) (Math.random() * 1500));

                Severity ramdonSeverity = getRamdonSeverity();
                String lugar = "Lugar- " + (int) (Math.random() * 50);

                CasoEmergencia nuevoCaso = new CasoEmergencia(ramdonSeverity, lugar);

                emergencias.put(nuevoCaso);

                System.out.printf(" %s Recibe llamadas #%d: %s en %s. Pendientes: %d\n",
                        idOperador,
                        nuevoCaso.getCasoId(),
                        ramdonSeverity,
                        lugar,
                        emergencias.size());

            }
        } catch (InterruptedException e) {
            System.out.println(idOperador + "interrumpido y detenido.");
            Thread.currentThread().interrupt();
        }
    }

    private CasoEmergencia.Severity getRamdonSeverity() {
        double r = Math.random();
        if (r < 0.2) {
            return CasoEmergencia.Severity.CRITICO;
        } else if (r < 0.5) {
            return CasoEmergencia.Severity.GRAVE;
        } else if (r < 0.8) {
            return CasoEmergencia.Severity.MODERADO;
        } else {
            return CasoEmergencia.Severity.LEVE;
        }
    }

    @Override
    public void stop() {
        this.corriendo = false;
    }
}
