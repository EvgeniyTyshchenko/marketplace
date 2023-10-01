package ru.evgeniy.marketplace.utils.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFound extends RuntimeException implements CustomException {

    private final Class<?> klass;
    private final HttpStatus status;

    public EntityNotFound(Class<?> klass, HttpStatus status) {
        this.klass = klass;
        this.status = status;
    }

    @Override
    public String getMessage() {
        return String.format("Такого %s не существует!", klass.getSimpleName());
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}