package ui;

/**
 * Utilidades para la interfaz de consola con colores ANSI y banners.
 */
public class ConsoleUI {
    // Códigos de color ANSI
    public static final String VERDE = "\u001B[32m";
    public static final String ROJO = "\u001B[31m";
    public static final String AMARILLO = "\u001B[33m";
    public static final String CIAN = "\u001B[36m";
    public static final String RESET = "\u001B[0m";

    /**
     * Imprime el banner ASCII de "LogTrack Portal".
     */
    public static void mostrarBanner() {
        System.out.println(CIAN);
        System.out.println("  _                                     ____             _   ");
        System.out.println(" | |    ___  __ _  __ _ _   _  ___ _ __|  _ \\ __ _ _ __ | |_ ");
        System.out.println(" | |   / _ \\/ _` |/ _` | | | |/ _ \\ '__| |_) / _` | '_ \\| __|");
        System.out.println(" | |__|  __/ (_| | (_| | |_| |  __/ |  |  __/ (_| | | | | |_ ");
        System.out.println(" |_____\\___|\\__,_|\\__, |\\__,_|\\___|_|  |_|   \\__,_|_| |_|\\__|");
        System.out.println("                  |___/                                      ");
        System.out.println(RESET);
        System.out.println(AMARILLO + "Sistema de Detección y Registro de Eventos de Seguridad" + RESET);
        System.out.println();
    }

    /**
     * Imprime un menú con bordes decorativos.
     *
     * @param titulo El título del menú.
     * @param opciones Las opciones del menú.
     */
    public static void imprimirMenu(String titulo, String[] opciones) {
        int ancho = 40;
        StringBuilder linea = new StringBuilder("+");
        for (int i = 0; i < ancho - 2; i++) {
            linea.append("-");
        }
        linea.append("+");

        System.out.println(linea.toString());
        System.out.println("| " + centrar(titulo, ancho - 4) + " |");
        System.out.println(linea.toString());

        for (String opcion : opciones) {
            System.out.println("| " + opcion + espacios(ancho - 3 - opcion.length()) + "|");
        }

        System.out.println(linea.toString());
    }

    /**
     * Centra un texto dentro de un ancho dado.
     */
    private static String centrar(String texto, int ancho) {
        int espaciosIzquierda = (ancho - texto.length()) / 2;
        int espaciosDerecha = ancho - espaciosIzquierda - texto.length();
        return espacios(espaciosIzquierda) + texto + espacios(espaciosDerecha);
    }

    /**
     * Genera una cadena de espacios.
     */
    private static String espacios(int cantidad) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cantidad; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
