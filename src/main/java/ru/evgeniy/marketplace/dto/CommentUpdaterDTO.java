package ru.evgeniy.marketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class CommentUpdaterDTO {

    @Schema(description = "Текст комментария")
    public String text;
}