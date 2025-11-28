package org.iudigital.emergencias;

import org.iudigital.emergencias.CasoEmergencia.Severity;

public interface Recursos {

    enum Status { DISPONIBLE, EN_RUTA, OCUPADO, RETORNANDO, NO_DISPONIBLE }

    String getIdAmbulancia();

    void setStatusAmbulancia(Ambulancia.StatusAmbulancia nuevoStatus);

    void asingnacionCaso(CasoEmergencia casoEmergencia);

    void casoCompletado();

    String getLugar();

    void run();

}

