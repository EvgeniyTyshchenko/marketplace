package ru.evgeniy.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.evgeniy.marketplace.entity.Comment;
import ru.evgeniy.marketplace.entity.User;

@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
public class CommentDTO {

    @JsonProperty(value = "pk")
    @Schema(description = "id комментария")
    public long id;

    @JsonProperty(value = "author")
    @Schema(description = "id автора комментария")
    public long authorId;

    @JsonProperty(value = "authorFirstName")
    @Schema(description = "Имя создателя комментария")
    public String firstName;

    @JsonProperty(value = "authorImage")
    @Schema(description = "Путь на аватар автора комментария")
    public String avatarPath;

    @JsonProperty(value = "createdAt")
    @Schema(description = "Дата и время создания комментария")
    public long creationTime;

    @Schema(description = "Текст комментария")
    public String text;

    public static CommentDTO toDto(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        User user = comment.getAd().getUser();
        commentDTO.id = comment.getId();
        commentDTO.authorId = user.getId();
        commentDTO.firstName = user.getFirstName();
        commentDTO.avatarPath = user.getAvatarPath();
        commentDTO.text = comment.getText();
        commentDTO.creationTime = comment.getCreationTime();
        return commentDTO;
    }
}