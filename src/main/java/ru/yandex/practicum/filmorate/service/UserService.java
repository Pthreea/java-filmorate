package ru.yandex.practicum.filmorate.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое, установлено значение логина: {}", user.getLogin());
        }
        User createdUser = userStorage.create(user);
        log.info("Создан пользователь: {}", createdUser);
        return createdUser;
    }

    public User updateUser(User user) {
        getUserById(user.getId()); // Проверка существования

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User updatedUser = userStorage.update(user);
        log.info("Обновлен пользователь: {}", updatedUser);
        return updatedUser;
    }

    public List<User> getAllUsers() {
        List<User> users = userStorage.findAll();
        log.debug("Получен список из {} пользователей", users.size());
        return users;
    }

    public User getUserById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", id);
                    return new NotFoundException("Пользователь с id " + id + " не найден");
                });
    }

    /**
     * Односторонняя дружба: user добавляет friend в друзья
     */
    public void addFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        // Проверяем, не добавлен ли уже в друзья
        if (user.getFriends().containsKey(friendId)) {
            log.warn("Пользователь {} уже в друзьях у пользователя {}", friendId, userId);
            return;
        }

        // Односторонняя дружба: только user добавляет friend
        // Проверяем, есть ли встречная заявка (для статуса CONFIRMED)
        if (friend.getFriends().containsKey(userId)) {
            // Если friend уже добавил user в друзья, то дружба становится подтвержденной
            user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
            friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);
            userStorage.update(friend);
            log.info("Дружба между пользователями {} и {} подтверждена", userId, friendId);
        } else {
            // Иначе - неподтвержденная дружба
            user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);
            log.info("Пользователь {} отправил заявку в друзья пользователю {}", userId, friendId);
        }

        userStorage.update(user);
    }

    /**
     * Удаление из друзей (односторонне)
     */
    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (!user.getFriends().containsKey(friendId)) {
            log.warn("Пользователь {} не является другом пользователя {}", friendId, userId);
            return;
        }

        // Удаляем дружбу
        user.getFriends().remove(friendId);
        userStorage.update(user);

        // Если дружба была взаимной, меняем статус у friend на UNCONFIRMED
        if (friend.getFriends().containsKey(userId) &&
                friend.getFriends().get(userId) == FriendshipStatus.CONFIRMED) {
            friend.getFriends().put(userId, FriendshipStatus.UNCONFIRMED);
            userStorage.update(friend);
            log.info("Статус дружбы изменен на UNCONFIRMED для пользователя {}", friendId);
        }

        log.info("Пользователь {} удален из друзей пользователя {}", friendId, userId);
    }

    /**
     * Получить список друзей пользователя
     */
    public List<User> getFriends(Long userId) {
        User user = getUserById(userId);

        List<User> friends = user.getFriends().keySet().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());

        log.debug("У пользователя {} найдено {} друзей", userId, friends.size());
        return friends;
    }

    /**
     * Получить список общих друзей двух пользователей
     */
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = getUserById(userId);
        User otherUser = getUserById(otherUserId);

        List<User> commonFriends = user.getFriends().keySet().stream()
                .filter(friendId -> otherUser.getFriends().containsKey(friendId))
                .map(this::getUserById)
                .collect(Collectors.toList());

        log.debug("Найдено {} общих друзей между пользователями {} и {}",
                commonFriends.size(), userId, otherUserId);
        return commonFriends;
    }
}