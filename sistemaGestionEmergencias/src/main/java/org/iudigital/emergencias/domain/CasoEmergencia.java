package org.iudigital.emergencias.domain;

import java.util.concurrent.atomic.AtomicLong;

public class CasoEmergencia implements Comparable<CasoEmergencia> {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1000);

    public enum Severity {
        CRITICO,
        GRAVE,
        MODERADO,
        LEVE
    }

    private final long casoId;
    private final Severity severidad;
    private final long horaRecibido;
    private final String lugar;
    private Ambulancia recursoAsignado;
    private long horaInicioServicio = 0;
    private long horaAtendido = 0;

    private static final double WG = 4.0;
    private static final double WT = 0.5;

    public CasoEmergencia(Severity severidad, String lugar) {
        this.casoId = ID_GENERATOR.getAndIncrement();
        this.severidad = severidad;
        this.horaRecibido = System.currentTimeMillis();
        this.lugar = lugar;
    }

    @Override
    public int compareTo(CasoEmergencia other) {
        double thisPriority = calculatePriorityScore();
        double otherPriority = other.calculatePriorityScore();

        if (thisPriority > otherPriority)
            return -1;
        if (thisPriority < otherPriority)
            return 1;
        return 0;
    }

    private double calculatePriorityScore() {
        long timeInQueue = (System.currentTimeMillis() - horaRecibido) / 1000;

        int severityValue = switch (severidad) {
            case CRITICO -> 4;
            case GRAVE -> 3;
            case MODERADO -> 2;
            case LEVE -> 1;
        };
        return (severityValue * WG) + (timeInQueue * WT);
    }

    // Getters
    public long getCasoId() {
        return casoId;
    }

    public Severity getSeveridad() {
        return severidad;
    }

    public long getHoraRecibido() {
        return horaRecibido;
    }

    public String getLugar() {
        return lugar;
    }

    public Ambulancia getRecursoAsignado() {
        return recursoAsignado;
    }

    public long getHoraInicioServicio() {
        return horaInicioServicio;
    }

    public long getHoraAtendido() {
        return horaAtendido;
    }

    // Setters
    public void setRecursoAsignado(Ambulancia recursoAsignado) {
        this.recursoAsignado = recursoAsignado;
    }

    public void setHoraInicioServicio(long horaInicioServicio) {
        this.horaInicioServicio = horaInicioServicio;
    }

    public void setHoraAtendido(long horaAtendido) {
        this.horaAtendido = horaAtendido;
    }

    // Métodos de cálculo
    public long getTiempoEsperaMs() {
        if (horaInicioServicio == 0)
            return 0;
        return horaInicioServicio - horaRecibido;
    }

    public long getTiempoTotalServicioMs() {
        if (horaAtendido == 0)
            return 0;
        return horaAtendido - horaRecibido;
    }
}
