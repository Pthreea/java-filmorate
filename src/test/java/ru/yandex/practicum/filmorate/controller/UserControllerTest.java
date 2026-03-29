package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_Valid_ReturnsCreatedUser() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        String userJson = objectMapper.writeValueAsString(user);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void createUser_EmptyName_UsesLogin() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName(""); // Пустое имя
        user.setBirthday(LocalDate.of(1990, 1, 1));

        String userJson = objectMapper.writeValueAsString(user);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("testuser")); // Имя = логин
    }

    @Test
    void createUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        User user = new User();
        user.setEmail("invalid-email"); // Нет @
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        String userJson = objectMapper.writeValueAsString(user);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_LoginWithSpaces_ReturnsBadRequest() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test user"); // Пробел в логине
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        String userJson = objectMapper.writeValueAsString(user);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_FutureBirthday_ReturnsBadRequest() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.now().plusDays(1)); // Дата в будущем

        String userJson = objectMapper.writeValueAsString(user);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_Valid_ReturnsUpdatedUser() throws Exception {
        // Given - создаем пользователя
        User user = new User();
        user.setEmail("original@example.com");
        user.setLogin("original");
        user.setName("Original Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        String createJson = objectMapper.writeValueAsString(user);

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = objectMapper.readValue(response, User.class);

        // Обновляем пользователя
        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@example.com");
        String updateJson = objectMapper.writeValueAsString(createdUser);

        // When & Then
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void getAllUsers_ReturnsListOfUsers() throws Exception {
        // Given - создаем несколько пользователей
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].login").value("user1"))
                .andExpect(jsonPath("$[1].login").value("user2"));
    }

    @Test
    void getUserById_ValidId_ReturnsUser() throws Exception {
        // Given - создаем пользователя
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = objectMapper.readValue(response, User.class);

        // When & Then
        mockMvc.perform(get("/users/" + createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.login").value("testuser"));
    }

    @Test
    void addFriend_ValidIds_ReturnsNoContent() throws Exception {
        // Given - создаем двух пользователей
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        String response1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser1 = objectMapper.readValue(response1, User.class);

        String response2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser2 = objectMapper.readValue(response2, User.class);

        // When & Then
        mockMvc.perform(put("/users/" + createdUser1.getId() + "/friends/" + createdUser2.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeFriend_ValidIds_ReturnsNoContent() throws Exception {
        // Given - создаем двух пользователей и добавляем дружбу
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        String response1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser1 = objectMapper.readValue(response1, User.class);

        String response2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser2 = objectMapper.readValue(response2, User.class);

        // Добавляем дружбу
        mockMvc.perform(put("/users/" + createdUser1.getId() + "/friends/" + createdUser2.getId()))
                .andExpect(status().isNoContent());

        // When & Then - удаляем дружбу
        mockMvc.perform(delete("/users/" + createdUser1.getId() + "/friends/" + createdUser2.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getFriends_ReturnsListOfFriends() throws Exception {
        // Given - создаем пользователей и добавляем дружбу
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        String response1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser1 = objectMapper.readValue(response1, User.class);

        String response2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser2 = objectMapper.readValue(response2, User.class);

        mockMvc.perform(put("/users/" + createdUser1.getId() + "/friends/" + createdUser2.getId()))
                .andExpect(status().isNoContent());

        // When & Then
        mockMvc.perform(get("/users/" + createdUser1.getId() + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].login").value("user2"));
    }

    @Test
    void getCommonFriends_ReturnsListOfCommonFriends() throws Exception {
        // Given - создаем трех пользователей
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        User commonFriend = new User();
        commonFriend.setEmail("common@example.com");
        commonFriend.setLogin("common");
        commonFriend.setName("Common Friend");
        commonFriend.setBirthday(LocalDate.of(1992, 3, 3));

        String response1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser1 = objectMapper.readValue(response1, User.class);

        String response2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser2 = objectMapper.readValue(response2, User.class);

        String response3 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commonFriend)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdCommonFriend = objectMapper.readValue(response3, User.class);

        // Оба пользователя добавляют общего друга
        mockMvc.perform(put("/users/" + createdUser1.getId() + "/friends/" + createdCommonFriend.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/users/" + createdUser2.getId() + "/friends/" + createdCommonFriend.getId()))
                .andExpect(status().isNoContent());

        // When & Then
        mockMvc.perform(get("/users/" + createdUser1.getId() + "/friends/common/" + createdUser2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].login").value("common"));
    }
}