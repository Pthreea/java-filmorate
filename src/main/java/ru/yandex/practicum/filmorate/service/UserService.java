package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(Long userId, Long friendId) {
        log.debug("Добавление в друзья: userId={}, friendId={}", userId, friendId);

        User user = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден при попытке добавить друга", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден при попытке добавить в друзья", friendId);
                    return new NotFoundException("Пользователь с id " + friendId + " не найден");
                });

        int userFriendsBefore = user.getFriends().size();
        int friendFriendsBefore = friend.getFriends().size();

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователь {} ({}) и пользователь {} ({}) теперь друзья",
                userId, user.getLogin(), friendId, friend.getLogin());
        log.debug("Количество друзей пользователя {}: {} -> {}", userId, userFriendsBefore, user.getFriends().size());
        log.debug("Количество друзей пользователя {}: {} -> {}", friendId, friendFriendsBefore, friend.getFriends().size());
    }

    public void removeFriend(Long userId, Long friendId) {
        log.debug("Удаление из друзей: userId={}, friendId={}", userId, friendId);

        User user = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден при попытке удалить друга", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден при попытке удалить из друзей", friendId);
                    return new NotFoundException("Пользователь с id " + friendId + " не найден");
                });

        boolean removedFromUser = user.getFriends().remove(friendId);
        boolean removedFromFriend = friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);

        if (removedFromUser && removedFromFriend) {
            log.info("Пользователь {} ({}) и пользователь {} ({}) больше не друзья",
                    userId, user.getLogin(), friendId, friend.getLogin());
            log.debug("Количество друзей пользователя {}: {}", userId, user.getFriends().size());
            log.debug("Количество друзей пользователя {}: {}", friendId, friend.getFriends().size());
        } else {
            log.debug("Пользователи {} и {} не были друзьями", userId, friendId);
        }
    }

    public List<User> getFriends(Long userId) {
        log.debug("Получение списка друзей пользователя с id={}", userId);

        User user = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден при попытке получить список друзей", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        List<User> friends = user.getFriends().stream()
                .map(friendId -> userStorage.findById(friendId)
                        .orElseThrow(() -> {
                            log.error("Друг с id={} не найден в хранилище", friendId);
                            return new NotFoundException("Друг с id " + friendId + " не найден");
                        }))
                .collect(Collectors.toList());

        log.info("У пользователя {} ({}) найдено {} друзей", userId, user.getLogin(), friends.size());
        log.trace("Друзья пользователя {}: {}", userId, friends);

        return friends;
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.debug("Поиск общих друзей: userId={}, otherId={}", userId, otherId);

        User user = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден при поиске общих друзей", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });
        User other = userStorage.findById(otherId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден при поиске общих друзей", otherId);
                    return new NotFoundException("Пользователь с id " + otherId + " не найден");
                });

        Set<Long> commonFriendsIds = user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .collect(Collectors.toSet());

        log.debug("Найдено {} ID общих друзей: {}", commonFriendsIds.size(), commonFriendsIds);

        List<User> commonFriends = commonFriendsIds.stream()
                .map(friendId -> userStorage.findById(friendId)
                        .orElseThrow(() -> {
                            log.error("Общий друг с id={} не найден в хранилище", friendId);
                            return new NotFoundException("Общий друг с id " + friendId + " не найден");
                        }))
                .collect(Collectors.toList());

        log.info("У пользователей {} ({}) и {} ({}) найдено {} общих друзей",
                userId, user.getLogin(), otherId, other.getLogin(), commonFriends.size());
        log.trace("Общие друзья: {}", commonFriends);

        return commonFriends;
    }
}
