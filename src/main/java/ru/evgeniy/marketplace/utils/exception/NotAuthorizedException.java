package ru.evgeniy.marketplace.utils.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class NotAuthorizedException extends RuntimeException implements CustomException {

    private final HttpStatus status;

    private final String message;
}