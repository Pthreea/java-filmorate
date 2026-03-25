package ru.yandex.practicum.filmorate.storage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    void testCreateUser() {
        User user = createTestUser("test@example.com", "testuser", "Test User");

        User createdUser = userStorage.create(user);

        assertThat(createdUser)
                .isNotNull()
                .hasFieldOrProperty("id");

        assertThat(createdUser.getId()).isPositive();
        assertThat(createdUser.getEmail()).isEqualTo("test@example.com");
        assertThat(createdUser.getLogin()).isEqualTo("testuser");
        assertThat(createdUser.getName()).isEqualTo("Test User");
        assertThat(createdUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testFindUserById() {
        User user = createTestUser("find@example.com", "finduser", "Find User");
        User createdUser = userStorage.create(user);

        Optional<User> foundUser = userStorage.findById(createdUser.getId());

        assertThat(foundUser)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u).hasFieldOrPropertyWithValue("id", createdUser.getId());
                    assertThat(u.getEmail()).isEqualTo("find@example.com");
                    assertThat(u.getLogin()).isEqualTo("finduser");
                    assertThat(u.getName()).isEqualTo("Find User");
                });
    }

    @Test
    void testFindUserById_NotFound() {
        Optional<User> foundUser = userStorage.findById(9999L);

        assertThat(foundUser).isEmpty();
    }

    @Test
    void testUpdateUser() {
        User user = createTestUser("update@example.com", "updateuser", "Original Name");
        User createdUser = userStorage.create(user);

        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@example.com");
        User updatedUser = userStorage.update(createdUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");

        Optional<User> foundUser = userStorage.findById(createdUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Updated Name");
        assertThat(foundUser.get().getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void testFindAllUsers() {
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");
        User user3 = createTestUser("user3@example.com", "user3", "User Three");

        userStorage.create(user1);
        userStorage.create(user2);
        userStorage.create(user3);

        List<User> users = userStorage.findAll();

        assertThat(users)
                .isNotNull()
                .hasSize(3)
                .extracting(User::getLogin)
                .containsExactlyInAnyOrder("user1", "user2", "user3");
    }

    @Test
    void testFindAllUsers_EmptyDatabase() {
        List<User> users = userStorage.findAll();

        assertThat(users).isEmpty();
    }

    @Test
    void testCreateUserWithFriends() {
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        createdUser1.getFriends().put(createdUser2.getId(), FriendshipStatus.UNCONFIRMED);

        User updatedUser = userStorage.update(createdUser1);

        assertThat(updatedUser.getFriends())
                .hasSize(1)
                .containsKey(createdUser2.getId())
                .containsValue(FriendshipStatus.UNCONFIRMED);

        Optional<User> foundUser = userStorage.findById(createdUser1.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFriends())
                .containsEntry(createdUser2.getId(), FriendshipStatus.UNCONFIRMED);
    }

    @Test
    void testUpdateFriendshipStatus() {
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        createdUser1.getFriends().put(createdUser2.getId(), FriendshipStatus.UNCONFIRMED);
        userStorage.update(createdUser1);

        createdUser1.getFriends().put(createdUser2.getId(), FriendshipStatus.CONFIRMED);
        userStorage.update(createdUser1);

        Optional<User> foundUser = userStorage.findById(createdUser1.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFriends())
                .containsEntry(createdUser2.getId(), FriendshipStatus.CONFIRMED);
    }

    @Test
    void testRemoveFriendship() {
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        createdUser1.getFriends().put(createdUser2.getId(), FriendshipStatus.CONFIRMED);
        userStorage.update(createdUser1);

        createdUser1.getFriends().remove(createdUser2.getId());
        userStorage.update(createdUser1);

        Optional<User> foundUser = userStorage.findById(createdUser1.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFriends()).isEmpty();
    }

    @Test
    void testCreateUserWithEmptyFriends() {
        User user = createTestUser("test@example.com", "testuser", "Test User");
        user.setFriends(new HashMap<>());

        User createdUser = userStorage.create(user);

        assertThat(createdUser.getFriends()).isEmpty();

        Optional<User> foundUser = userStorage.findById(createdUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFriends()).isEmpty();
    }

    private User createTestUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setFriends(new HashMap<>());
        return user;
    }
}