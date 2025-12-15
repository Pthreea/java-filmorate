package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void shouldCreateUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);

        assertNotNull(created.getId(), "ID должен быть присвоен");
        assertEquals("user@example.com", created.getEmail());
        assertEquals(1, userController.getAllUsers().size());
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);

        created.setName("Updated Name");
        User updated = userController.updateUser(created);

        assertEquals("Updated Name", updated.getName());
        assertEquals(1, userController.getAllUsers().size());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        User user = new User();
        user.setId(999L);
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(RuntimeException.class, () -> userController.updateUser(user));
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);

        assertEquals("userlogin", created.getName(), "Имя должно быть заменено на логин");
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);

        assertEquals("userlogin", created.getName(), "Имя должно быть заменено на логин");
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsBlank() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("   ");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);

        assertEquals("userlogin", created.getName(), "Имя должно быть заменено на логин");
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 1, 1));

        userController.create(user1);
        userController.create(user2);

        assertEquals(2, userController.getAllUsers().size());
    }

    @Test
    void shouldGetUserById() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);
        User found = userController.getUserById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getEmail(), found.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentUser() {
        assertThrows(RuntimeException.class, () -> userController.getUserById(999L));
    }

    @Test
    void shouldIncrementIdForEachNewUser() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 1, 1));

        User created1 = userController.create(user1);
        User created2 = userController.create(user2);

        assertEquals(1L, created1.getId());
        assertEquals(2L, created2.getId());
    }
}