package web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlTemplates {

    private static final String ARCHIVO_INICIO = "01_pantalla_inicio.html";
    private static final String ARCHIVO_PANEL = "02_panel_control.html";
    private static final String ARCHIVO_LOGS = "03_vista_logs.html";
    private static final String ARCHIVO_METRICAS = "04_vista_metricas.html";

    private static String cargarPlantilla(String nombreArchivo) {
        Path[] rutasPosibles = { Paths.get(nombreArchivo), Paths.get("src", nombreArchivo),
                Paths.get("LogTrackPortal", "src", nombreArchivo) };
        for (Path ruta : rutasPosibles) {
            if (Files.exists(ruta)) {
                try {
                    return Files.readString(ruta);
                } catch (IOException e) {
                    // seguir probando las demás rutas
                }
            }
        }
        throw new RuntimeException("No se encontró la plantilla HTML: " + nombreArchivo
                + ". Verifica que el archivo esté junto a los .java o en la carpeta src.");
    }

    public static String escaparHtml(String texto) {
        if (texto == null)
            return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String paginaInicio(String vulnerableUrl, String remediadoUrl) {
        String html = cargarPlantilla(ARCHIVO_INICIO);
        html = html.replace("{{VULNERABLE_MODE_URL}}", vulnerableUrl);
        html = html.replace("{{REMEDIATED_MODE_URL}}", remediadoUrl);
        html = html.replace("{{COURSE_INFO}}", "");
        return html;
    }

    public static class ResultadoAccion {
        public String nombre;
        public String detectionClass;
        public String detectionLabel;
        public String timestamp;
        public String summary;
        public String eventos;
        public String alertas;
        public String tiempoDeteccion;
        public String contextoPct;

        public static ResultadoAccion pendiente() {
            ResultadoAccion r = new ResultadoAccion();
            r.nombre = "Ningún escenario ejecutado todavía";
            r.detectionClass = "undetected";
            r.detectionLabel = "En espera";
            r.timestamp = "—";
            r.summary = "Selecciona un escenario de ataque arriba para ver aquí el resultado.";
            r.eventos = "0";
            r.alertas = "0";
            r.tiempoDeteccion = "N/A";
            r.contextoPct = "0%";
            return r;
        }
    }

    public static String paginaPanel(String modeClass, String modeLabel, String homeUrl,
            String runCredentialStuffingUrl, String runPrivilegeEscalationUrl, String runLogTamperingUrl,
            String logsUrl, String metricsUrl, ResultadoAccion resultado) {
        String html = cargarPlantilla(ARCHIVO_PANEL);
        html = html.replace("{{MODE_CLASS}}", modeClass);
        html = html.replace("{{MODE_LABEL}}", modeLabel);
        html = html.replace("{{HOME_URL}}", homeUrl);
        html = html.replace("{{RUN_CREDENTIAL_STUFFING_URL}}", runCredentialStuffingUrl);
        html = html.replace("{{RUN_PRIVILEGE_ESCALATION_URL}}", runPrivilegeEscalationUrl);
        html = html.replace("{{RUN_LOG_TAMPERING_URL}}", runLogTamperingUrl);
        html = html.replace("{{LOGS_URL}}", logsUrl);
        html = html.replace("{{METRICS_URL}}", metricsUrl);
        html = html.replace("{{LAST_ACTION_NAME}}", escaparHtml(resultado.nombre));
        html = html.replace("{{LAST_ACTION_DETECTION_CLASS}}", resultado.detectionClass);
        html = html.replace("{{LAST_ACTION_DETECTION_LABEL}}", escaparHtml(resultado.detectionLabel));
        html = html.replace("{{LAST_ACTION_TIMESTAMP}}", escaparHtml(resultado.timestamp));
        html = html.replace("{{LAST_ACTION_SUMMARY}}", escaparHtml(resultado.summary));
        html = html.replace("{{LAST_ACTION_EVENTS}}", escaparHtml(resultado.eventos));
        html = html.replace("{{LAST_ACTION_ALERTS}}", escaparHtml(resultado.alertas));
        html = html.replace("{{LAST_ACTION_DETECTION_TIME}}", escaparHtml(resultado.tiempoDeteccion));
        html = html.replace("{{LAST_ACTION_CONTEXT_PCT}}", escaparHtml(resultado.contextoPct));
        return html;
    }

    public static String paginaLogs(String modeClass, String modeLabel, String panelUrl, String totalLineas,
            String formatoLabel, String integridadLabel, String logRawContentSinEscapar, String logTableRowsHtml) {
        String html = cargarPlantilla(ARCHIVO_LOGS);
        html = html.replace("{{MODE_CLASS}}", modeClass);
        html = html.replace("{{MODE_LABEL}}", modeLabel);
        html = html.replace("{{PANEL_URL}}", panelUrl);
        html = html.replace("{{LOG_TOTAL_LINES}}", escaparHtml(totalLineas));
        html = html.replace("{{LOG_FORMAT_LABEL}}", escaparHtml(formatoLabel));
        html = html.replace("{{LOG_INTEGRITY_LABEL}}", escaparHtml(integridadLabel));
        html = html.replace("{{LOG_RAW_CONTENT}}", escaparHtml(logRawContentSinEscapar));
        html = html.replace("{{LOG_TABLE_ROWS}}", logTableRowsHtml);
        return html;
    }

    public static String filaLog(String timestamp, String levelClass, String levelLabel, String usuario, String evento,
            String ip, String detalle) {
        return "<tr>" + "<td class=\"mono\">" + escaparHtml(timestamp) + "</td>" + "<td><span class=\"level-badge "
                + levelClass + "\">" + escaparHtml(levelLabel) + "</span></td>" + "<td>" + escaparHtml(usuario)
                + "</td>" + "<td>" + escaparHtml(evento) + "</td>" + "<td class=\"mono\">" + escaparHtml(ip) + "</td>"
                + "<td>" + escaparHtml(detalle) + "</td>" + "</tr>";
    }

    public static String paginaMetricas(String modeClass, String modeLabel, String panelUrl, String riskLevelClass,
            String riskLevelLabel, String riskLevelDesc, String metricaEventos, String metricaAlertas,
            String metricaAlertasClass, String metricaTiempoDeteccion, String metricaContextoPct,
            String filasEscenariosHtml, String insightText) {
        String html = cargarPlantilla(ARCHIVO_METRICAS);
        html = html.replace("{{MODE_CLASS}}", modeClass);
        html = html.replace("{{MODE_LABEL}}", modeLabel);
        html = html.replace("{{PANEL_URL}}", panelUrl);
        html = html.replace("{{RISK_LEVEL_CLASS}}", riskLevelClass);
        html = html.replace("{{RISK_LEVEL_LABEL}}", escaparHtml(riskLevelLabel));
        html = html.replace("{{RISK_LEVEL_DESC}}", escaparHtml(riskLevelDesc));
        html = html.replace("{{METRICA_EVENTOS}}", escaparHtml(metricaEventos));
        html = html.replace("{{METRICA_ALERTAS_CLASS}}", metricaAlertasClass);
        html = html.replace("{{METRICA_ALERTAS}}", escaparHtml(metricaAlertas));
        html = html.replace("{{METRICA_TIEMPO_DETECCION}}", escaparHtml(metricaTiempoDeteccion));
        html = html.replace("{{METRICA_CONTEXTO_PCT}}", escaparHtml(metricaContextoPct));
        html = html.replace("{{METRICAS_ESCENARIOS_ROWS}}", filasEscenariosHtml);
        html = html.replace("{{METRICAS_INSIGHT_TEXT}}", escaparHtml(insightText));
        return html;
    }

    public static String filaMetricaEscenario(String nombreEscenario, String eventos, String alertas,
            String tiempoDeteccion, String contextoPct, String statusClass, String statusLabel) {
        return "<tr>" + "<td>" + escaparHtml(nombreEscenario) + "</td>" + "<td>" + escaparHtml(eventos) + "</td>"
                + "<td>" + escaparHtml(alertas) + "</td>" + "<td>" + escaparHtml(tiempoDeteccion) + "</td>" + "<td>"
                + escaparHtml(contextoPct) + "</td>" + "<td><span class=\"status-badge " + statusClass
                + "\"><span class=\"dot\"></span>" + escaparHtml(statusLabel) + "</span></td>" + "</tr>";
    }
}