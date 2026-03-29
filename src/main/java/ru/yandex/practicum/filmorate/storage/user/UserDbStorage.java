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

        if (user.getFriends() != null && !user.getFriends().isEmpty()) {
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

        updateFriendships(user);

        return user;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY user_id";
        List<User> users = jdbcTemplate.query(sql, userRowMapper());

        for (User user : users) {
            loadFriendships(user);
        }

        log.debug("Найдено {} пользователей", users.size());
        return users;
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

    @Override
    public List<User> findFriends(Long userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships fr ON u.user_id = fr.friend_id " +
                "WHERE fr.user_id = ? " +
                "ORDER BY u.user_id";

        List<User> friends = jdbcTemplate.query(sql, userRowMapper(), userId);

        for (User friend : friends) {
            loadFriendships(friend);
        }

        log.debug("Найдено {} друзей для пользователя {}", friends.size(), userId);
        return friends;
    }

    @Override
    public List<User> findCommonFriends(Long userId, Long otherUserId) {
        String sql = "SELECT u.* FROM users u " +
                "WHERE u.user_id IN (" +
                "    SELECT fr1.friend_id FROM friendships fr1 WHERE fr1.user_id = ? " +
                "    INTERSECT " +
                "    SELECT fr2.friend_id FROM friendships fr2 WHERE fr2.user_id = ?" +
                ") " +
                "ORDER BY u.user_id";

        List<User> commonFriends = jdbcTemplate.query(sql, userRowMapper(), userId, otherUserId);

        for (User friend : commonFriends) {
            loadFriendships(friend);
        }

        log.debug("Найдено {} общих друзей для пользователей {} и {}",
                commonFriends.size(), userId, otherUserId);
        return commonFriends;
    }


    private void saveFriendships(User user) {
        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";

        for (Map.Entry<Long, FriendshipStatus> entry : user.getFriends().entrySet()) {
            jdbcTemplate.update(sql,
                    user.getId(),
                    entry.getKey(),
                    entry.getValue().name()
            );
        }

        log.debug("Сохранено {} связей дружбы для пользователя {}", user.getFriends().size(), user.getId());
    }

    private void updateFriendships(User user) {
        String deleteSql = "DELETE FROM friendships WHERE user_id = ?";
        jdbcTemplate.update(deleteSql, user.getId());

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
        log.trace("Загружено {} друзей для пользователя {}", friends.size(), user.getId());
    }


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

    public boolean isFriend(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }


    public Optional<FriendshipStatus> getFriendshipStatus(Long userId, Long friendId) {
        String sql = "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?";
        List<String> statuses = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getString("status"),
                userId, friendId);

        if (statuses.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(FriendshipStatus.valueOf(statuses.get(0)));
    }


    public void addFriend(Long userId, Long friendId) {
        String sql = "MERGE INTO friendships (user_id, friend_id, status) KEY(user_id, friend_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, FriendshipStatus.UNCONFIRMED.name());
        log.debug("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }


    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        log.debug("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }
}