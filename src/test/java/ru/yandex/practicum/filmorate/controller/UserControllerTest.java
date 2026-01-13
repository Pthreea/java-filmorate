package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;
    private InMemoryUserStorage userStorage;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        userController = new UserController(userStorage, userService);
    }

    @Test
    void shouldCreateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.createUser(user);

        assertNotNull(createdUser.getId());
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("testLogin", createdUser.getLogin());
        assertEquals(1, userController.getAllUsers().size());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName(""); // Пустое имя
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.createUser(user);

        assertEquals("testLogin", createdUser.getName());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName(null); // Null имя
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.createUser(user);

        assertEquals("testLogin", createdUser.getName());
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("original@example.com");
        user.setLogin("originalLogin");
        user.setName("Original Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.createUser(user);

        createdUser.setEmail("updated@example.com");
        createdUser.setName("Updated Name");
        User updatedUser = userController.updateUser(createdUser);

        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals(createdUser.getId(), updatedUser.getId());
    }

    @Test
    void shouldThrowExceptionWhenUpdateNonExistentUser() {
        User user = new User();
        user.setId(999L);
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(NotFoundException.class, () -> userController.updateUser(user));
    }

    @Test
    void shouldGetUserById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.createUser(user);
        User foundUser = userController.getUserById(createdUser.getId());

        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenGetNonExistentUser() {
        assertThrows(NotFoundException.class, () -> userController.getUserById(999L));
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("login1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("login2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        userController.createUser(user1);
        userController.createUser(user2);

        assertEquals(2, userController.getAllUsers().size());
    }

    @Test
    void shouldAddFriend() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("login1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("login2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        User createdUser1 = userController.createUser(user1);
        User createdUser2 = userController.createUser(user2);

        userController.addFriend(createdUser1.getId(), createdUser2.getId());

        assertEquals(1, userController.getFriends(createdUser1.getId()).size());
        assertEquals(1, userController.getFriends(createdUser2.getId()).size());
    }

    @Test
    void shouldRemoveFriend() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("login1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("login2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        User createdUser1 = userController.createUser(user1);
        User createdUser2 = userController.createUser(user2);

        userController.addFriend(createdUser1.getId(), createdUser2.getId());
        userController.removeFriend(createdUser1.getId(), createdUser2.getId());

        assertEquals(0, userController.getFriends(createdUser1.getId()).size());
        assertEquals(0, userController.getFriends(createdUser2.getId()).size());
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("login1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("login2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        User user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setLogin("login3");
        user3.setName("User 3");
        user3.setBirthday(LocalDate.of(1992, 1, 1));

        User createdUser1 = userController.createUser(user1);
        User createdUser2 = userController.createUser(user2);
        User createdUser3 = userController.createUser(user3);

        // User1 и User2 добавляют User3 в друзья
        userController.addFriend(createdUser1.getId(), createdUser3.getId());
        userController.addFriend(createdUser2.getId(), createdUser3.getId());

        // Проверяем общих друзей
        assertEquals(1, userController.getCommonFriends(createdUser1.getId(), createdUser2.getId()).size());
        assertEquals(createdUser3.getId(),
                userController.getCommonFriends(createdUser1.getId(), createdUser2.getId()).get(0).getId());
    }
}