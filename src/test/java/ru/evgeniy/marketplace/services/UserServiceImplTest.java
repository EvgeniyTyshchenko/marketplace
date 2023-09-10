package ru.evgeniy.marketplace.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.evgeniy.marketplace.dto.*;
import ru.evgeniy.marketplace.entity.User;
import ru.evgeniy.marketplace.repository.UserRepository;
import ru.evgeniy.marketplace.service.Impl.FileServiceImpl;
import ru.evgeniy.marketplace.service.Impl.UserServiceImpl;
import ru.evgeniy.marketplace.utils.exception.EntityNotFound;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FileServiceImpl fileServiceImpl;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private UserServiceImpl userServiceImpl;
    private static User user;
    private static String userName = "test@mail.ru";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername(userName);
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setAvatarPath("path/to/avatar.jpg");
    }

    @Test
    void shouldGetTheUserByUserName() {
        when(userRepository.findByUsername(userName)).thenReturn(Optional.of(user));
        User result = userServiceImpl.getUser(userName);
        assertEquals(user, result);
        verify(userRepository, times(1)).findByUsername(userName);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionIfUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getName()).thenReturn("nonExistingUser");

        PasswordUpdaterDTO passwordUpdaterDTO = new PasswordUpdaterDTO();
        passwordUpdaterDTO.setNewPassword("newPassword");

        assertThrows(EntityNotFound.class,
                () -> userServiceImpl.setPassword(passwordUpdaterDTO, authentication));
    }

    @Test
    void shouldUpdateUserInformation() {
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userName);
        when(userRepository.findByUsername(userName)).thenReturn(Optional.of(user));

        UserUpdaterDTO userUpdaterDTO = new UserUpdaterDTO();
        userUpdaterDTO.setFirstName("Петр");
        userUpdaterDTO.setLastName("Петров");
        userUpdaterDTO.setPhone("+79881002233");

        userServiceImpl.updateInfo(userUpdaterDTO, authentication);

        assertEquals("Петр", user.getFirstName());
        assertEquals("Петров", user.getLastName());
        assertEquals("+79881002233", user.getPhone());
    }

    @Test
    void shouldGetAvatar() {
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userName);
        when(userRepository.findByUsername(userName)).thenReturn(Optional.of(user));

        ImageDTO expectedImage = new ImageDTO();
        when(fileServiceImpl.readImage(user.getAvatarPath())).thenReturn(expectedImage);

        ImageDTO actualImage = userServiceImpl.getAvatar(authentication);

        verify(fileServiceImpl).readImage(user.getAvatarPath());
        assertEquals(expectedImage, actualImage);
    }

    @Test
    void shouldBeAnExceptionWhenUpdatingAvatar() {
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userName);
        when(userRepository.findByUsername(userName)).thenReturn(Optional.of(user));

        MockMultipartFile file = new MockMultipartFile("avatar.jpg", new byte[0]);
        when(userServiceImpl.getUser(userName)).thenThrow(new EntityNotFound(User.class, HttpStatus.NOT_FOUND));

        assertThrows(EntityNotFound.class, () -> userServiceImpl.updateAvatar(file, authentication));
    }
}