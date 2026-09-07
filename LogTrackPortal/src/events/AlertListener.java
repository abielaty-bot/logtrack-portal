package events;

import domain.SecurityEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Escuchador que monitorea intentos fallidos de usuarios y genera alertas
 * cuando se detectan 3 o más intentos fallidos consecutivos.
 */
public class AlertListener implements EventListener {
    // Mapa que almacena el contador de intentos fallidos por usuario
    private final Map<String, Integer> intentosFallidosPorUsuario;

    /**
     * Constructor inicializa el mapa de contadores.
     */
    public AlertListener() {
        this.intentosFallidosPorUsuario = new HashMap<>();
    }

    /**
     * Procesa un evento de seguridad.
     * - Si el outcome es "FALLO", incrementa el contador del usuario.
     * - Si el outcome es "EXITO" y la acción es "LOGIN", reinicia el contador.
     * - Si se alcanzan 3 intentos fallidos consecutivos, imprime una alerta en rojo.
     * @param evento El evento de seguridad a procesar.
     */
    @Override
    public void onEvent(SecurityEvent evento) {
        String actor = evento.getActor();
        String outcome = evento.getOutcome();
        String action = evento.getAction();

        // Si el evento es de LOGIN con EXITO, reiniciar contador
        if ("LOGIN".equals(action) && "EXITO".equals(outcome)) {
            intentosFallidosPorUsuario.put(actor, 0);
            return;
        }

        // Si el outcome es FALLO, incrementar contador
        if ("FALLO".equals(outcome)) {
            int contador = intentosFallidosPorUsuario.getOrDefault(actor, 0);
            contador++;
            intentosFallidosPorUsuario.put(actor, contador);

            // Verificar si se alcanzaron 3 intentos fallidos consecutivos
            if (contador >= 3) {
                // Imprimir alerta en color rojo usando código ANSI
                System.out.println("\u001B[31m[ALERTA] 3 intentos fallidos consecutivos para el usuario: " + actor + "\u001B[0m");
            }
        }
    }
}
