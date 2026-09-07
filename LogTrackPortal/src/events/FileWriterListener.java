package events;

import domain.SecurityEvent;
import logging.LoggingStrategy;

/**
 * Escuchador que delega la escritura de eventos a una estrategia de logging.
 * Utiliza el patrón Strategy para permitir diferentes formas de registrar eventos.
 */
public class FileWriterListener implements EventListener {
    // Estrategia de logging a utilizar
    private final LoggingStrategy loggingStrategy;

    /**
     * Constructor que recibe la estrategia de logging.
     * @param loggingStrategy La estrategia a usar para registrar eventos.
     */
    public FileWriterListener(LoggingStrategy loggingStrategy) {
        this.loggingStrategy = loggingStrategy;
    }

    /**
     * Procesa un evento delegando su registro a la estrategia configurada.
     * @param evento El evento de seguridad a registrar.
     */
    @Override
    public void onEvent(SecurityEvent evento) {
        loggingStrategy.registrar(evento);
    }
}
