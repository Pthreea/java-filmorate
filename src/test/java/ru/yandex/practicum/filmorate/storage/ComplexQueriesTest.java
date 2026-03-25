package ru.yandex.practicum.filmorate.storage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ComplexQueriesTest {

    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;
    private FilmDbStorage filmStorage;

    @BeforeEach
    void setUp() {
        userStorage = new UserDbStorage(jdbcTemplate);
        filmStorage = new FilmDbStorage(jdbcTemplate);
    }

    @Test
    void testGetTopPopularFilms() {
        // Given - создаем фильмы с разным количеством лайков
        Film film1 = createFilm("Least Popular", Mpa.G);
        film1.setLikes(Set.of(1L));

        Film film2 = createFilm("Most Popular", Mpa.PG);
        film2.setLikes(Set.of(1L, 2L, 3L, 4L, 5L));

        Film film3 = createFilm("Medium Popular", Mpa.PG_13);
        film3.setLikes(Set.of(1L, 2L, 3L));

        filmStorage.create(film1);
        filmStorage.create(film2);
        filmStorage.create(film3);

        // When - получаем топ-2 популярных фильма
        String sql = "SELECT f.*, COUNT(l.user_id) as likes_count " +
                "FROM films f " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY likes_count DESC " +
                "LIMIT 2";

        List<String> topFilms = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"));

        // Then
        assertThat(topFilms)
                .hasSize(2)
                .containsExactly("Most Popular", "Medium Popular");
    }

    @Test
    void testGetCommonFriends() {
        // Given - создаем пользователей
        User alice = createUser("alice@example.com", "alice", "Alice");
        User bob = createUser("bob@example.com", "bob", "Bob");
        User charlie = createUser("charlie@example.com", "charlie", "Charlie");
        User dave = createUser("dave@example.com", "dave", "Dave");

        User createdAlice = userStorage.create(alice);
        User createdBob = userStorage.create(bob);
        User createdCharlie = userStorage.create(charlie);
        User createdDave = userStorage.create(dave);

        // Alice и Bob оба дружат с Charlie
        createdAlice.getFriends().put(createdCharlie.getId(), FriendshipStatus.CONFIRMED);
        createdBob.getFriends().put(createdCharlie.getId(), FriendshipStatus.CONFIRMED);

        // Только Alice дружит с Dave
        createdAlice.getFriends().put(createdDave.getId(), FriendshipStatus.CONFIRMED);

        userStorage.update(createdAlice);
        userStorage.update(createdBob);

        // When - ищем общих друзей Alice и Bob
        String sql = "SELECT u.* FROM users u " +
                "WHERE u.user_id IN (" +
                "    SELECT fr1.friend_id FROM friendships fr1 WHERE fr1.user_id = ? " +
                "    INTERSECT " +
                "    SELECT fr2.friend_id FROM friendships fr2 WHERE fr2.user_id = ?" +
                ")";

        List<Long> commonFriendIds = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("user_id"),
                createdAlice.getId(),
                createdBob.getId()
        );

        // Then - Charlie должен быть общим другом
        assertThat(commonFriendIds)
                .hasSize(1)
                .containsExactly(createdCharlie.getId());
    }

    @Test
    void testGetFilmsByGenre() {
        // Given - создаем фильмы с разными жанрами
        Film comedy1 = createFilm("Comedy Film 1", Mpa.G);
        comedy1.setGenres(Set.of(Genre.COMEDY));

        Film comedy2 = createFilm("Comedy Film 2", Mpa.PG);
        comedy2.setGenres(Set.of(Genre.COMEDY, Genre.DRAMA));

        Film action = createFilm("Action Film", Mpa.R);
        action.setGenres(Set.of(Genre.ACTION));

        filmStorage.create(comedy1);
        filmStorage.create(comedy2);
        filmStorage.create(action);

        // When - ищем фильмы жанра "Комедия"
        String sql = "SELECT f.* FROM films f " +
                "JOIN film_genres fg ON f.film_id = fg.film_id " +
                "WHERE fg.genre_id = 1"; // 1 = Комедия

        List<String> comedyFilms = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"));

        // Then
        assertThat(comedyFilms)
                .hasSize(2)
                .containsExactlyInAnyOrder("Comedy Film 1", "Comedy Film 2");
    }

    @Test
    void testGetUserFriendsWithStatus() {
        // Given
        User alice = createUser("alice@example.com", "alice", "Alice");
        User bob = createUser("bob@example.com", "bob", "Bob");
        User charlie = createUser("charlie@example.com", "charlie", "Charlie");

        User createdAlice = userStorage.create(alice);
        User createdBob = userStorage.create(bob);
        User createdCharlie = userStorage.create(charlie);

        // Alice добавляет друзей с разными статусами
        createdAlice.getFriends().put(createdBob.getId(), FriendshipStatus.CONFIRMED);
        createdAlice.getFriends().put(createdCharlie.getId(), FriendshipStatus.UNCONFIRMED);
        userStorage.update(createdAlice);

        // When - получаем только подтвержденных друзей
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships fr ON u.user_id = fr.friend_id " +
                "WHERE fr.user_id = ? AND fr.status = 'CONFIRMED'";

        List<String> confirmedFriends = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("login"),
                createdAlice.getId()
        );

        // Then
        assertThat(confirmedFriends)
                .hasSize(1)
                .containsExactly("bob");
    }

    @Test
    void testGetFilmsWithMultipleGenres() {
        // Given
        Film multiGenreFilm = createFilm("Multi-Genre Film", Mpa.PG_13);
        multiGenreFilm.setGenres(Set.of(Genre.ACTION, Genre.COMEDY, Genre.DRAMA));

        Film singleGenreFilm = createFilm("Single-Genre Film", Mpa.G);
        singleGenreFilm.setGenres(Set.of(Genre.COMEDY));

        filmStorage.create(multiGenreFilm);
        filmStorage.create(singleGenreFilm);

        // When - ищем фильмы с более чем одним жанром
        String sql = "SELECT f.film_id, f.name, COUNT(fg.genre_id) as genre_count " +
                "FROM films f " +
                "LEFT JOIN film_genres fg ON f.film_id = fg.film_id " +
                "GROUP BY f.film_id, f.name " +
                "HAVING COUNT(fg.genre_id) > 1";

        List<String> multiGenreFilms = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"));

        // Then
        assertThat(multiGenreFilms)
                .hasSize(1)
                .containsExactly("Multi-Genre Film");
    }

    @Test
    void testGetUsersWhoLikedFilm() {
        // Given - создаем пользователей и фильм
        User alice = createUser("alice@example.com", "alice", "Alice");
        User bob = createUser("bob@example.com", "bob", "Bob");
        User charlie = createUser("charlie@example.com", "charlie", "Charlie");

        User createdAlice = userStorage.create(alice);
        User createdBob = userStorage.create(bob);
        User createdCharlie = userStorage.create(charlie);

        Film film = createFilm("Popular Film", Mpa.G);
        film.setLikes(Set.of(createdAlice.getId(), createdBob.getId()));
        Film createdFilm = filmStorage.create(film);

        // When - получаем пользователей, которые лайкнули фильм
        String sql = "SELECT u.login FROM users u " +
                "JOIN likes l ON u.user_id = l.user_id " +
                "WHERE l.film_id = ? " +
                "ORDER BY u.login";

        List<String> usersWhoLiked = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("login"),
                createdFilm.getId()
        );

        // Then
        assertThat(usersWhoLiked)
                .hasSize(2)
                .containsExactly("alice", "bob");
    }

    @Test
    void testGetMostActiveUser() {
        // Given - создаем пользователей с разным количеством друзей
        User alice = createUser("alice@example.com", "alice", "Alice");
        User bob = createUser("bob@example.com", "bob", "Bob");
        User charlie = createUser("charlie@example.com", "charlie", "Charlie");
        User dave = createUser("dave@example.com", "dave", "Dave");

        User createdAlice = userStorage.create(alice);
        User createdBob = userStorage.create(bob);
        User createdCharlie = userStorage.create(charlie);
        User createdDave = userStorage.create(dave);

        // Alice имеет больше всего друзей
        createdAlice.getFriends().put(createdBob.getId(), FriendshipStatus.CONFIRMED);
        createdAlice.getFriends().put(createdCharlie.getId(), FriendshipStatus.CONFIRMED);
        createdAlice.getFriends().put(createdDave.getId(), FriendshipStatus.CONFIRMED);

        createdBob.getFriends().put(createdAlice.getId(), FriendshipStatus.CONFIRMED);

        userStorage.update(createdAlice);
        userStorage.update(createdBob);

        // When - находим самого активного пользователя
        String sql = "SELECT u.login, COUNT(fr.friend_id) as friends_count " +
                "FROM users u " +
                "LEFT JOIN friendships fr ON u.user_id = fr.user_id " +
                "GROUP BY u.user_id, u.login " +
                "ORDER BY friends_count DESC " +
                "LIMIT 1";

        String mostActiveUser = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> rs.getString("login"));

        // Then
        assertThat(mostActiveUser).isEqualTo("alice");
    }

    // Helper methods
    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setFriends(new HashMap<>());
        return user;
    }

    private Film createFilm(String name, Mpa mpa) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description for " + name);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);
        film.setGenres(new HashSet<>());
        film.setLikes(new HashSet<>());
        return film;
    }
}
