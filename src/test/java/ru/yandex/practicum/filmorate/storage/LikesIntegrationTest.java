package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LikesIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void testAddLikesToFilm() {
        User user1 = createUser("user1@example.com", "user1", "User One", "1990-01-01");
        User user2 = createUser("user2@example.com", "user2", "User Two", "1991-02-02");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        Film film = createFilm("Test Film", "Test Description", "2020-01-01", 120, Mpa.G);
        Film createdFilm = filmStorage.create(film);

        filmStorage.addLike(createdFilm.getId(), createdUser1.getId());
        filmStorage.addLike(createdFilm.getId(), createdUser2.getId());

        int likesCount = filmStorage.getLikesCount(createdFilm.getId());
        assertThat(likesCount).isEqualTo(2);

        Film retrievedFilm = filmStorage.findById(createdFilm.getId()).orElseThrow();
        assertThat(retrievedFilm.getLikes())
                .hasSize(2)
                .containsExactlyInAnyOrder(createdUser1.getId(), createdUser2.getId());
    }

    @Test
    void testRemoveLikeFromFilm() {
        User user1 = createUser("user1@example.com", "user1", "User One", "1990-01-01");
        User user2 = createUser("user2@example.com", "user2", "User Two", "1991-02-02");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        Film film = createFilm("Test Film", "Test Description", "2020-01-01", 120, Mpa.G);
        Film createdFilm = filmStorage.create(film);

        filmStorage.addLike(createdFilm.getId(), createdUser1.getId());
        filmStorage.addLike(createdFilm.getId(), createdUser2.getId());

        filmStorage.removeLike(createdFilm.getId(), createdUser1.getId());


        int likesCount = filmStorage.getLikesCount(createdFilm.getId());
        assertThat(likesCount).isEqualTo(1);

        Film retrievedFilm = filmStorage.findById(createdFilm.getId()).orElseThrow();
        assertThat(retrievedFilm.getLikes())
                .hasSize(1)
                .containsExactly(createdUser2.getId());
    }

    @Test
    void testFilmsOrderedByLikes() {

        User user1 = createUser("user1@example.com", "user1", "User One", "1990-01-01");
        User user2 = createUser("user2@example.com", "user2", "User Two", "1991-02-02");
        User user3 = createUser("user3@example.com", "user3", "User Three", "1992-03-03");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);
        User createdUser3 = userStorage.create(user3);


        Film film1 = createFilm("Film 1", "Description 1", "2020-01-01", 90, Mpa.G);
        Film film2 = createFilm("Film 2", "Description 2", "2021-02-02", 100, Mpa.PG);
        Film film3 = createFilm("Film 3", "Description 3", "2022-03-03", 110, Mpa.PG_13);

        Film createdFilm1 = filmStorage.create(film1);
        Film createdFilm2 = filmStorage.create(film2);
        Film createdFilm3 = filmStorage.create(film3);


        filmStorage.addLike(createdFilm1.getId(), createdUser1.getId());

        filmStorage.addLike(createdFilm2.getId(), createdUser1.getId());
        filmStorage.addLike(createdFilm2.getId(), createdUser2.getId());
        filmStorage.addLike(createdFilm2.getId(), createdUser3.getId());

        filmStorage.addLike(createdFilm3.getId(), createdUser1.getId());
        filmStorage.addLike(createdFilm3.getId(), createdUser2.getId());


        List<Film> topFilms = filmStorage.findTopPopular(3);


        assertThat(topFilms)
                .hasSize(3)
                .extracting(Film::getName)
                .containsExactly("Film 2", "Film 3", "Film 1");
    }

    @Test
    void testDuplicateLikeNotAdded() {

        User user = createUser("user@example.com", "user", "User", "1990-01-01");
        User createdUser = userStorage.create(user);

        Film film = createFilm("Test Film", "Description", "2020-01-01", 120, Mpa.G);
        Film createdFilm = filmStorage.create(film);


        filmStorage.addLike(createdFilm.getId(), createdUser.getId());
        filmStorage.addLike(createdFilm.getId(), createdUser.getId()); // повторно


        int likesCount = filmStorage.getLikesCount(createdFilm.getId());
        assertThat(likesCount).isEqualTo(1);
    }

    @Test
    void testRemoveNonExistentLike() {

        User user = createUser("user@example.com", "user", "User", "1990-01-01");
        User createdUser = userStorage.create(user);

        Film film = createFilm("Test Film", "Description", "2020-01-01", 120, Mpa.G);
        Film createdFilm = filmStorage.create(film);


        filmStorage.removeLike(createdFilm.getId(), createdUser.getId());


        int likesCount = filmStorage.getLikesCount(createdFilm.getId());
        assertThat(likesCount).isEqualTo(0);
    }

    @Test
    void testHasUserLiked() {
        // Given - создаем пользователя и фильм
        User user = createUser("user@example.com", "user", "User", "1990-01-01");
        User createdUser = userStorage.create(user);

        Film film = createFilm("Test Film", "Description", "2020-01-01", 120, Mpa.G);
        Film createdFilm = filmStorage.create(film);

        filmStorage.addLike(createdFilm.getId(), createdUser.getId());


        boolean hasLiked = filmStorage.hasUserLiked(createdFilm.getId(), createdUser.getId());
        assertThat(hasLiked).isTrue();
    }

    @Test
    void testHasUserNotLiked() {

        User user = createUser("user@example.com", "user", "User", "1990-01-01");
        User createdUser = userStorage.create(user);

        Film film = createFilm("Test Film", "Description", "2020-01-01", 120, Mpa.G);
        Film createdFilm = filmStorage.create(film);


        boolean hasLiked = filmStorage.hasUserLiked(createdFilm.getId(), createdUser.getId());
        assertThat(hasLiked).isFalse();
    }

    @Test
    void testMultipleUsersLikingSameFilm() {
        // Given - создаем несколько пользователей
        User user1 = createUser("user1@example.com", "user1", "User One", "1990-01-01");
        User user2 = createUser("user2@example.com", "user2", "User Two", "1991-02-02");
        User user3 = createUser("user3@example.com", "user3", "User Three", "1992-03-03");
        User user4 = createUser("user4@example.com", "user4", "User Four", "1993-04-04");
        User user5 = createUser("user5@example.com", "user5", "User Five", "1994-05-05");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);
        User createdUser3 = userStorage.create(user3);
        User createdUser4 = userStorage.create(user4);
        User createdUser5 = userStorage.create(user5);

        Film film = createFilm("Popular Film", "Very popular", "2020-01-01", 120, Mpa.PG);
        Film createdFilm = filmStorage.create(film);

        filmStorage.addLike(createdFilm.getId(), createdUser1.getId());
        filmStorage.addLike(createdFilm.getId(), createdUser2.getId());
        filmStorage.addLike(createdFilm.getId(), createdUser3.getId());
        filmStorage.addLike(createdFilm.getId(), createdUser4.getId());
        filmStorage.addLike(createdFilm.getId(), createdUser5.getId());

        int likesCount = filmStorage.getLikesCount(createdFilm.getId());
        assertThat(likesCount).isEqualTo(5);

        Film retrievedFilm = filmStorage.findById(createdFilm.getId()).orElseThrow();
        assertThat(retrievedFilm.getLikes())
                .hasSize(5)
                .containsExactlyInAnyOrder(
                        createdUser1.getId(),
                        createdUser2.getId(),
                        createdUser3.getId(),
                        createdUser4.getId(),
                        createdUser5.getId()
                );
    }

    @Test
    void testRemoveAllLikesFromFilm() {
        // Given - создаем пользователей
        User user1 = createUser("user1@example.com", "user1", "User One", "1990-01-01");
        User user2 = createUser("user2@example.com", "user2", "User Two", "1991-02-02");
        User user3 = createUser("user3@example.com", "user3", "User Three", "1992-03-03");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);
        User createdUser3 = userStorage.create(user3);

        Film film = createFilm("Test Film", "Description", "2020-01-01", 120, Mpa.G);
        Film createdFilm = filmStorage.create(film);

        filmStorage.addLike(createdFilm.getId(), createdUser1.getId());
        filmStorage.addLike(createdFilm.getId(), createdUser2.getId());
        filmStorage.addLike(createdFilm.getId(), createdUser3.getId());

        filmStorage.removeLike(createdFilm.getId(), createdUser1.getId());
        filmStorage.removeLike(createdFilm.getId(), createdUser2.getId());
        filmStorage.removeLike(createdFilm.getId(), createdUser3.getId());

        int likesCount = filmStorage.getLikesCount(createdFilm.getId());
        assertThat(likesCount).isEqualTo(0);

        Film retrievedFilm = filmStorage.findById(createdFilm.getId()).orElseThrow();
        assertThat(retrievedFilm.getLikes()).isEmpty();
    }

    @Test
    void testOneUserLikingMultipleFilms() {
        User user = createUser("user@example.com", "user", "User", "1990-01-01");
        User createdUser = userStorage.create(user);

        Film film1 = createFilm("Film 1", "Description 1", "2020-01-01", 90, Mpa.G);
        Film film2 = createFilm("Film 2", "Description 2", "2021-02-02", 100, Mpa.PG);
        Film film3 = createFilm("Film 3", "Description 3", "2022-03-03", 110, Mpa.PG_13);

        Film createdFilm1 = filmStorage.create(film1);
        Film createdFilm2 = filmStorage.create(film2);
        Film createdFilm3 = filmStorage.create(film3);

        filmStorage.addLike(createdFilm1.getId(), createdUser.getId());
        filmStorage.addLike(createdFilm2.getId(), createdUser.getId());
        filmStorage.addLike(createdFilm3.getId(), createdUser.getId());

        assertThat(filmStorage.getLikesCount(createdFilm1.getId())).isEqualTo(1);
        assertThat(filmStorage.getLikesCount(createdFilm2.getId())).isEqualTo(1);
        assertThat(filmStorage.getLikesCount(createdFilm3.getId())).isEqualTo(1);

        assertThat(filmStorage.hasUserLiked(createdFilm1.getId(), createdUser.getId())).isTrue();
        assertThat(filmStorage.hasUserLiked(createdFilm2.getId(), createdUser.getId())).isTrue();
        assertThat(filmStorage.hasUserLiked(createdFilm3.getId(), createdUser.getId())).isTrue();
    }

    @Test
    void testGetLikesCountForFilmWithNoLikes() {
        Film film = createFilm("Unpopular Film", "No one likes this", "2020-01-01", 120, Mpa.G);
        Film createdFilm = filmStorage.create(film);

        int likesCount = filmStorage.getLikesCount(createdFilm.getId());

        assertThat(likesCount).isEqualTo(0);
    }


    private User createUser(String email, String login, String name, String birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.parse(birthday));
        user.setFriends(new java.util.HashMap<>());
        return user;
    }

    private Film createFilm(String name, String description, String releaseDate, int duration, Mpa mpa) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.parse(releaseDate));
        film.setDuration(duration);
        film.setMpa(mpa);
        film.setGenres(new HashSet<>());
        film.setLikes(new HashSet<>());
        return film;
    }
}
