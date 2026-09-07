package logging;

import domain.SecurityEvent;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Implementación vulnerable de la estrategia de logging.
 * Escribe los eventos en un archivo de texto plano sin estructura definida,
 * sin neutralizar caracteres especiales, lo que permite inyección de líneas
 * y corrupción del formato del log.
 */
public class VulnerableLoggingStrategy implements LoggingStrategy {
    
    private static final String ARCHIVO_LOG = "log_vulnerable.txt";
    
    /**
     * Registra un evento de seguridad escribiendo solo algunos campos
     * en texto plano, sin sanitizar los datos.
     * 
     * @param evento El evento de seguridad a registrar.
     */
    @Override
    public void registrar(SecurityEvent evento) {
        try (FileWriter writer = new FileWriter(ARCHIVO_LOG, true)) {
            // Escribe solo actor y action en texto plano, sin estructura
            // Si el campo actor contiene saltos de línea, se escriben tal cual
            // rompiendo el formato del archivo de log
            writer.write(evento.getActor());
            writer.write(" - ");
            writer.write(evento.getAction());
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Error al escribir en el log vulnerable: " + e.getMessage());
        }
    }
}
