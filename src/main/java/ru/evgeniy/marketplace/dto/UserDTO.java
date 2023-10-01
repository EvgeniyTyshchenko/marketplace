package ru.evgeniy.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.evgeniy.marketplace.entity.User;
import ru.evgeniy.marketplace.utils.Paths;

public class UserDTO {

    @Schema(description = "id пользователя")
    public long id;

    @Schema(description = "Имя пользователя")
    public String firstName;

    @Schema(description = "Фамилия пользователя")
    public String lastName;

    @Schema(description = "email пользователя")
    public String email;

    @Schema(description = "Телефон пользователя")
    public String phone;

    @JsonProperty(value = "image")
    @Schema(description = "Путь на аватар пользователя")
    public String avatarPath;

    public static UserDTO toUserDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.id = user.getId();
        userDTO.email = user.getEmail();
        userDTO.firstName = user.getFirstName();
        userDTO.lastName = user.getLastName();
        userDTO.phone = user.getPhone();
        userDTO.avatarPath = Paths.GET_AVATAR_ENDPOINT;
        return userDTO;
    }
}