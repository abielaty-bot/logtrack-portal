package events;

import domain.SecurityEvent;

/**
 * Interfaz que define un escuchador de eventos de seguridad.
 * Los implementadores recibirán notificaciones cuando ocurra un evento.
 */
public interface EventListener {
    /**
     * Método llamado cuando ocurre un evento de seguridad.
     * @param evento El evento de seguridad ocurrido.
     */
    void onEvent(SecurityEvent evento);
}
