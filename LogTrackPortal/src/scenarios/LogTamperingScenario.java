package scenarios;

import domain.SecurityEvent;
import domain.Severity;
import events.EventBus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Escenario de ataque de Manipulación de Logs (Log Tampering).
 * Intenta inyectar caracteres especiales en los logs y detener el logger.
 */
public class LogTamperingScenario implements AttackScenario {

    @Override
    public void ejecutar(EventBus bus, String actor) {
        // Evento con salto de línea en el actor para intentar romper el formato del log
        SecurityEvent eventoInyeccion = new SecurityEvent(
                LocalDateTime.now(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "SISTEMA_AUDITORIA",
                actor + "\nLINEA_INYECTADA_FALSA",
                "ACCESO_NO_AUTORIZADO",
                "ARCHIVO_CONFIG",
                "EXITO",
                "Intento de inyección en logs",
                Severity.ALTO,
                "172.16.0.99"
        );
        bus.publicar(eventoInyeccion);

        // Evento para intentar detener el logger
        SecurityEvent eventoDetener = new SecurityEvent(
                LocalDateTime.now(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "SISTEMA_LOGGER",
                actor,
                "DETENER_LOGGER",
                "SERVICIO_LOG",
                "EXITO",
                "Intento de detención del servicio de logging",
                Severity.CRITICO,
                "172.16.0.99"
        );
        bus.publicar(eventoDetener);
    }
}
