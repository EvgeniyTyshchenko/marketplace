package ru.evgeniy.marketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;

@Setter
public class UserUpdaterDTO {

    @Schema(description = "Имя пользователя")
    public String firstName;

    @Schema(description = "Фамилия пользователя")
    public String lastName;

    @Schema(description = "Телефон пользователя")
    public String phone;
}