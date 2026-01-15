package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.debug("Получен запрос POST /users: {}", user);
        User createdUser = userService.createUser(user);
        log.info("Создан пользователь с id={}", createdUser.getId());
        return createdUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.debug("Получен запрос PUT /users: {}", user);
        User updatedUser = userService.updateUser(user);
        log.info("Обновлён пользователь с id={}", updatedUser.getId());
        return updatedUser;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.debug("Получен запрос GET /users");
        List<User> users = userService.getAllUsers();
        log.info("Возвращено {} пользователей", users.size());
        return users;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.debug("Получен запрос GET /users/{}", id);
        User user = userService.getUserById(id);
        log.info("Найден пользователь с id={}", id);
        return user;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("Получен запрос PUT /users/{}/friends/{}", id, friendId);
        userService.addFriend(id, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("Получен запрос DELETE /users/{}/friends/{}", id, friendId);
        userService.removeFriend(id, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        log.debug("Получен запрос GET /users/{}/friends", id);
        List<User> friends = userService.getFriends(id);
        log.info("Возвращено {} друзей пользователя {}", friends.size(), id);
        return friends;
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.debug("Получен запрос GET /users/{}/friends/common/{}", id, otherId);
        List<User> commonFriends = userService.getCommonFriends(id, otherId);
        log.info("Возвращено {} общих друзей для пользователей {} и {}", commonFriends.size(), id, otherId);
        return commonFriends;
    }
}