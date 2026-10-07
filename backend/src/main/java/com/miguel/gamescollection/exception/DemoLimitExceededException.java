package com.miguel.gamescollection.exception;

// Las cuentas DEMO ya han creado hoy todos los juegos permitidos.
// El GlobalExceptionHandler la convierte en un 429.
public class DemoLimitExceededException extends RuntimeException {

    public DemoLimitExceededException(int limit) {
        super("Se ha alcanzado el límite de " + limit + " juegos por día para las cuentas demo. Vuelve a intentarlo mañana.");
    }
}
