package ru.evgeniy.marketplace.controller.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.evgeniy.marketplace.controller.UserApiController;
import ru.evgeniy.marketplace.dto.ImageDTO;
import ru.evgeniy.marketplace.dto.PasswordUpdaterDTO;
import ru.evgeniy.marketplace.dto.UserDTO;
import ru.evgeniy.marketplace.dto.UserUpdaterDTO;
import ru.evgeniy.marketplace.entity.User;
import ru.evgeniy.marketplace.service.UserService;

@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/users")
public class UserController implements UserApiController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/set_password")
    public ResponseEntity<?> setPassword(@RequestBody PasswordUpdaterDTO passwordUpdaterDTO,
                                         Authentication authentication) {
        userService.setPassword(passwordUpdaterDTO, authentication);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public UserDTO getUser(Authentication authentication) {
        User user = userService.getUser(authentication.getName());
        return UserDTO.toUserDto(user);
    }

    @PatchMapping("/me")
    public UserUpdaterDTO updateInfo(@RequestBody UserUpdaterDTO userUpdaterDTO,
                                     Authentication authentication) {
        userService.updateInfo(userUpdaterDTO, authentication);
        return userUpdaterDTO;
    }

    @PatchMapping("/me/image")
    public ResponseEntity<?> updateAvatar(@RequestParam("image") MultipartFile file,
                                          Authentication authentication) {
        userService.updateAvatar(file, authentication);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/image")
    public ResponseEntity<byte[]> getAvatar(Authentication authentication) {
        ImageDTO avatar = userService.getAvatar(authentication);
        byte[] bytes = avatar.bytes;
        return ResponseEntity.ok()
                .contentLength(bytes.length)
                .contentType(avatar.mediaType)
                .body(bytes);
    }
}