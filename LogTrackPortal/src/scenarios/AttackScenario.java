package scenarios;

import events.EventBus;

/**
 * Interfaz para definir escenarios de ataque.
 */
public interface AttackScenario {
    /**
     * Ejecuta el escenario de ataque utilizando el bus de eventos proporcionado.
     *
     * @param bus El bus de eventos donde se publicarán los eventos del escenario.
     * @param actor El nombre del actor que realiza el ataque.
     */
    void ejecutar(EventBus bus, String actor);
}
