package ru.evgeniy.marketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AdUpdaterDTO {

    @Schema(description = "Цена объявления")
    public double price;

    @Schema(description = "Заголовок объявления")
    public String title;

    @Schema(description = "Описание объявления")
    public String description;
}