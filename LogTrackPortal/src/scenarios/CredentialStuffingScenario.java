package scenarios;

import domain.SecurityEvent;
import domain.Severity;
import events.EventBus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Escenario de ataque de Credential Stuffing.
 * Genera múltiples intentos de login fallidos consecutivos.
 */
public class CredentialStuffingScenario implements AttackScenario {

    @Override
    public void ejecutar(EventBus bus, String actor) {
        for (int i = 0; i < 10; i++) {
            SecurityEvent evento = new SecurityEvent(
                    LocalDateTime.now(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    "SISTEMA_LOGIN",
                    actor,
                    "LOGIN",
                    "CUENTA_USUARIO",
                    "FALLO",
                    "Credenciales incorrectas",
                    Severity.ALTO,
                    "192.168.1." + (i % 255)
            );
            bus.publicar(evento);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
