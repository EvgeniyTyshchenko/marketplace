package ru.evgeniy.marketplace.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.evgeniy.marketplace.entity.Comment;
import ru.evgeniy.marketplace.service.CommentService;
import ru.evgeniy.marketplace.utils.exception.EntityNotFound;
import ru.evgeniy.marketplace.repository.CommentRepository;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Comment getComment(long id) {
        log.info("Запрос на получение комментария id=" + id);
        return commentRepository.findById(id).orElseThrow(() -> new EntityNotFound(Comment.class, HttpStatus.NOT_FOUND));
    }

    @Override
    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public void removeComment(Comment comment) {
        commentRepository.delete(comment);
    }
}