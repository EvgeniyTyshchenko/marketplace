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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.evgeniy.marketplace.dto.*;
import ru.evgeniy.marketplace.entity.Ad;
import ru.evgeniy.marketplace.entity.Comment;
import ru.evgeniy.marketplace.entity.Role;
import ru.evgeniy.marketplace.entity.User;
import ru.evgeniy.marketplace.service.AdService;
import ru.evgeniy.marketplace.service.CommentService;
import ru.evgeniy.marketplace.service.RoleService;
import ru.evgeniy.marketplace.service.UserService;
import ru.evgeniy.marketplace.utils.Paths;
import ru.evgeniy.marketplace.utils.exception.EntityNotFound;

import javax.transaction.Transactional;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "test@yandex.ru", password = "password", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@SpringBootTest
@AutoConfigureMockMvc
class AdControllerTest {

    @Autowired
    private MockMvc mokMvc;
    @Autowired
    private AdService adService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void fillDataBase() {
        User user = createUser("test@yandex.ru", "+79881002233");
        Ad ad = createAd();
        Comment comment = createComment();
        ad.addComment(comment);
        comment.setAd(ad);
        userService.saveUser(user);
        user.addAd(ad);
        adService.saveAd(ad);
        ad.addComment(comment);
        commentService.saveComment(comment);
        userService.saveUser(user);
    }

