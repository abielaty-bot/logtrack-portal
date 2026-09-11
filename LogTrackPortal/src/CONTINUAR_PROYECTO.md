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
-  Se corrigió y verificó la visualización del porcentaje de contexto:
  ahora muestra `0%` en modo vulnerable y `100%` en modo remediado,
  sin duplicar el símbolo `%`.
- La corrección fue guardada en el commit `00879bb` y subida a la
  rama `main` de GitHub.

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

Se ejecutaron los tres escenarios:

- Credential stuffing: 10 eventos y 10 alertas.
- Escalación de privilegios: 1 evento y 1 alerta.
- Log tampering: 2 eventos y 2 alertas.

Resultado acumulado:

- 13 eventos.
- 13 alertas.
- Tiempo promedio de detección mostrado: 0.7s.
- Contexto: 100%.
- Los tres escenarios aparecen como detectados.
- `AlertListener` imprime alertas en la terminal.
- Los eventos se guardan en formato estructurado.
- Los saltos de línea maliciosos se neutralizan.

## Pruebas completadas

- Se confirmó que la tarjeta superior de métricas muestra `0%` y `100%`
  correctamente, sin duplicar el símbolo `%`.
- Se revisó `/logs?modo=vulnerable`.
- El log vulnerable permite que el salto de línea inyectado genere una
  línea falsa independiente.
- Se revisó `/logs?modo=remediado`.
- El log remediado neutraliza el salto de línea y mantiene al actor
  malicioso en una sola fila estructurada.
- Se revisaron las métricas acumuladas de ambos modos.
- Se realizó una compilación completa sin errores.

## Estado de las pruebas

Las funciones principales del aplicativo web quedaron verificadas:

- Página de inicio.
- Panel vulnerable.
- Panel remediado.
- Ejecución de Credential stuffing.
- Ejecución de Escalación de privilegios.
- Ejecución de Log tampering.
- Vista de logs vulnerable.
- Vista de logs remediada.
- Vista de métricas vulnerable.
- Vista de métricas remediada.
- Redirección desde `/ejecutar` hacia `/panel`.
- Separación de métricas entre los dos modos.

## Siguiente paso exacto

Guardar en Git la corrección visual de `100%%` y la actualización de este
archivo de continuación.

Después, revisar el estado del repositorio, crear un commit y subirlo a la
rama `main`.

## Comandos para retomar

Entrar en la carpeta del código:

```powershell
cd LogTrackPortal\src
```

Compilar el proyecto:

```powershell
javac domain\*.java logging\*.java events\*.java scenarios\*.java metrics\*.java ui\*.java web\*.java Main.java
```

Iniciar el servidor:

```powershell
java web.WebServer
```

Abrir en el navegador:

```text
http://localhost:8080
```

Detener el servidor:

```text
Ctrl + C
```

Volver a la raíz del repositorio:

```powershell
cd ..\..
```

Revisar el estado de Git:

```powershell
git status
```

## Archivos que no deben subirse

- Archivos `*.class`.
- `log_vulnerable.txt`.
- `log_remediado.txt`.