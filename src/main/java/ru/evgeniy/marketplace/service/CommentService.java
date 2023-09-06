package ru.evgeniy.marketplace.service;

import ru.evgeniy.marketplace.entity.Comment;

public interface CommentService {

    Comment saveComment(Comment comment);

    Comment getComment(long id);

    void removeComment(Comment comment);
}