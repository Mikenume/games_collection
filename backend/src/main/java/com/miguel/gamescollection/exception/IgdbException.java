package com.miguel.gamescollection.exception;

// IGDB (o Twitch) no ha respondido bien. El GlobalExceptionHandler la convierte en un 502.
public class IgdbException extends RuntimeException {

    public IgdbException(String message) {
        super(message);
    }

    public IgdbException(String message, Throwable cause) {
        super(message, cause);
    }
}
