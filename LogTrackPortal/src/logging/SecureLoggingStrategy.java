package logging;

import domain.SecurityEvent;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Implementación segura de la estrategia de logging.
 * Escribe los eventos en un archivo con formato estructurado y neutraliza
 * cualquier carácter de control o salto de línea en los campos de texto
 * para evitar inyección en el log.
 */
public class SecureLoggingStrategy implements LoggingStrategy {
    
    private static final String ARCHIVO_LOG = "log_remediado.txt";
    
    /**
     * Registra un evento de seguridad escribiendo los campos en formato estructurado.
     * Antes de escribir, sanitiza los campos de texto reemplazando caracteres especiales.
     * 
     * @param evento El evento de seguridad a registrar.
     */
    @Override
    public void registrar(SecurityEvent evento) {
        try (FileWriter writer = new FileWriter(ARCHIVO_LOG, true)) {
            // Neutralizar caracteres de control y saltos de línea en cada campo
            String actorLimpio = neutralizarTexto(evento.getActor());
            String accionLimpia = neutralizarTexto(evento.getAction());
            String resultadoLimpio = neutralizarTexto(evento.getOutcome());
            String origenLimpio = neutralizarTexto(evento.getTechnicalOrigin());
            String severidadLimpia = neutralizarTexto(evento.getSeverity().name());
            
            // Formato estructurado: [timestamp] | usuario=actor | accion=action | resultado=outcome | origen=technicalOrigin | severidad=severity
            String lineaLog = String.format("[%s] | usuario=%s | accion=%s | resultado=%s | origen=%s | severidad=%s%n",
                    evento.getTimestamp(),
                    actorLimpio,
                    accionLimpia,
                    resultadoLimpio,
                    origenLimpio,
                    severidadLimpia);
            
            writer.write(lineaLog);
        } catch (IOException e) {
            System.err.println("Error al escribir en el log seguro: " + e.getMessage());
        }
    }
    
    /**
     * Neutraliza caracteres de control y saltos de línea en un texto.
     * Reemplaza \\n, \\r, \\t y otros caracteres de control por espacios.
     * 
     * @param texto El texto a sanitizar.
     * @return El texto con caracteres de control reemplazados por espacios.
     */
    private String neutralizarTexto(String texto) {
        if (texto == null) {
            return "null";
        }
        // Reemplazar saltos de línea y caracteres de control por espacios
        return texto.replaceAll("[\\r\\n\\t]", " ")
                    .replaceAll("\\p{Cntrl}", " ");
    }
}
