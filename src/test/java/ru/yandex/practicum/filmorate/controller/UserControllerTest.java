package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setId(1L);
        validUser.setEmail("test@example.com");
        validUser.setLogin("testuser");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createUser_Valid_ReturnsCreatedUser() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(validUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.login").value("testuser"));
    }

    @Test
    void createUser_EmptyEmail_ReturnsBadRequest() throws Exception {
        validUser.setEmail("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        validUser.setEmail("invalid-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_EmptyLogin_ReturnsBadRequest() throws Exception {
        validUser.setLogin("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_LoginWithSpaces_ReturnsBadRequest() throws Exception {
        validUser.setLogin("user name");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_FutureBirthday_ReturnsBadRequest() throws Exception {
        validUser.setBirthday(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_Valid_ReturnsUpdatedUser() throws Exception {
        when(userService.updateUser(any(User.class))).thenReturn(validUser);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllUsers_ReturnsListOfUsers() throws Exception {
        List<User> users = Arrays.asList(validUser);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    void getUserById_ExistingUser_ReturnsUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(validUser);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void addFriend_ValidIds_ReturnsOk() throws Exception {
        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());
    }

    @Test
    void removeFriend_ValidIds_ReturnsOk() throws Exception {
        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());
    }

    @Test
    void getFriends_ExistingUser_ReturnsFriendsList() throws Exception {
        List<User> friends = Arrays.asList(validUser);
        when(userService.getFriends(1L)).thenReturn(friends);

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getCommonFriends_ValidIds_ReturnsCommonFriendsList() throws Exception {
        List<User> commonFriends = Arrays.asList(validUser);
        when(userService.getCommonFriends(1L, 2L)).thenReturn(commonFriends);

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}