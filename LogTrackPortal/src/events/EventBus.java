package events;

import domain.SecurityEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que actúa como bus de eventos, permitiendo suscribir escuchadores
 * y notificarles cuando ocurren eventos de seguridad.
 */
public class EventBus {
    // Lista de escuchadores suscritos
    private final List<EventListener> listeners;

    /**
     * Constructor inicializa la lista de escuchadores.
     */
    public EventBus() {
        this.listeners = new ArrayList<>();
    }

    /**
     * Suscribe un escuchador al bus de eventos.
     * @param listener El escuchador a suscribir.
     */
    public void suscribir(EventListener listener) {
        listeners.add(listener);
    }

    /**
     * Publica un evento a todos los escuchadores suscritos.
     * @param evento El evento de seguridad a publicar.
     */
    public void publicar(SecurityEvent evento) {
        for (EventListener listener : listeners) {
            listener.onEvent(evento);
        }
    }
}
