import ui.ConsoleUI;
import ui.MenuController;

/**
 * Punto de entrada principal de la aplicación LogTrack Portal.
 */
public class Main {
    /**
     * Método principal que inicia la aplicación.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        // Mostrar banner de bienvenida
        ConsoleUI.mostrarBanner();

        // Iniciar el controlador del menú
        MenuController controller = new MenuController();
        controller.iniciar();
    }
}
