package metrics;

import domain.SecurityEvent;
import domain.Severity;
import events.EventListener;

/**
 * Motor de métricas que cuenta eventos y alertas, y calcula tiempos de detección.
 */
public class MetricsEngine implements EventListener {
    private int totalEventos;
    private int alertasDisparadas;
    private long tiempoInicio;
    private long tiempoFin;
    private boolean ejecutando;

    /**
     * Constructor que inicializa los contadores.
     */
    public MetricsEngine() {
        this.totalEventos = 0;
        this.alertasDisparadas = 0;
        this.ejecutando = false;
    }

    /**
     * Inicia el cronómetro para medir el tiempo de ejecución.
     */
    public void iniciarMedicion() {
        this.tiempoInicio = System.currentTimeMillis();
        this.ejecutando = true;
    }

    /**
     * Detiene el cronómetro y calcula el tiempo transcurrido.
     */
    public void detenerMedicion() {
        if (this.ejecutando) {
            this.tiempoFin = System.currentTimeMillis();
            this.ejecutando = false;
        }
    }

    /**
     * Registra un evento y actualiza las métricas.
     *
     * @param evento El evento de seguridad procesado.
     */
    @Override
    public void onEvent(SecurityEvent evento) {
        this.totalEventos++;
        // Simulación básica: si la severidad es CRITICO o ALTO, contamos como alerta potencial
        if (evento.getSeverity() == Severity.CRITICO || evento.getSeverity() == Severity.ALTO) {
            this.alertasDisparadas++;
        }
    }

    /**
     * Imprime un panel de resumen con bordes ASCII.
     */
    public void imprimirResumen() {
        long tiempoTranscurrido = 0;
        if (!ejecutando && tiempoInicio > 0) {
            tiempoTranscurrido = (tiempoFin - tiempoInicio) / 1000;
        }

        System.out.println("+----------------------------------+");
        System.out.println("|       PANEL DE MÉTRICAS          |");
        System.out.println("+----------------------------------+");
        System.out.println("| Eventos generados: " + String.format("%-17d", totalEventos) + " |");
        System.out.println("| Alertas disparadas: " + String.format("%-17d", alertasDisparadas) + " |");
        System.out.println("| Tiempo hasta detección: " + String.format("%-12d", tiempoTranscurrido) + " s |");
        System.out.println("+----------------------------------+");
    }

    /**
     * Reinicia todas las métricas a cero.
     */
    public void reiniciar() {
        this.totalEventos = 0;
        this.alertasDisparadas = 0;
        this.tiempoInicio = 0;
        this.tiempoFin = 0;
        this.ejecutando = false;
    }
}
