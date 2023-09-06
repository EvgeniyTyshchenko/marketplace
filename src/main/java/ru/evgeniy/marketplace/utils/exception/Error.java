package ru.evgeniy.marketplace.utils.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class Error {

    @JsonIgnore(value = false)
    private Timestamp timestamp;

    private int status;

    private String message;

    private String path;
}