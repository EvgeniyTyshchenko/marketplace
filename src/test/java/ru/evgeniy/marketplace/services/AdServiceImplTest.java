package ru.evgeniy.marketplace.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import ru.evgeniy.marketplace.dto.*;
import ru.evgeniy.marketplace.entity.Ad;
import ru.evgeniy.marketplace.entity.Comment;
import ru.evgeniy.marketplace.entity.User;
import ru.evgeniy.marketplace.repository.AdRepository;
import ru.evgeniy.marketplace.service.Impl.AdServiceImpl;
import ru.evgeniy.marketplace.service.Impl.CommentServiceImpl;
import ru.evgeniy.marketplace.service.Impl.FileServiceImpl;
import ru.evgeniy.marketplace.service.Impl.UserServiceImpl;
import ru.evgeniy.marketplace.utils.Paths;
import ru.evgeniy.marketplace.utils.Permission;
import ru.evgeniy.marketplace.utils.exception.EntityNotFound;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdServiceImplTest {

    @Mock
    private UserServiceImpl userServiceImpl;
    @Mock
    private AdRepository adRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FileServiceImpl fileServiceImpl;
    @Mock
    private CommentServiceImpl commentServiceImpl;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private AdServiceImpl adServiceImpl;
    private static User user;
    private static String userName = "test@mail.ru";
    private static Ad ad;
    private static Comment comment;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername(userName);
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setAvatarPath("path/to/avatar.jpg");

        ad = new Ad();
        ad.setId(1L);
        ad.setImagePath("Путь к изображению");
        ad.setUser(user);

        comment = new Comment();
        comment.setId(1);
        comment.setText("Комментарий");
        comment.setAd(ad);
    }

    @Test
    void shouldAddAd() {
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userName);
        when(userServiceImpl.getUser(userName)).thenReturn(user);

        AdUpdaterDTO adUpdaterDTO = new AdUpdaterDTO();
        adUpdaterDTO.setPrice(100);
        adUpdaterDTO.setTitle("Тестовое объявление");
        adUpdaterDTO.setDescription("Тестовое объявление");
        MultipartFile file = mock(MultipartFile.class);
        ImageDTO imageDTO = new ImageDTO(new byte[]{}, MediaType.IMAGE_JPEG, "/ads/0/image");

        when(fileServiceImpl.writeImage(file, Paths.IMAGE_DIRECTORY)).thenReturn(imageDTO);

        AdDTO result = adServiceImpl.addAd(adUpdaterDTO, file, authentication);
        assertNotNull(result);
        assertEquals(adUpdaterDTO.getPrice(), result.getPrice());
        assertEquals(adUpdaterDTO.getTitle(), result.getTitle());
        assertEquals(imageDTO.getPath(), result.getImagePath());
    }

    @Test
    void shouldGetAdById() {
        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        assertEquals(ad, adServiceImpl.getAd(1L));
    }

    @Test
    void shouldGetEntityNotFound() {
        when(adRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFound.class, () -> adServiceImpl.getAd(1L));
    }

    @Test
    void shouldRemoveAd() {
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userName);
        when(userServiceImpl.getUser(userName)).thenReturn(user);

        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        adServiceImpl.removeAd(1L, authentication);
        verify(adRepository, times(1)).delete(ad);
    }

    @Test
    void shouldUpdateAd() {
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userName);
        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));

        AdUpdaterDTO adUpdaterDTO = new AdUpdaterDTO();
        adUpdaterDTO.setPrice(100);
        adUpdaterDTO.setTitle("Тест");
        adUpdaterDTO.setDescription("Тест");

        AdDTO updatedAd = adServiceImpl.updateAd(1L, adUpdaterDTO, authentication);

        verify(adRepository, times(1)).save(ad);
        assertEquals(adUpdaterDTO.title, ad.getTitle());
        assertEquals(adUpdaterDTO.description, ad.getDescription());
        assertEquals(adUpdaterDTO.price, ad.getPrice());
        assertEquals(ad.getUser(), user);
        assertEquals(updatedAd.getTitle(), ad.getTitle());
        assertEquals(updatedAd.getPrice(), ad.getPrice());
    }

    @Test
    void shouldGetAdImage() {
        ImageDTO expectedImage = new ImageDTO();
        when(adRepository.findById(ad.getId())).thenReturn(Optional.of(ad));
        when(fileServiceImpl.readImage(ad.getImagePath())).thenReturn(expectedImage);

        ImageDTO actualImage = adServiceImpl.getAdImage(ad.getId());
        verify(fileServiceImpl, times(1)).readImage(ad.getImagePath());
        Assertions.assertEquals(expectedImage, actualImage);
    }

    @Test
    void shouldAddCommentToAd() {
        when(adRepository.findById(ad.getId())).thenReturn(Optional.of(ad));
        when(commentServiceImpl.saveComment(any(Comment.class))).thenReturn(comment);

        CommentUpdaterDTO commentUpdaterDTO = new CommentUpdaterDTO();
        commentUpdaterDTO.setText("Тестовый комментарий");
        comment.setText(commentUpdaterDTO.getText());
        comment.setCreationTime(System.currentTimeMillis());

        CommentDTO result = adServiceImpl.addCommentToAd(ad.getId(), commentUpdaterDTO);
        assertEquals(comment.getText(), result.getText());
        verify(adRepository, times(1)).findById(ad.getId());
        verify(commentServiceImpl, times(1)).saveComment(any(Comment.class));
        verify(adRepository, times(1)).save(ad);
    }

    @Test
    void shouldRemoveComment() {
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userName);
        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        Mockito.when(commentServiceImpl.getComment(1)).thenReturn(comment);
        Permission.checkPermission(comment.getAd().getUser(), authentication);

        adServiceImpl.removeComment(1, 1, authentication);

        Assertions.assertFalse(ad.getComments().contains(comment));
        Mockito.verify(commentServiceImpl).removeComment(comment);
    }

    @Test
    void shouldUpdateComment() {
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userName);
        when(adRepository.findById(ad.getId())).thenReturn(Optional.of(ad));
        when(commentServiceImpl.getComment(comment.getId())).thenReturn(comment);
        when(commentServiceImpl.saveComment(comment)).thenReturn(comment);

        CommentUpdaterDTO commentUpdaterDTO = new CommentUpdaterDTO();
        commentUpdaterDTO.text = "Новый комментарий";

        CommentDTO updatedComment = adServiceImpl.updateComment(ad.getId(), comment.getId(),
                commentUpdaterDTO, authentication);

        assertEquals(commentUpdaterDTO.text, updatedComment.getText());
        verify(adRepository, times(1)).findById(ad.getId());
        verify(commentServiceImpl, times(1)).getComment(comment.getId());
        verify(commentServiceImpl, times(1)).saveComment(comment);
    }
}