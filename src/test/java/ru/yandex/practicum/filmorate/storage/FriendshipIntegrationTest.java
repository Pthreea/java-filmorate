package ru.yandex.practicum.filmorate.storage;

import java.time.LocalDate;
import java.util.HashMap;

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
class FriendshipIntegrationTest {

    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    void testUnconfirmedFriendship() {
        User alice = createUser("alice@example.com", "alice", "Alice");
        User bob = createUser("bob@example.com", "bob", "Bob");

        User createdAlice = userStorage.create(alice);
        User createdBob = userStorage.create(bob);

        createdAlice.getFriends().put(createdBob.getId(), FriendshipStatus.UNCONFIRMED);
        userStorage.update(createdAlice);

        User foundAlice = userStorage.findById(createdAlice.getId()).orElseThrow();
        assertThat(foundAlice.getFriends())
                .containsEntry(createdBob.getId(), FriendshipStatus.UNCONFIRMED);
    }

    @Test
    void testConfirmedFriendship() {
        User alice = createUser("alice@example.com", "alice", "Alice");
        User bob = createUser("bob@example.com", "bob", "Bob");

        User createdAlice = userStorage.create(alice);
        User createdBob = userStorage.create(bob);

        createdAlice.getFriends().put(createdBob.getId(), FriendshipStatus.UNCONFIRMED);
        userStorage.update(createdAlice);

        createdAlice.getFriends().put(createdBob.getId(), FriendshipStatus.CONFIRMED);
        createdBob.getFriends().put(createdAlice.getId(), FriendshipStatus.CONFIRMED);

        userStorage.update(createdAlice);
        userStorage.update(createdBob);

        User foundAlice = userStorage.findById(createdAlice.getId()).orElseThrow();
        User foundBob = userStorage.findById(createdBob.getId()).orElseThrow();

        assertThat(foundAlice.getFriends())
                .containsEntry(createdBob.getId(), FriendshipStatus.CONFIRMED);
        assertThat(foundBob.getFriends())
                .containsEntry(createdAlice.getId(), FriendshipStatus.CONFIRMED);
    }

    @Test
    void testMultipleFriends() {
        User alice = createUser("alice@example.com", "alice", "Alice");
        User bob = createUser("bob@example.com", "bob", "Bob");
        User charlie = createUser("charlie@example.com", "charlie", "Charlie");

        User createdAlice = userStorage.create(alice);
        User createdBob = userStorage.create(bob);
        User createdCharlie = userStorage.create(charlie);

        createdAlice.getFriends().put(createdBob.getId(), FriendshipStatus.CONFIRMED);
        createdAlice.getFriends().put(createdCharlie.getId(), FriendshipStatus.UNCONFIRMED);
        userStorage.update(createdAlice);

        User foundAlice = userStorage.findById(createdAlice.getId()).orElseThrow();
        assertThat(foundAlice.getFriends())
                .hasSize(2)
                .containsEntry(createdBob.getId(), FriendshipStatus.CONFIRMED)
                .containsEntry(createdCharlie.getId(), FriendshipStatus.UNCONFIRMED);
    }

    @Test
    void testRemoveFriend() {
        User alice = createUser("alice@example.com", "alice", "Alice");
        User bob = createUser("bob@example.com", "bob", "Bob");

        User createdAlice = userStorage.create(alice);
        User createdBob = userStorage.create(bob);

        createdAlice.getFriends().put(createdBob.getId(), FriendshipStatus.CONFIRMED);
        userStorage.update(createdAlice);

        createdAlice.getFriends().remove(createdBob.getId());
        userStorage.update(createdAlice);

        User foundAlice = userStorage.findById(createdAlice.getId()).orElseThrow();
        assertThat(foundAlice.getFriends()).isEmpty();
    }

    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setFriends(new HashMap<>());
        return user;
    }
}
