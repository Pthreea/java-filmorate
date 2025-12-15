package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей. Количество: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        validateAndSetName(user);
        user.setId(currentId++);
        users.put(user.getId(), user);
        log.info("Создан новый пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        validateAndSetName(user);
        if (!users.containsKey(user.getId())) {
            log.warn("Попытка обновить несуществующего пользователя с id: {}", user.getId());
            throw new RuntimeException("Пользователь с id " + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.info("Обновлён пользователь: {}", user);
        return user;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        User user = users.get(id);
        if (user == null) {
            log.warn("Пользователь с id {} не найден", id);
            throw new RuntimeException("Пользователь с id " + id + " не найден");
        }
        log.info("Получен пользователь: {}", user);
        return user;
    }

    private void validateAndSetName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя пустое, используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}