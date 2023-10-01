package ru.evgeniy.marketplace.utils.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class RuntimeIOException extends RuntimeException implements CustomException {

    private HttpStatus status;
}