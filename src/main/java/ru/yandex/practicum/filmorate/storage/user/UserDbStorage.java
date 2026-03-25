package ru.yandex.practicum.filmorate.storage.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        log.debug("Создан пользователь с ID: {}", user.getId());

        // Сохраняем друзей если есть
        if (!user.getFriends().isEmpty()) {
            saveFriendships(user);
        }

        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday()),
                user.getId()
        );

        if (rowsAffected == 0) {
            log.error("Пользователь с ID {} не найден для обновления", user.getId());
            throw new RuntimeException("Пользователь с ID " + user.getId() + " не найден");
        }

        log.debug("Обновлен пользователь с ID: {}", user.getId());

        // Обновляем дружбу
        updateFriendships(user);

        return user;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY user_id";
        List<User> users = jdbcTemplate.query(sql, userRowMapper());

        // Загружаем друзей для каждого пользователя
        for (User user : users) {
            loadFriendships(user);
        }

        log.debug("Найдено {} пользователей", users.size());
        return users;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        List<User> users = jdbcTemplate.query(sql, userRowMapper(), id);

        if (users.isEmpty()) {
            log.debug("Пользователь с ID {} не найден", id);
            return Optional.empty();
        }

        User user = users.get(0);
        loadFriendships(user);

        log.debug("Найден пользователь с ID: {}", id);
        return Optional.of(user);
    }

    // Вспомогательные методы для работы с дружбой

    private void saveFriendships(User user) {
        String deleteSql = "DELETE FROM friendships WHERE user_id = ?";
        jdbcTemplate.update(deleteSql, user.getId());

        if (user.getFriends().isEmpty()) {
            return;
        }

        String insertSql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";

        for (Map.Entry<Long, FriendshipStatus> entry : user.getFriends().entrySet()) {
            jdbcTemplate.update(insertSql,
                    user.getId(),
                    entry.getKey(),
                    entry.getValue().name()
            );
        }

        log.debug("Сохранено {} связей дружбы для пользователя {}", user.getFriends().size(), user.getId());
    }

    private void updateFriendships(User user) {
        saveFriendships(user);
    }

    private void loadFriendships(User user) {
        String sql = "SELECT friend_id, status FROM friendships WHERE user_id = ?";

        Map<Long, FriendshipStatus> friends = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            Long friendId = rs.getLong("friend_id");
            FriendshipStatus status = FriendshipStatus.valueOf(rs.getString("status"));
            friends.put(friendId, status);
        }, user.getId());

        user.setFriends(friends);
        log.debug("Загружено {} друзей для пользователя {}", friends.size(), user.getId());
    }

    // RowMapper для User

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> mapRowToUser(rs);
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        user.setFriends(new HashMap<>());
        return user;
    }
}