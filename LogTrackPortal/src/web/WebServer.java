package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import events.AlertListener;
import events.EventBus;
import events.FileWriterListener;
import logging.LoggingStrategy;
import logging.SecureLoggingStrategy;
import logging.VulnerableLoggingStrategy;
import metrics.MetricsEngine;
import scenarios.AttackScenario;
import scenarios.CredentialStuffingScenario;
import scenarios.LogTamperingScenario;
import scenarios.PrivilegeEscalationScenario;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServer {

    private static final int PUERTO = 8080;
    private static final String ACTOR_PRUEBA = "jperez";

    private static final String MODO_VULNERABLE = "vulnerable";
    private static final String MODO_REMEDIADO = "remediado";

    private static final String ESCENARIO_CREDENTIAL_STUFFING = "credential_stuffing";

    private static final String ESCENARIO_ESCALACION_PRIVILEGIOS = "escalacion_privilegios";

    private static final String ESCENARIO_LOG_TAMPERING = "log_tampering";

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final Pattern PATRON_LOG_REMEDIADO = Pattern
            .compile("^\\[(.*?)]\\s*\\|\\s*" + "usuario=(.*?)\\s*\\|\\s*" + "accion=(.*?)\\s*\\|\\s*"
                    + "resultado=(.*?)\\s*\\|\\s*" + "origen=(.*?)\\s*\\|\\s*" + "severidad=(.*?)\\s*$");

    private static final ContextoModo CONTEXTO_VULNERABLE = crearContextoVulnerable();

    private static final ContextoModo CONTEXTO_REMEDIADO = crearContextoRemediado();

    public static void main(String[] args) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);

        servidor.createContext("/", WebServer::manejarInicio);
        servidor.createContext("/panel", WebServer::manejarPanel);
        servidor.createContext("/ejecutar", WebServer::manejarEjecucion);
        servidor.createContext("/logs", WebServer::manejarLogs);
        servidor.createContext("/metricas", WebServer::manejarMetricas);

        servidor.setExecutor(null);
        servidor.start();

        System.out.println("Servidor LogTrack Portal corriendo en http://localhost:8080");
    }

    private static ContextoModo crearContextoVulnerable() {
        LoggingStrategy estrategia = new VulnerableLoggingStrategy();

        ContextoModo contexto = new ContextoModo(MODO_VULNERABLE, estrategia, Path.of("log_vulnerable.txt"));

        contexto.bus.suscribir(contexto.metrics);

        contexto.bus.suscribir(new FileWriterListener(contexto.loggingStrategy));

        return contexto;
    }

    private static ContextoModo crearContextoRemediado() {
        LoggingStrategy estrategia = new SecureLoggingStrategy();

        ContextoModo contexto = new ContextoModo(MODO_REMEDIADO, estrategia, Path.of("log_remediado.txt"));

        contexto.bus.suscribir(contexto.metrics);
        contexto.bus.suscribir(new AlertListener());

        contexto.bus.suscribir(new FileWriterListener(contexto.loggingStrategy));

        return contexto;
    }

    private static void manejarInicio(HttpExchange exchange) throws IOException {

        if (!validarMetodoGet(exchange)) {
            return;
        }

        if (!"/".equals(exchange.getRequestURI().getPath())) {
            enviarRespuestaHtml(exchange, 404,
                    construirPaginaError("Página no encontrada", "La ruta solicitada no existe."));
            return;
        }

        String respuesta = HtmlTemplates.paginaInicio("/panel?modo=vulnerable", "/panel?modo=remediado");

        enviarRespuestaHtml(exchange, 200, respuesta);
    }

    private static void manejarPanel(HttpExchange exchange) throws IOException {

        if (!validarMetodoGet(exchange)) {
            return;
        }

        Map<String, String> parametros = obtenerParametrosConsulta(exchange);

        ContextoModo contexto = obtenerContexto(parametros.get("modo"));

        if (contexto == null) {
            enviarRespuestaHtml(exchange, 400,
                    construirPaginaError("Modo no válido", "Usa modo=vulnerable o modo=remediado."));
            return;
        }

        String respuesta;

        synchronized (contexto) {
            respuesta = HtmlTemplates.paginaPanel(obtenerModeClass(contexto), obtenerModeLabel(contexto), "/",
                    construirUrlEjecucion(contexto, ESCENARIO_CREDENTIAL_STUFFING),
                    construirUrlEjecucion(contexto, ESCENARIO_ESCALACION_PRIVILEGIOS),
                    construirUrlEjecucion(contexto, ESCENARIO_LOG_TAMPERING), "/logs?modo=" + contexto.modo,
                    "/metricas?modo=" + contexto.modo, contexto.ultimaAccion);
        }

        enviarRespuestaHtml(exchange, 200, respuesta);
    }

    private static void manejarEjecucion(HttpExchange exchange) throws IOException {

        if (!validarMetodoGet(exchange)) {
            return;
        }

        Map<String, String> parametros = obtenerParametrosConsulta(exchange);

        String modo = parametros.get("modo");
        String escenarioId = parametros.get("escenario");

        ContextoModo contexto = obtenerContexto(modo);

        if (contexto == null) {
            enviarRespuestaHtml(exchange, 400,
                    construirPaginaError("Modo no válido", "Usa modo=vulnerable o modo=remediado."));
            return;
        }

        AttackScenario escenario = crearEscenario(escenarioId);

        if (escenario == null) {
            enviarRespuestaHtml(exchange, 400,
                    construirPaginaError("Escenario no válido", "Selecciona un escenario disponible desde el panel."));
            return;
        }

        synchronized (contexto) {
            ejecutarEscenario(contexto, escenarioId, escenario);
        }

        redirigir(exchange, "/panel?modo=" + contexto.modo);
    }

    private static void ejecutarEscenario(ContextoModo contexto, String escenarioId, AttackScenario escenario) {
        contexto.metrics.reiniciar();
        contexto.metrics.iniciarMedicion();

        try {
            escenario.ejecutar(contexto.bus, ACTOR_PRUEBA);
        } finally {
            contexto.metrics.detenerMedicion();
        }

        contexto.ultimaAccion = construirResultadoAccion(contexto, escenarioId);

        contexto.historial.put(escenarioId,
                new DatosEscenario(contexto.ultimaAccion.nombre, contexto.ultimaAccion.eventos,
                        contexto.ultimaAccion.alertas, contexto.ultimaAccion.tiempoDeteccion,
                        contexto.ultimaAccion.contextoPct, contexto.ultimaAccion.detectionClass,
                        contexto.ultimaAccion.detectionLabel));
    }

    private static HtmlTemplates.ResultadoAccion construirResultadoAccion(ContextoModo contexto, String escenarioId) {
        HtmlTemplates.ResultadoAccion resultado = new HtmlTemplates.ResultadoAccion();

        resultado.nombre = obtenerNombreEscenario(escenarioId);

        resultado.timestamp = LocalDateTime.now().format(FORMATO_FECHA);

        resultado.eventos = String.valueOf(contexto.metrics.getTotalEventos());

        if (esModoVulnerable(contexto)) {
            resultado.detectionClass = "undetected";
            resultado.detectionLabel = "No detectado";

            resultado.summary = "El ataque generó eventos, pero el modo vulnerable "
                    + "no dispone de monitoreo ni contexto suficiente " + "para detectarlos correctamente.";

            resultado.alertas = "0";
            resultado.tiempoDeteccion = "N/A";
            resultado.contextoPct = "0%";
        } else {
            resultado.detectionClass = "detected";
            resultado.detectionLabel = "Detectado";

            resultado.summary = "El ataque fue registrado con información estructurada, "
                    + "severidad, resultado y origen técnico.";

            resultado.alertas = String.valueOf(contexto.metrics.getAlertasDisparadas());

            resultado.tiempoDeteccion = formatearTiempo(contexto.metrics.getTiempoTranscurridoSegundos());

            resultado.contextoPct = "100%";
        }

        return resultado;
    }

    private static void manejarLogs(HttpExchange exchange) throws IOException {

        if (!validarMetodoGet(exchange)) {
            return;
        }

        Map<String, String> parametros = obtenerParametrosConsulta(exchange);

        ContextoModo contexto = obtenerContexto(parametros.get("modo"));

        if (contexto == null) {
            enviarRespuestaHtml(exchange, 400,
                    construirPaginaError("Modo no válido", "Usa modo=vulnerable o modo=remediado."));
            return;
        }

        String contenidoCrudo;
        List<String> lineas;

        if (Files.exists(contexto.archivoLog)) {
            contenidoCrudo = Files.readString(contexto.archivoLog, StandardCharsets.UTF_8);

            lineas = Files.readAllLines(contexto.archivoLog, StandardCharsets.UTF_8);
        } else {
            contenidoCrudo = "Aún no se han generado registros.";

            lineas = List.of();
        }

        boolean vulnerable = esModoVulnerable(contexto);

        String filasTabla = "";

        if (!vulnerable) {
            filasTabla = construirFilasLogRemediado(lineas);

            if (lineas.isEmpty()) {
                filasTabla = HtmlTemplates.filaLog("N/A", "level-info", "SIN REGISTROS", "N/A",
                        "Aún no se han generado registros.", "N/A", "Ejecuta un escenario desde el panel.");
            }
        }

        String formatoLabel = vulnerable ? "Texto plano no estructurado" : "Registro estructurado";

        String integridadLabel = vulnerable ? "Integridad no garantizada" : "Entradas neutralizadas";

        String respuesta = HtmlTemplates.paginaLogs(obtenerModeClass(contexto), obtenerModeLabel(contexto),
                "/panel?modo=" + contexto.modo, String.valueOf(lineas.size()), formatoLabel, integridadLabel,
                contenidoCrudo, filasTabla);

        enviarRespuestaHtml(exchange, 200, respuesta);
    }

    private static String construirFilasLogRemediado(List<String> lineas) {
        StringBuilder filas = new StringBuilder();

        for (String linea : lineas) {
            Matcher matcher = PATRON_LOG_REMEDIADO.matcher(linea);

            if (!matcher.matches()) {
                filas.append(HtmlTemplates.filaLog("N/A", "level-warning", "FORMATO DESCONOCIDO", "N/A",
                        "REGISTRO NO PARSEADO", "N/A", linea));

                continue;
            }

            String timestamp = matcher.group(1).trim();

            String usuario = matcher.group(2).trim();

            String accion = matcher.group(3).trim();

            String resultado = matcher.group(4).trim();

            String origen = matcher.group(5).trim();

            String severidad = matcher.group(6).trim();

            filas.append(HtmlTemplates.filaLog(timestamp, obtenerLevelClass(severidad), severidad, usuario, accion,
                    origen, "Resultado: " + resultado));
        }

        return filas.toString();
    }

    private static String obtenerLevelClass(String severidad) {
        if (severidad == null) {
            return "level-info";
        }

        return switch (severidad.toUpperCase(Locale.ROOT)) {
        case "CRITICO" -> "level-critical";
        case "ALTO", "ADVERTENCIA" -> "level-warning";
        case "INFORMATIVO" -> "level-info";
        default -> "level-info";
        };
    }

    private static void manejarMetricas(HttpExchange exchange) throws IOException {

        if (!validarMetodoGet(exchange)) {
            return;
        }

        Map<String, String> parametros = obtenerParametrosConsulta(exchange);

        ContextoModo contexto = obtenerContexto(parametros.get("modo"));

        if (contexto == null) {
            enviarRespuestaHtml(exchange, 400,
                    construirPaginaError("Modo no válido", "Usa modo=vulnerable o modo=remediado."));
            return;
        }

        String respuesta;

        synchronized (contexto) {
            ResumenMetricas resumen = calcularResumenMetricas(contexto);

            boolean vulnerable = esModoVulnerable(contexto);

            String riskLevelClass;
            String riskLevelLabel;
            String riskLevelDesc;
            String metricaAlertasClass;
            String insightText;

            if (contexto.historial.isEmpty()) {
                riskLevelClass = "risk-warning";
                riskLevelLabel = "Sin ejecuciones";

                riskLevelDesc = "Todavía no se ha ejecutado ningún escenario.";

                metricaAlertasClass = "negative";

                insightText = "Ejecuta un escenario desde el panel " + "para generar métricas comparables.";
            } else if (vulnerable) {
                riskLevelClass = "risk-critical";
                riskLevelLabel = "Riesgo crítico";

                riskLevelDesc = "Los eventos ocurren sin alertas ni contexto " + "suficiente para investigarlos.";

                metricaAlertasClass = "negative";

                insightText = "El modo vulnerable demuestra la falta de " + "registro y monitoreo efectivo. "
                        + "La actividad maliciosa ocurre, " + "pero no produce señales útiles.";
            } else {
                riskLevelClass = "risk-controlled";
                riskLevelLabel = "Riesgo controlado";

                riskLevelDesc = "Los ataques quedan registrados con severidad, " + "resultado y contexto técnico.";

                metricaAlertasClass = "positive";

                insightText = "El modo remediado permite detectar, analizar " + "y responder ante los incidentes.";
            }

            respuesta = HtmlTemplates.paginaMetricas(obtenerModeClass(contexto), obtenerModeLabel(contexto),
                    "/panel?modo=" + contexto.modo, riskLevelClass, riskLevelLabel, riskLevelDesc,
                    String.valueOf(resumen.totalEventos), vulnerable ? "0" : String.valueOf(resumen.totalAlertas),
                    metricaAlertasClass, vulnerable ? "N/A" : resumen.tiempoPromedio, vulnerable ? "0" : "100",
                    construirFilasMetricas(contexto), insightText);
        }

        enviarRespuestaHtml(exchange, 200, respuesta);
    }

    private static ResumenMetricas calcularResumenMetricas(ContextoModo contexto) {
        int totalEventos = 0;
        int totalAlertas = 0;

        double sumaTiempos = 0;
        int tiemposValidos = 0;

        for (DatosEscenario datos : contexto.historial.values()) {

            totalEventos += convertirEntero(datos.eventos);

            totalAlertas += convertirEntero(datos.alertas);

            Double tiempo = convertirTiempo(datos.tiempoDeteccion);

            if (tiempo != null) {
                sumaTiempos += tiempo;
                tiemposValidos++;
            }
        }

        String tiempoPromedio;

        if (tiemposValidos == 0) {
            tiempoPromedio = "N/A";
        } else {
            tiempoPromedio = formatearTiempo(sumaTiempos / tiemposValidos);
        }

        String contextoPct;

        if (contexto.historial.isEmpty() || esModoVulnerable(contexto)) {
            contextoPct = "0%";
        } else {
            contextoPct = "100%";
        }

        return new ResumenMetricas(totalEventos, totalAlertas, tiempoPromedio, contextoPct);
    }

    private static String construirFilasMetricas(ContextoModo contexto) {
        StringBuilder filas = new StringBuilder();

        agregarFilaMetrica(filas, contexto, ESCENARIO_CREDENTIAL_STUFFING);

        agregarFilaMetrica(filas, contexto, ESCENARIO_ESCALACION_PRIVILEGIOS);

        agregarFilaMetrica(filas, contexto, ESCENARIO_LOG_TAMPERING);

        return filas.toString();
    }

    private static void agregarFilaMetrica(StringBuilder filas, ContextoModo contexto, String escenarioId) {
        DatosEscenario datos = contexto.historial.get(escenarioId);

        if (datos == null) {
            filas.append(HtmlTemplates.filaMetricaEscenario(obtenerNombreEscenario(escenarioId), "0", "0", "N/A", "0%",
                    "pending", "Pendiente"));

            return;
        }

        filas.append(HtmlTemplates.filaMetricaEscenario(datos.nombre, datos.eventos, datos.alertas,
                datos.tiempoDeteccion, datos.contextoPct, datos.statusClass, datos.statusLabel));
    }

    private static AttackScenario crearEscenario(String escenarioId) {
        if (ESCENARIO_CREDENTIAL_STUFFING.equals(escenarioId)) {
            return new CredentialStuffingScenario();
        }

        if (ESCENARIO_ESCALACION_PRIVILEGIOS.equals(escenarioId)) {
            return new PrivilegeEscalationScenario();
        }

        if (ESCENARIO_LOG_TAMPERING.equals(escenarioId)) {
            return new LogTamperingScenario();
        }

        return null;
    }

    private static String obtenerNombreEscenario(String escenarioId) {
        if (ESCENARIO_CREDENTIAL_STUFFING.equals(escenarioId)) {
            return "Credential stuffing";
        }

        if (ESCENARIO_ESCALACION_PRIVILEGIOS.equals(escenarioId)) {
            return "Escalación de privilegios";
        }

        if (ESCENARIO_LOG_TAMPERING.equals(escenarioId)) {
            return "Log tampering";
        }

        return "Escenario desconocido";
    }

    private static ContextoModo obtenerContexto(String modo) {
        if (modo == null) {
            return null;
        }

        if (MODO_VULNERABLE.equalsIgnoreCase(modo)) {
            return CONTEXTO_VULNERABLE;
        }

        if (MODO_REMEDIADO.equalsIgnoreCase(modo)) {
            return CONTEXTO_REMEDIADO;
        }

        return null;
    }

    private static boolean esModoVulnerable(ContextoModo contexto) {
        return MODO_VULNERABLE.equals(contexto.modo);
    }

    private static String obtenerModeClass(ContextoModo contexto) {
        return esModoVulnerable(contexto) ? "mode-vulnerable" : "mode-remediado";
    }

    private static String obtenerModeLabel(ContextoModo contexto) {
        return esModoVulnerable(contexto) ? "Modo vulnerable" : "Modo remediado";
    }

    private static String construirUrlEjecucion(ContextoModo contexto, String escenarioId) {
        return "/ejecutar?modo=" + contexto.modo + "&escenario=" + escenarioId;
    }

    private static String formatearTiempo(double segundos) {
        return String.format(Locale.US, "%.1fs", segundos);
    }

    private static int convertirEntero(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException excepcion) {
            return 0;
        }
    }

    private static Double convertirTiempo(String valor) {
        if (valor == null || "N/A".equalsIgnoreCase(valor)) {
            return null;
        }

        try {
            return Double.parseDouble(valor.replace("s", "").trim());
        } catch (NumberFormatException excepcion) {
            return null;
        }
    }

    private static Map<String, String> obtenerParametrosConsulta(HttpExchange exchange) {

        Map<String, String> parametros = new HashMap<>();

        String consulta = exchange.getRequestURI().getRawQuery();

        if (consulta == null || consulta.isBlank()) {
            return parametros;
        }

        String[] pares = consulta.split("&");

        for (String par : pares) {
            if (par.isBlank()) {
                continue;
            }

            String[] partes = par.split("=", 2);

            String clave = decodificarUrl(partes[0]);

            String valor = partes.length == 2 ? decodificarUrl(partes[1]) : "";

            parametros.put(clave, valor);
        }

        return parametros;
    }

    private static String decodificarUrl(String texto) {
        return URLDecoder.decode(texto, StandardCharsets.UTF_8);
    }

    private static boolean validarMetodoGet(HttpExchange exchange) throws IOException {

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            return true;
        }

        exchange.getResponseHeaders().set("Allow", "GET");

        enviarRespuestaHtml(exchange, 405,
                construirPaginaError("Método no permitido", "Esta ruta solamente acepta solicitudes GET."));

        return false;
    }

    private static String construirPaginaError(String titulo, String mensaje) {
        return "<!DOCTYPE html>" + "<html lang=\"es\">" + "<head>" + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" " + "content=\"width=device-width, initial-scale=1.0\">"
                + "<title>Error</title>" + "</head>" + "<body>" + "<h1>" + HtmlTemplates.escaparHtml(titulo) + "</h1>"
                + "<p>" + HtmlTemplates.escaparHtml(mensaje) + "</p>" + "<p>\"/\"Volver al inicio</a></p>" + "</body>"
                + "</html>";
    }

    private static void enviarRespuestaHtml(HttpExchange exchange, int codigoEstado, String respuesta)
            throws IOException {

        byte[] contenido = respuesta.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

        exchange.getResponseHeaders().set("Cache-Control", "no-store");

        exchange.sendResponseHeaders(codigoEstado, contenido.length);

        try (OutputStream salida = exchange.getResponseBody()) {

            salida.write(contenido);
        }
    }

    private static void redirigir(HttpExchange exchange, String ubicacion) throws IOException {

        exchange.getResponseHeaders().set("Location", ubicacion);

        exchange.getResponseHeaders().set("Cache-Control", "no-store");

        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private static class DatosEscenario {

        private final String nombre;
        private final String eventos;
        private final String alertas;
        private final String tiempoDeteccion;
        private final String contextoPct;
        private final String statusClass;
        private final String statusLabel;

        private DatosEscenario(String nombre, String eventos, String alertas, String tiempoDeteccion,
                String contextoPct, String statusClass, String statusLabel) {
            this.nombre = nombre;
            this.eventos = eventos;
            this.alertas = alertas;
            this.tiempoDeteccion = tiempoDeteccion;
            this.contextoPct = contextoPct;
            this.statusClass = statusClass;
            this.statusLabel = statusLabel;
        }
    }

    private static class ResumenMetricas {

        private final int totalEventos;
        private final int totalAlertas;
        private final String tiempoPromedio;
        private final String contextoPct;

        private ResumenMetricas(int totalEventos, int totalAlertas, String tiempoPromedio, String contextoPct) {
            this.totalEventos = totalEventos;
            this.totalAlertas = totalAlertas;
            this.tiempoPromedio = tiempoPromedio;
            this.contextoPct = contextoPct;
        }
    }

    private static class ContextoModo {

        private final String modo;
        private final EventBus bus;
        private final LoggingStrategy loggingStrategy;
        private final MetricsEngine metrics;
        private final Path archivoLog;

        private final Map<String, DatosEscenario> historial;

        private HtmlTemplates.ResultadoAccion ultimaAccion;

        private ContextoModo(String modo, LoggingStrategy loggingStrategy, Path archivoLog) {
            this.modo = modo;
            this.bus = new EventBus();
            this.loggingStrategy = loggingStrategy;
            this.metrics = new MetricsEngine();
            this.archivoLog = archivoLog;
            this.historial = new LinkedHashMap<>();

            this.ultimaAccion = HtmlTemplates.ResultadoAccion.pendiente();
        }
    }
}