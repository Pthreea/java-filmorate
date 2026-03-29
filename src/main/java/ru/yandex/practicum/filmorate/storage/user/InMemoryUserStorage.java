package ru.yandex.practicum.filmorate.storage.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long currentId = 1L;

    @Override
    public User create(User user) {
        user.setId(currentId++);
        users.put(user.getId(), user);
        log.debug("Создан пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new RuntimeException("Пользователь с ID " + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.debug("Обновлен пользователь: {}", user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findFriends(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            return new ArrayList<>();
        }

        if (user.getFriends() == null) {
            return new ArrayList<>();
        }

        return user.getFriends().keySet().stream()
                .map(users::get)
                .filter(friend -> friend != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findCommonFriends(Long userId, Long otherUserId) {
        User user = users.get(userId);
        User otherUser = users.get(otherUserId);

        if (user == null || otherUser == null) {
            return new ArrayList<>();
        }

        if (user.getFriends() == null || otherUser.getFriends() == null) {
            return new ArrayList<>();
        }

        return user.getFriends().keySet().stream()
                .filter(friendId -> otherUser.getFriends().containsKey(friendId))
                .map(users::get)
                .filter(friend -> friend != null)
                .collect(Collectors.toList());
    }
}
