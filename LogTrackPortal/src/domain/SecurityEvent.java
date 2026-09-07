package domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Clase inmutable que representa un evento de seguridad en el sistema LogTrack Portal.
 */
public class SecurityEvent {
    private final LocalDateTime timestamp;
    private final String eventId;
    private final String interactionId;
    private final String source;
    private final String actor;
    private final String action;
    private final String object;
    private final String outcome;
    private final String reason;
    private final Severity severity;
    private final String technicalOrigin;

    /**
     * Constructor de la clase SecurityEvent.
     *
     * @param timestamp        Marca de tiempo del evento.
     * @param eventId          Identificador único del evento.
     * @param interactionId    Identificador de la interacción asociada.
     * @param source           Fuente del evento.
     * @param actor            Actor que realizó la acción.
     * @param action           Acción realizada.
     * @param object           Objeto sobre el cual se realizó la acción.
     * @param outcome          Resultado de la acción.
     * @param reason           Razón o justificación de la acción.
     * @param severity         Nivel de severidad del evento.
     * @param technicalOrigin  Origen técnico del evento (ej. IP, servicio, etc.).
     */
    public SecurityEvent(LocalDateTime timestamp, String eventId, String interactionId,
                         String source, String actor, String action, String object,
                         String outcome, String reason, Severity severity, String technicalOrigin) {
        this.timestamp = timestamp;
        this.eventId = eventId;
        this.interactionId = interactionId;
        this.source = source;
        this.actor = actor;
        this.action = action;
        this.object = object;
        this.outcome = outcome;
        this.reason = reason;
        this.severity = severity;
        this.technicalOrigin = technicalOrigin;
    }

    // Getters (no hay setters porque es inmutable)

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public String getInteractionId() {
        return interactionId;
    }

    public String getSource() {
        return source;
    }

    public String getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public String getObject() {
        return object;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getReason() {
        return reason;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getTechnicalOrigin() {
        return technicalOrigin;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SecurityEvent that = (SecurityEvent) obj;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "SecurityEvent{" +
                "timestamp=" + timestamp +
                ", eventId='" + eventId + '\'' +
                ", severity=" + severity +
                '}';
    }
}
