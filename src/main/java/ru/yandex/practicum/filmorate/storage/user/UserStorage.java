package ru.yandex.practicum.filmorate.storage.user;

import java.util.List;
import java.util.Optional;

import ru.yandex.practicum.filmorate.model.User;

public interface UserStorage {

    User create(User user);

    User update(User user);

    List<User> findAll();

    Optional<User> findById(Long id);

    List<User> findFriends(Long userId);

    List<User> findCommonFriends(Long userId, Long otherUserId);
}