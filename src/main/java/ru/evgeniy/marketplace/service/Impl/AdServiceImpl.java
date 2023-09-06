package ru.evgeniy.marketplace.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.evgeniy.marketplace.dto.*;
import ru.evgeniy.marketplace.entity.User;
import ru.evgeniy.marketplace.entity.Ad;
import ru.evgeniy.marketplace.entity.Comment;
import ru.evgeniy.marketplace.service.AdService;
import ru.evgeniy.marketplace.utils.exception.EntityNotFound;
import ru.evgeniy.marketplace.repository.AdRepository;
import ru.evgeniy.marketplace.utils.Paths;
import ru.evgeniy.marketplace.utils.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdServiceImpl implements AdService {

    private final AdRepository adRepository;
    private final CommentServiceImpl commentServiceImpl;
    private final UserServiceImpl userServiceImpl;
    private final FileServiceImpl fileServiceImpl;

    @Autowired
    public AdServiceImpl(AdRepository adRepository, CommentServiceImpl commentServiceImpl,
                         UserServiceImpl userServiceImpl, FileServiceImpl fileServiceImpl) {
        this.adRepository = adRepository;
        this.commentServiceImpl = commentServiceImpl;
        this.userServiceImpl = userServiceImpl;
        this.fileServiceImpl = fileServiceImpl;
    }

    @Override
    public AdsDTO getAllAds() {
        List<Ad> ads = adRepository.findAll();
        ArrayList<AdDTO> adDTOs = ads.stream()
                .map(AdDTO::toDto)
                .collect(Collectors.toCollection(ArrayList::new));
        log.info("Запрос на получение всех объявлений!");
        return new AdsDTO(adDTOs.size(), adDTOs);
    }

    @Override
    public AdDTO addAd(AdUpdaterDTO adUpdaterDTO, MultipartFile file,
                       Authentication authentication) {
        User user = userServiceImpl.getUser(authentication.getName());
        Ad ad = new Ad();
        ImageDTO imageDTO = fileServiceImpl.writeImage(file, Paths.IMAGE_DIRECTORY);
        ad.setPrice(adUpdaterDTO.price);
        ad.setTitle(adUpdaterDTO.title);
        ad.setDescription(adUpdaterDTO.description);
        ad.setImagePath(imageDTO.path);
        user.addAd(ad);
        saveAd(ad);
        userServiceImpl.saveUser(user);
        log.info("Объявление " + ad + " добавлено!");
        return AdDTO.toDto(ad);
    }

    @Override
    public Ad getAd(long id) {
        log.info("Запрос на получение объявления id=" + id);
        return adRepository.findById(id).orElseThrow(() -> new EntityNotFound(Ad.class, HttpStatus.NOT_FOUND));
    }

    @Override
    public void removeAd(long id, Authentication authentication) {
        Ad ad = getAd(id);
        Permission.checkPermission(ad.getUser(), authentication);
        User user = userServiceImpl.getUser(authentication.getName());
        user.removeAd(ad);
        removeAd(ad);
        log.info("Удаление объявления id=" + id);
        userServiceImpl.saveUser(user);
    }

    @Override
    public AdDTO updateAd(long id, AdUpdaterDTO adUpdaterDTO,
                          Authentication authentication) {
        Ad ad = getAd(id);
        Permission.checkPermission(ad.getUser(), authentication);
        ad.setPrice(adUpdaterDTO.price);
        ad.setTitle(adUpdaterDTO.title);
        ad.setDescription(adUpdaterDTO.description);
        adRepository.save(ad);
        log.info("Обновление объявления id=" + id);
        return AdDTO.toDto(ad);
    }

    @Override
    public ImageDTO updateAdImage(long id, MultipartFile file,
                                  Authentication authentication) {
        Ad ad = getAd(id);
        Permission.checkPermission(ad.getUser(), authentication);
        fileServiceImpl.removeImage(ad.getImagePath());
        ImageDTO imageDTO = fileServiceImpl.writeImage(file, Paths.IMAGE_DIRECTORY);
        ad.setImagePath(imageDTO.path);
        saveAd(ad);
        log.info("Обновление изображения объявления id=" + id);
        return imageDTO;
    }

    @Override
    public ImageDTO getAdImage(long id) {
        Ad ad = getAd(id);
        String imagePath = ad.getImagePath();
        log.info("Запрос на получение изображения объявления id=" + id);
        return fileServiceImpl.readImage(imagePath);
    }

    @Override
    public CommentsDTO getAdComments(long id) {
        Ad ad = getAd(id);
        ArrayList<CommentDTO> commentDTOs = ad.getComments().stream()
                .map(CommentDTO::toDto)
                .collect(Collectors.toCollection(ArrayList::new));
        log.info("Запрос на получение комментариев к изображению id=" + id);
        return new CommentsDTO(commentDTOs.size(), commentDTOs);
    }

    @Override
    public CommentDTO addCommentToAd(long id, CommentUpdaterDTO commentUpdaterDTO) {
        Ad ad = getAd(id);
        Comment comment = new Comment();
        comment.setText(commentUpdaterDTO.text);
        comment.setCreationTime(System.currentTimeMillis());
        ad.addComment(comment);
        commentServiceImpl.saveComment(comment);
        saveAd(ad);
        log.info("Добавление комментария к объявлению id=" + id);
        return CommentDTO.toDto(comment);
    }

    @Override
    public void removeComment(long id, long commentId, Authentication authentication) {
        Comment comment = commentServiceImpl.getComment(commentId);
        Permission.checkPermission(comment.getAd().getUser(), authentication);
        Ad ad = getAd(id);
        ad.removeComment(comment);
        commentServiceImpl.removeComment(comment);
        saveAd(ad);
        log.info("Удаление комментария commentId=" + commentId);
    }

    @Override
    public CommentDTO updateComment(long id, long commentId, CommentUpdaterDTO commentUpdaterDTO,
                                    Authentication authentication) {
        Ad ad = getAd(id);
        Permission.checkPermission(ad.getUser(), authentication);
        Comment comment = commentServiceImpl.getComment(commentId);
        comment.setText(commentUpdaterDTO.text);
        commentServiceImpl.saveComment(comment);
        log.info("Обновление комментария commentId=" + commentId);
        return CommentDTO.toDto(comment);
    }

    @Override
    public void saveAd(Ad ad) {
        adRepository.save(ad);
    }

    @Override
    public void removeAd(Ad ad) {
        adRepository.delete(ad);
    }
}