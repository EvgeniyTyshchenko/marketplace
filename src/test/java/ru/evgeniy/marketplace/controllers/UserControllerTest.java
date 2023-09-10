package ru.evgeniy.marketplace.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.evgeniy.marketplace.dto.PasswordUpdaterDTO;
import ru.evgeniy.marketplace.dto.UserUpdaterDTO;
import ru.evgeniy.marketplace.entity.Role;
import ru.evgeniy.marketplace.entity.User;
import ru.evgeniy.marketplace.service.RoleService;
import ru.evgeniy.marketplace.service.UserService;
import ru.evgeniy.marketplace.utils.Paths;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "test@yandex.ru", password = "password", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mokMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void fillDataBase() {
        User user = createUser("test@yandex.ru", "+79881002233");
        userService.saveUser(user);
    }

    @Test
    void shouldSetPassword() throws Exception {
        String password = userService.getUser("test@yandex.ru").getPassword();
        PasswordUpdaterDTO passwordUpdaterDTO = new PasswordUpdaterDTO();
        passwordUpdaterDTO.newPassword = "12345678";
        passwordUpdaterDTO.currentPassword = "01234567";
        String json = objectMapper.writeValueAsString(passwordUpdaterDTO);

        mokMvc.perform(MockMvcRequestBuilders.post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.getBytes()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        User user = userService.getUser("test@yandex.ru");
        Assertions.assertNotEquals(password, user.getPassword());
    }

    @Test
    void shouldGetUser() throws Exception {
        mokMvc.perform(MockMvcRequestBuilders.get("/users/me"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("TestUser"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.image").value(Paths.GET_AVATAR_ENDPOINT))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value("+79881002233"));
    }

    @Test
    void shouldUpdateInfo() throws Exception {
        UserUpdaterDTO userUpdaterDTO = new UserUpdaterDTO();
        userUpdaterDTO.firstName = "TestUser2";
        userUpdaterDTO.lastName = "TestUser2";
        userUpdaterDTO.phone = "+79881002233";
        String json = objectMapper.writeValueAsString(userUpdaterDTO);
        User user = userService.getUser("test@yandex.ru");
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String phone = user.getPhone();

        mokMvc.perform(MockMvcRequestBuilders.patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.getBytes()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("TestUser2"));

        User updatedUser = userService.getUser("test@yandex.ru");
        Assertions.assertNotEquals(firstName, updatedUser.getFirstName());
        Assertions.assertNotEquals(lastName, updatedUser.getLastName());
        Assertions.assertEquals(phone, updatedUser.getPhone());
    }

    @Test
    void shouldUpdateAvatar() throws Exception {
        File file = new File(Paths.STANDARD_AD_IMAGE_PATH);
        String name = file.getName();
        byte[] bytes = Files.readAllBytes(Path.of(Paths.STANDARD_AD_IMAGE_PATH));
        String avatarPath = userService.getUser("test@yandex.ru").getAvatarPath();

        MockMultipartFile mockMultipartFile = new MockMultipartFile("image", name, MediaType.IMAGE_JPEG_VALUE, bytes);

        mokMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/users/me/image")
                        .file(mockMultipartFile))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        User user = userService.getUser("test@yandex.ru");
        Assertions.assertNotEquals(avatarPath, user.getAvatarPath());
        Assertions.assertTrue(user.getAvatarPath().contains(name));

        Files.deleteIfExists(Path.of(user.getAvatarPath()));
    }

    @Test
    void shouldGetAvatar() throws Exception {
        String avatarPath = userService.getUser("test@yandex.ru").getAvatarPath();
        byte[] bytes = Files.readAllBytes(Path.of(avatarPath));

        mokMvc.perform(MockMvcRequestBuilders.get("/users/me/image"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().bytes(bytes))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG));
    }

    private User createUser(String username, String phone) {
        Role userRole = roleService.getRole("ROLE_USER");
        User user = new User();
        user.setEmail(username);
        user.setFirstName("TestUser");
        user.setLastName("TestUser");
        user.setPhone(phone);
        user.setPassword("password");
        user.setUsername(username);
        user.setAvatarPath(Paths.STANDARD_AVATAR_PATH);
        user.getRoles().add(userRole);
        return user;
    }
}