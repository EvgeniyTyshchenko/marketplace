package ru.evgeniy.marketplace.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.evgeniy.marketplace.dto.*;
import ru.evgeniy.marketplace.entity.User;
import ru.evgeniy.marketplace.entity.Ad;
import ru.evgeniy.marketplace.service.UserService;
import ru.evgeniy.marketplace.utils.exception.EntityNotFound;
import ru.evgeniy.marketplace.repository.UserRepository;
import ru.evgeniy.marketplace.utils.Paths;

import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FileServiceImpl fileServiceImpl;
    private final PasswordEncoder encoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, FileServiceImpl fileServiceImpl,
                           PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.fileServiceImpl = fileServiceImpl;
        this.encoder = encoder;
    }

    @Override
    public User getUser(String username) {
        log.info("Запрос на получение пользователя " + username);
        return userRepository.findByUsername(username).orElseThrow(()
                -> new EntityNotFound(User.class, HttpStatus.NOT_FOUND));
    }

    @Override
    public void setPassword(PasswordUpdaterDTO passwordUpdaterDTO, Authentication authentication) {
        User user = getUser(authentication.getName());
        String encodedPassword = encoder.encode(passwordUpdaterDTO.newPassword);
        user.setPassword(encodedPassword);
        saveUser(user);
        log.info("Запрос на обновление пароля " + user.getFirstName() + user.getLastName());
    }

    @Override
    public void updateInfo(UserUpdaterDTO userUpdaterDTO, Authentication authentication) {
        User user = getUser(authentication.getName());
        user.setFirstName(userUpdaterDTO.firstName);
        user.setLastName(userUpdaterDTO.lastName);
        user.setPhone(userUpdaterDTO.phone);
        saveUser(user);
        log.info("Запрос на обновление данных " + user.getFirstName() + user.getLastName());
    }

    @Override
    public ImageDTO getAvatar(Authentication authentication) {
        User user = getUser(authentication.getName());
        log.info("Запрос на получение аватара " + user.getFirstName() + user.getLastName());
        return fileServiceImpl.readImage(user.getAvatarPath());
    }

    @Override
    public ImageDTO updateAvatar(MultipartFile file, Authentication authentication) {
        User user = getUser(authentication.getName());
        fileServiceImpl.removeImage(user.getAvatarPath());
        ImageDTO imageDTO = fileServiceImpl.writeImage(file, Paths.AVATAR_DIRECTORY);
        user.setAvatarPath(imageDTO.path);
        saveUser(user);
        log.info("Обновление аватара " + user.getFirstName() + user.getLastName());
        return imageDTO;
    }

    @Override
    public AdsDTO getUserAds(Authentication authentication) {
        User user = getUser(authentication.getName());
        Set<Ad> ads = user.getAds();
        LinkedList<AdDTO> adDTOs = ads.stream()
                .map(AdDTO::toDto)
                .collect(Collectors.toCollection(LinkedList::new));
        log.info("Запрос на получение объявлений пользователя!");
        return new AdsDTO(adDTOs.size(), adDTOs);
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }
}