package logging;

import domain.SecurityEvent;

/**
 * Interfaz que define la estrategia para registrar eventos de seguridad.
 * Implementa el patrón Strategy para permitir diferentes formas de logging.
 */
public interface LoggingStrategy {
    
    /**
     * Registra un evento de seguridad según la estrategia implementada.
     * 
     * @param evento El evento de seguridad a registrar.
     */
    void registrar(SecurityEvent evento);
}
