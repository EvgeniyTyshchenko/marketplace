package ru.evgeniy.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.evgeniy.marketplace.entity.Ad;
import ru.evgeniy.marketplace.utils.Paths;

@Getter
public class AdDTO {

    @JsonProperty(value = "pk")
    @Schema(description = "id объявления")
    public long id;

    @JsonProperty(value = "author")
    @Schema(description = "id автора объявления")
    public long authorId;

    @JsonProperty(value = "image")
    @Schema(description = "Картинка объявления")
    public String imagePath;

    @Schema(description = "Цена объявления")
    public double price;

    @Schema(description = "Заголовок объявления")
    public String title;

    public static AdDTO toDto(Ad ad) {
        AdDTO adDTO = new AdDTO();
        adDTO.id = ad.getId();
        adDTO.authorId = ad.getUser().getId();
        adDTO.title = ad.getTitle();
        adDTO.price = ad.getPrice();
        adDTO.imagePath = String.format(Paths.GET_IMAGE_ENDPOINT, ad.getId());
        return adDTO;
    }
}