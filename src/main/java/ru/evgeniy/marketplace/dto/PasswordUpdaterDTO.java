package ru.evgeniy.marketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class PasswordUpdaterDTO {

    @Schema(description = "Текущий пароль")
    public String currentPassword;

    @Schema(description = "Новый пароль")
    public String newPassword;
}