package ru.evgeniy.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class CommentsDTO {

    @Schema(description = "Общее количество комментариев")
    public int count;

    @JsonProperty(value = "results")
    public List<CommentDTO> commentDTOS;

    public CommentsDTO(int count, List<CommentDTO> commentDTOS) {
        this.count = count;
        this.commentDTOS = commentDTOS;
    }
}