package scenarios;

import domain.SecurityEvent;
import domain.Severity;
import events.EventBus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Escenario de ataque de Escalada de Privilegios.
 * Intenta cambiar el rol de un usuario a ADMIN.
 */
public class PrivilegeEscalationScenario implements AttackScenario {

    @Override
    public void ejecutar(EventBus bus, String actor) {
        SecurityEvent evento = new SecurityEvent(
                LocalDateTime.now(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "SISTEMA_AUTORIZACION",
                actor,
                "CAMBIO_ROL",
                "rol propio a ADMIN",
                "EXITO",
                "Explotación de vulnerabilidad de autorización",
                Severity.CRITICO,
                "10.0.0.50"
        );
        bus.publicar(evento);
    }
}
