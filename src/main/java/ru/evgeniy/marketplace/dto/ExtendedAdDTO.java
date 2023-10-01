package ru.evgeniy.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.evgeniy.marketplace.entity.User;
import ru.evgeniy.marketplace.entity.Ad;
import ru.evgeniy.marketplace.utils.Paths;

public class ExtendedAdDTO {

    @JsonProperty(value = "pk")
    @Schema(description = "id объявления")
    public long id;

    @JsonProperty(value = "image")
    @Schema(description = "Путь на картинку объявления")
    public String imagePath;

    @Schema(description = "Цена объявления")
    public double price;

    @Schema(description = "Заголовок объявления")
    public String title;

    @Schema(description = "Описание объявления")
    public String description;

    @JsonProperty(value = "authorFirstName")
    @Schema(description = "Имя автора объявления")
    public String firstName;
    @JsonProperty(value = "authorLastName")
    @Schema(description = "Фамилия автора объявления")
    public String lastName;

    @Schema(description = "email автора объявления")
    public String email;

    @Schema(description = "Телефон автора объявления")
    public String phone;

    public static ExtendedAdDTO toDto(Ad ad) {
        ExtendedAdDTO extendedAdDTO = new ExtendedAdDTO();
        User user = ad.getUser();
        extendedAdDTO.id = ad.getId();
        extendedAdDTO.imagePath = String.format(Paths.GET_IMAGE_ENDPOINT, ad.getId());
        extendedAdDTO.price = ad.getPrice();
        extendedAdDTO.title = ad.getTitle();
        extendedAdDTO.description = ad.getDescription();
        extendedAdDTO.firstName = user.getFirstName();
        extendedAdDTO.lastName = user.getLastName();
        extendedAdDTO.email = user.getEmail();
        extendedAdDTO.phone = user.getPhone();
        return extendedAdDTO;
    }
}