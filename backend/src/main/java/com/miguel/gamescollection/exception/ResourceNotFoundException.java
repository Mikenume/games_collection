package com.miguel.gamescollection.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Integer id) {
        super("No se ha encontrado " + resource + " con id " + id);
    }
}
