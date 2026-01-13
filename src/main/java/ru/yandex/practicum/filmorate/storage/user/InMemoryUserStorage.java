package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    @Override
    public User create(User user) {
        log.debug("Создание пользователя: {}", user);
        user.setId(currentId++);
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с id={}: {} ({})", user.getId(), user.getName(), user.getLogin());
        log.debug("Текущее количество пользователей в хранилище: {}", users.size());
        return user;
    }

    @Override
    public User update(User user) {
        log.debug("Попытка обновления пользователя с id={}", user.getId());
        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id={} не найден при попытке обновления", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        User oldUser = users.get(user.getId());
        log.debug("Обновление пользователя. Старые данные: {}", oldUser);
        users.put(user.getId(), user);
        log.info("Пользователь с id={} успешно обновлён: {} ({})", user.getId(), user.getName(), user.getLogin());
        log.debug("Новые данные пользователя: {}", user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        log.debug("Поиск пользователя по id={}", id);
        Optional<User> user = Optional.ofNullable(users.get(id));
        if (user.isPresent()) {
            log.debug("Пользователь с id={} найден: {} ({})", id, user.get().getName(), user.get().getLogin());
        } else {
            log.debug("Пользователь с id={} не найден", id);
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        log.debug("Получение всех пользователей. Количество: {}", users.size());
        List<User> allUsers = new ArrayList<>(users.values());
        log.trace("Список всех пользователей: {}", allUsers);
        return allUsers;
    }

    @Override
    public void delete(Long id) {
        log.debug("Попытка удаления пользователя с id={}", id);
        User removed = users.remove(id);
        if (removed != null) {
            log.info("Пользователь с id={} успешно удалён: {} ({})", id, removed.getName(), removed.getLogin());
            log.debug("Текущее количество пользователей в хранилище: {}", users.size());
        } else {
            log.error("Пользователь с id={} не найден при попытке удаления", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }
}
