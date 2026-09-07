package ui;

import domain.SecurityEvent;
import domain.Severity;
import events.EventBus;
import events.AlertListener;
import events.FileWriterListener;
import logging.VulnerableLoggingStrategy;
import logging.SecureLoggingStrategy;
import metrics.MetricsEngine;
import scenarios.AttackScenario;
import scenarios.CredentialStuffingScenario;
import scenarios.PrivilegeEscalationScenario;
import scenarios.LogTamperingScenario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Controlador del menú principal y submenús de la aplicación.
 */
public class MenuController {
    private BufferedReader lector;
    private EventBus bus;
    private MetricsEngine metrics;
    private boolean vulnerable;

    /**
     * Constructor que inicializa el controlador.
     */
    public MenuController() {
        this.lector = new BufferedReader(new InputStreamReader(System.in));
        this.bus = new EventBus();
        this.metrics = new MetricsEngine();
        this.vulnerable = false;
    }

    /**
     * Inicia el bucle principal del menú.
     */
    public void iniciar() {
        boolean salir = false;

        while (!salir) {
            String[] opcionesPrincipal = {
                "1. Modo Vulnerable",
                "2. Modo Remediado",
                "3. Salir"
            };
            ConsoleUI.imprimirMenu("MENÚ PRINCIPAL", opcionesPrincipal);
            System.out.print("Seleccione una opción: ");

            String opcion = leerEntrada();

            switch (opcion) {
                case "1":
                    vulnerable = true;
                    mostrarSubMenu();
                    break;
                case "2":
                    vulnerable = false;
                    mostrarSubMenu();
                    break;
                case "3":
                    salir = true;
                    System.out.println(ConsoleUI.VERDE + "¡Hasta luego!" + ConsoleUI.RESET);
                    break;
                default:
                    System.out.println(ConsoleUI.ROJO + "Opción no válida." + ConsoleUI.RESET);
            }
        }
    }

    /**
     * Muestra el submenú de escenarios.
     */
    private void mostrarSubMenu() {
        boolean volver = false;

        while (!volver) {
            String modo = vulnerable ? "VULNERABLE" : "REMEDIADO";
            String[] opcionesSubMenu = {
                "1. Ejecutar Escenario A (Credential Stuffing)",
                "2. Ejecutar Escenario B (Privilege Escalation)",
                "3. Ejecutar Escenario C (Log Tampering)",
                "4. Ver logs",
                "5. Ver métricas",
                "0. Volver"
            };
            ConsoleUI.imprimirMenu("MODO " + modo, opcionesSubMenu);
            System.out.print("Seleccione una opción: ");

            String opcion = leerEntrada();

            switch (opcion) {
                case "1":
                    ejecutarEscenario(new CredentialStuffingScenario());
                    break;
                case "2":
                    ejecutarEscenario(new PrivilegeEscalationScenario());
                    break;
                case "3":
                    ejecutarEscenario(new LogTamperingScenario());
                    break;
                case "4":
                    verLogs();
                    break;
                case "5":
                    metrics.imprimirResumen();
                    break;
                case "0":
                    volver = true;
                    break;
                default:
                    System.out.println(ConsoleUI.ROJO + "Opción no válida." + ConsoleUI.RESET);
            }
        }
    }

    /**
     * Configura el bus de eventos y ejecuta un escenario.
     */
    private void ejecutarEscenario(AttackScenario escenario) {
        // Reiniciar estado
        bus = new EventBus();
        metrics.reiniciar();

        // Configurar listeners según el modo
        if (vulnerable) {
            bus.suscribir(new FileWriterListener(new VulnerableLoggingStrategy()));
        } else {
            bus.suscribir(new FileWriterListener(new SecureLoggingStrategy()));
        }

        AlertListener alertListener = new AlertListener();
        bus.suscribir(alertListener);
        bus.suscribir(metrics);

        // Ejecutar escenario midiendo tiempo
        metrics.iniciarMedicion();
        String actor = "atacante_externo";
        escenario.ejecutar(bus, actor);
        
        // Pequeña pausa para permitir que se procesen eventos asíncronos si los hubiera
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        metrics.detenerMedicion();

        System.out.println(ConsoleUI.VERDE + "Escenario ejecutado correctamente." + ConsoleUI.RESET);
    }

    /**
     * Muestra el contenido de los archivos de log.
     */
    private void verLogs() {
        String archivo = vulnerable ? "log_vulnerable.txt" : "log_remediado.txt";
        System.out.println(ConsoleUI.CIAN + "--- Contenido de " + archivo + " ---" + ConsoleUI.RESET);
        
        try {
            String contenido = Files.readString(Paths.get(archivo));
            System.out.println(contenido);
        } catch (IOException e) {
            System.out.println(ConsoleUI.AMARILLO + "El archivo de logs aún no existe o está vacío." + ConsoleUI.RESET);
        }
        
        System.out.println(ConsoleUI.CIAN + "-----------------------------------" + ConsoleUI.RESET);
    }

    /**
     * Lee una línea de entrada del usuario.
     */
    private String leerEntrada() {
        try {
            return lector.readLine().trim();
        } catch (IOException e) {
            return "";
        }
    }
}