    @Test
    void shouldReturnAllAds() throws Exception {
        Ad ad = adService.getAd(1L);
        AdsDTO adsDTO = new AdsDTO(1, List.of(AdDTO.toDto(ad)));
        String json = objectMapper.writeValueAsString(adsDTO);

        mokMvc.perform(MockMvcRequestBuilders.get("/ads"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(json));
    }

    @Test
    void shouldCreateAd() throws Exception {
        AdUpdaterDTO adUpdaterDTO = new AdUpdaterDTO();
        adUpdaterDTO.title = "Заголовок объявления";
        adUpdaterDTO.description = "Описание объявления";
        adUpdaterDTO.price = 1000;
        String json = objectMapper.writeValueAsString(adUpdaterDTO);
        byte[] bytes = Files.readAllBytes(Path.of(Paths.STANDARD_AD_IMAGE_PATH));

        MockMultipartFile mockMultipartFile = new MockMultipartFile("properties", "json.json",
                MediaType.APPLICATION_JSON_VALUE, json.getBytes());
        MockMultipartFile mockMultipartFile1 = new MockMultipartFile("image", "image.jpeg",
                MediaType.IMAGE_JPEG_VALUE, bytes);

        mokMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, "/ads")
                        .file(mockMultipartFile)
                        .file(mockMultipartFile1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(1000.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Заголовок объявления"))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));

        Ad ad = adService.getAd(2L);
        Assertions.assertEquals(2, adService.getAllAds().count);
        Assertions.assertEquals(1, ad.getUser().getId());
        Assertions.assertTrue(Files.exists(Path.of(ad.getImagePath())));

        Files.deleteIfExists(Path.of(ad.getImagePath()));
    }

    @Test
    void shouldReturnAd() throws Exception {
        mokMvc.perform(MockMvcRequestBuilders.get("/ads/{id}", 1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(1000.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Заголовок объявления"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.image").value(String.format(Paths.GET_IMAGE_ENDPOINT, 1)));
    }

    @Test
    @Transactional
    void shouldDeleteAd() throws Exception {
        mokMvc.perform(MockMvcRequestBuilders.delete("/ads/{id}", 1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Assertions.assertEquals(0, adService.getAllAds().count);
        Assertions.assertThrows(EntityNotFound.class, () -> commentService.getComment(1));
        Assertions.assertEquals(0, userService.getUser("test@yandex.ru").getAds().size());
    }

    @Test
    @WithMockUser(username = "user2@yandex.ru", password = "password")
    void shouldTellThatUserDoesNotHavePermission() throws Exception {
        mokMvc.perform(MockMvcRequestBuilders.delete("/ads/{id}", 1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertEquals(1, adService.getAllAds().count);
    }

    @Test
    @WithMockUser(username = "user2@yandex.ru", password = "password", roles = "ADMIN")
    void shouldDeleteAdBecauseOfTheAdminRole() throws Exception {
        User user = createUser("user2@yandex.ru", "+79880002233");
        userService.saveUser(user);

        mokMvc.perform(MockMvcRequestBuilders.delete("/ads/{id}", 1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Assertions.assertEquals(0, adService.getAllAds().count);
        Assertions.assertThrows(EntityNotFound.class, () -> commentService.getComment(1));
    }

    @Test
    void shouldUpdateAd() throws Exception {
        AdUpdaterDTO adUpdaterDTO = new AdUpdaterDTO();
        adUpdaterDTO.title = "Заголовок объявления";
        adUpdaterDTO.description = "Описание объявления";
        adUpdaterDTO.price = 1000;
        String json = objectMapper.writeValueAsString(adUpdaterDTO);

        mokMvc.perform(MockMvcRequestBuilders.patch("/ads/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.getBytes()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(1000.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Заголовок объявления"));

        Ad ad = adService.getAd(1L);
        Assertions.assertEquals("Описание объявления", ad.getDescription());
    }

    @Test
    void shouldReturnAllUserAds() throws Exception {
        String json = objectMapper.writeValueAsString(adService.getAllAds());

        mokMvc.perform(MockMvcRequestBuilders.get("/ads/me"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(json))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldUpdateAdImage() throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of(Paths.STANDARD_AD_IMAGE_PATH));
        File file = new File(Paths.STANDARD_AD_IMAGE_PATH);
        String name = file.getName();

        MockMultipartFile mockMultipartFile = new MockMultipartFile("image", name, MediaType.IMAGE_JPEG_VALUE, bytes);

        mokMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/ads/{id}/image", 1)
                        .file(mockMultipartFile))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(MockMvcResultMatchers.content().bytes(bytes));

        Ad ad = adService.getAd(1L);
        Assertions.assertTrue(ad.getImagePath().contains(name));

        Files.deleteIfExists(Path.of(ad.getImagePath()));
    }

    @Test
    void shouldGetAdImage() throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of(Paths.STANDARD_AD_IMAGE_PATH));

        mokMvc.perform(MockMvcRequestBuilders.get("/ads/{id}/image", 1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(MockMvcResultMatchers.content().bytes(bytes));
    }

    @Test
    @Transactional
    void shouldReturnAdComments() throws Exception {
        CommentsDTO adComments = adService.getAdComments(1L);
        String json = objectMapper.writeValueAsString(adComments);

        MvcResult mvcResult = mokMvc.perform(MockMvcRequestBuilders.get("/ads/{id}/comments", 1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(json))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        CommentsDTO commentsDTO = objectMapper.readValue(contentAsString, CommentsDTO.class);
        Assertions.assertEquals(1, commentsDTO.count);
    }

    @Test
    @Transactional
    void shouldAddComment() throws Exception {
        CommentUpdaterDTO commentUpdaterDTO = new CommentUpdaterDTO();
        commentUpdaterDTO.text = "Text";
        String json = objectMapper.writeValueAsString(commentUpdaterDTO);

        mokMvc.perform(MockMvcRequestBuilders.post("/ads/{id}/comments", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.getBytes()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pk").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value("Text"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.author").value(1))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));

        Assertions.assertEquals(2, adService.getAdComments(1L).count);
    }

    @Test
    @Transactional
    void shouldDeleteComment() throws Exception {
        mokMvc.perform(MockMvcRequestBuilders.delete("/ads/{id}/comments/{commentId}", 1, 1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        Assertions.assertThrows(EntityNotFound.class, () -> commentService.getComment(1L));
        Assertions.assertEquals(0, adService.getAd(1L).getComments().size());
    }

    @Test
    @Transactional
    @WithMockUser(username = "user2@yandex.ru", password = "password")
    void shouldThrowException() throws Exception {
        mokMvc.perform(MockMvcRequestBuilders.delete("/ads/{id}/comments/{commentId}", 1, 1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is(403));

        Assertions.assertNotNull(commentService.getComment(1L));
        Assertions.assertEquals(1, adService.getAd(1L).getComments().size());
    }

    @Test
    void shouldUpdateComment() throws Exception {
        CommentUpdaterDTO commentUpdaterDTO = new CommentUpdaterDTO();
        commentUpdaterDTO.text = "Text";
        String json = objectMapper.writeValueAsString(commentUpdaterDTO);

        Comment comment = commentService.getComment(1L);
        String text = comment.getText();

        mokMvc.perform(MockMvcRequestBuilders.patch("/ads/{id}/comments/{commentId}", 1, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.getBytes()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorFirstName").value("User"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pk").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value("Text"));

        Comment updatedComment = commentService.getComment(1L);
        Assertions.assertNotEquals(text, updatedComment.getText());
    }

    private User createUser(String username, String phone) {
        Role userRole = roleService.getRole("ROLE_USER");
        User user = new User();
        user.setEmail(username);
        user.setFirstName("User");
        user.setLastName("UserTest");
        user.setPhone(phone);
        user.setPassword("password");
        user.setUsername(username);
        user.setAvatarPath(Paths.STANDARD_AVATAR_PATH);
        user.getRoles().add(userRole);
        return user;
    }

    private Ad createAd() {
        Ad ad = new Ad();
        ad.setTitle("Заголовок объявления");
        ad.setDescription("Описание объявления");
        ad.setPrice(1000.0);
        ad.setImagePath(Paths.STANDARD_AD_IMAGE_PATH);
        return ad;
    }

    private Comment createComment() {
        Comment comment = new Comment();
        comment.setText("Text_test");
        comment.setCreationTime(12345678);
        return comment;
    }
}