package ru.evgeniy.marketplace.dto.other;

import io.swagger.v3.oas.annotations.media.Schema;

public class Credentials {

    @Schema(description = "Логин пользователя")
    public String username;

    @Schema(description = "Пароль пользователя")
    public String password;

    @Schema(description = "Имя пользователя")
    public String firstName;

    @Schema(description = "Фамилия пользователя")
    public String lastName;

    @Schema(description = "Телефон пользователя")
    public String phone;

    @Schema(description = "Роль")
    public String role;
}