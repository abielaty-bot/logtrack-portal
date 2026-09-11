# Punto de continuación: LogTrack Portal

## Última actualización

11 de septiembre de 2026

## Estado actual

La versión web de LogTrack Portal funciona con Java puro mediante
`com.sun.net.httpserver.HttpServer` en el puerto 8080.

La aplicación tiene dos contextos independientes:

- Modo vulnerable.
- Modo remediado.

Cada contexto tiene su propio `EventBus`, `MetricsEngine` y estrategia
de logging. `AlertListener` está suscrito únicamente al modo remediado.

## Cambios realizados

- Se agregaron tres getters públicos a
  `LogTrackPortal/src/metrics/MetricsEngine.java`.
- Se creó `LogTrackPortal/src/web/HtmlTemplates.java`.
- Se creó `LogTrackPortal/src/web/WebServer.java`.
- Se integraron las cuatro plantillas HTML.
- Se implementaron las rutas `/`, `/panel`, `/ejecutar`, `/logs`
  y `/metricas`.
- Se implementó un historial en memoria con la última ejecución de cada
  escenario.
- Se implementó la lectura del log vulnerable.
- Se implementó el parseo del log remediado mediante una expresión regular.
- Se corrigió la visualización de contexto para evitar `0%%`.

## Pruebas realizadas

### Modo vulnerable

Se ejecutaron los tres escenarios:

- Credential stuffing: 10 eventos.
- Escalación de privilegios: 1 evento.
- Log tampering: 2 eventos.

Resultado acumulado:

- 13 eventos.
- 0 alertas.
- Tiempo de detección: N/A.
- Contexto: 0%.
- Los tres escenarios aparecen como no detectados.

### Modo remediado

- Credential stuffing genera 10 eventos y 10 alertas.
- Log tampering genera 2 eventos y 2 alertas.
- Los escenarios ejecutados aparecen como detectados.
- `AlertListener` imprime alertas en la terminal.
- Los eventos se guardan en formato estructurado.
- Los saltos de línea maliciosos se neutralizan.

## Pruebas pendientes

- Confirmar visualmente que la tarjeta de métricas muestre `0%` y no `0%%`.
- Revisar completamente `/logs?modo=vulnerable`.
- Revisar completamente `/logs?modo=remediado`.
- Revisar las métricas acumuladas del modo remediado.
- Hacer una compilación final de todo el proyecto.

## Siguiente paso exacto

Completar las pruebas visuales pendientes y hacer el commit de los cambios
preparados.

## Comandos para retomar

Entrar en la carpeta del código:

```powershell
cd LogTrackPortal\src