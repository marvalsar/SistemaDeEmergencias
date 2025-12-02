package org.iudigital.emergencias.ui.model;

/**
 * Configuración de la simulación seleccionada por el usuario.
 */
public class SimulacionConfig {

    private int numAmbulancias;
    private int numEquiposMedicos;
    private int numOperadores;
    private int duracionSegundos;
    private boolean modoTurbo;

    public SimulacionConfig() {
        // Valores por defecto
        this.numAmbulancias = 4;
        this.numEquiposMedicos = 2;
        this.numOperadores = 2;
        this.duracionSegundos = 60;
        this.modoTurbo = false;
    }

    // Getters y Setters
    public int getNumAmbulancias() {
        return numAmbulancias;
    }

    public void setNumAmbulancias(int numAmbulancias) {
        this.numAmbulancias = numAmbulancias;
    }

    public int getNumEquiposMedicos() {
        return numEquiposMedicos;
    }

    public void setNumEquiposMedicos(int numEquiposMedicos) {
        this.numEquiposMedicos = numEquiposMedicos;
    }

    public int getNumOperadores() {
        return numOperadores;
    }

    public void setNumOperadores(int numOperadores) {
        this.numOperadores = numOperadores;
    }

    public int getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(int duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }

    public boolean isModoTurbo() {
        return modoTurbo;
    }

    public void setModoTurbo(boolean modoTurbo) {
        this.modoTurbo = modoTurbo;
    }
}
