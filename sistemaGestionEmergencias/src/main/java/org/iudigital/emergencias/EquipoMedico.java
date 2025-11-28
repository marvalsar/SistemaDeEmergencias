package org.iudigital.emergencias;

import java.util.concurrent.TimeUnit;

public class EquipoMedico implements Runnable, Stoppable {

    public enum StatusEquipo {
        DISPONIBLE,
        ASIGNADO,
        OCUPADO,
        RETORNANDO
    }

    private final int idEquipo;
    private volatile StatusEquipo statusEquipo;
    private CasoEmergencia casoEmergencia;
    private volatile boolean corriendo = true;

    public EquipoMedico(int idEquipo) {
        this.idEquipo = idEquipo;
        this.statusEquipo = StatusEquipo.DISPONIBLE;
    }

    @Override
    public void run() {
        try {
            while (corriendo && !Thread.currentThread().isInterrupted()) {

                if (statusEquipo == StatusEquipo.ASIGNADO) {
                    System.out.printf("⚕️ Equipo %d: En camino a %s.\n", idEquipo, casoEmergencia.getLugar());
                    TimeUnit.SECONDS.sleep(2);
                    this.statusEquipo = StatusEquipo.OCUPADO;

                } else if (statusEquipo == StatusEquipo.OCUPADO) {
                    System.out.printf("⚕️ Equipo %d: Atendiendo caso #%d (Servicio especializado).\n",
                            idEquipo, casoEmergencia.getCasoId());

                    TimeUnit.SECONDS.sleep(4 + (long) (Math.random() * 4));

                    System.out.printf("⚕️ Equipo %d: Finaliza servicio y retorna.\n", idEquipo);
                    this.statusEquipo = StatusEquipo.RETORNANDO;

                } else if (statusEquipo == StatusEquipo.RETORNANDO) {
                    TimeUnit.SECONDS.sleep(2);

                    this.statusEquipo = StatusEquipo.DISPONIBLE;
                    this.casoEmergencia = null;
                    System.out.printf("⚕️ Equipo %d: ahora DISPONIBLE.\n", idEquipo);

                } else {

                    TimeUnit.MILLISECONDS.sleep(100);
                }
            }
        } catch (InterruptedException e) {
            System.out.printf("⚕️ Equipo %d interrumpido y detenido.\n", idEquipo);
        }
    }

    public synchronized void asignarCaso(CasoEmergencia casoEmergencia) {
        this.casoEmergencia = casoEmergencia;
        this.statusEquipo = StatusEquipo.ASIGNADO;
    }

    public synchronized StatusEquipo getStatusEquipo() {
        return statusEquipo;
    }

    public int getIdEquipo() {
        return idEquipo;
    }

    @Override
    public void stop() {
        this.corriendo = false;
        Thread.currentThread().interrupt();
    }
}
