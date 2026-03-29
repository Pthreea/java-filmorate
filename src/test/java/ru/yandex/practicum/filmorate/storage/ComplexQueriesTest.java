package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ComplexQueriesTest {

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
    void testGetTopPopularFilms() {
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

        Film film1 = createFilm("Least Popular", "Short description", "2020-01-01", 90, Mpa.G);
        Film film2 = createFilm("Most Popular", "Another description", "2021-02-02", 120, Mpa.PG);
        Film film3 = createFilm("Medium Popular", "Third description", "2022-03-03", 110, Mpa.PG_13);

        Film createdFilm1 = filmStorage.create(film1);
        Film createdFilm2 = filmStorage.create(film2);
        Film createdFilm3 = filmStorage.create(film3);

        filmStorage.addLike(createdFilm1.getId(), createdUser1.getId());

        filmStorage.addLike(createdFilm2.getId(), createdUser1.getId());
        filmStorage.addLike(createdFilm2.getId(), createdUser2.getId());
        filmStorage.addLike(createdFilm2.getId(), createdUser3.getId());
        filmStorage.addLike(createdFilm2.getId(), createdUser4.getId());
        filmStorage.addLike(createdFilm2.getId(), createdUser5.getId());

        filmStorage.addLike(createdFilm3.getId(), createdUser1.getId());
        filmStorage.addLike(createdFilm3.getId(), createdUser2.getId());
        filmStorage.addLike(createdFilm3.getId(), createdUser3.getId());


        List<Film> topFilms = filmStorage.findTopPopular(2);


        assertThat(topFilms)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactly("Most Popular", "Medium Popular");


        assertThat(topFilms.get(0).getLikes()).hasSize(5);
        assertThat(topFilms.get(1).getLikes()).hasSize(3);
    }

    @Test
    void testGetTopPopularFilmsWithDefaultCount() {

        User user1 = createUser("user1@example.com", "user1", "User One", "1990-01-01");
        User createdUser1 = userStorage.create(user1);


        for (int i = 1; i <= 12; i++) {
            Film film = createFilm(
                    "Film " + i,
                    "Description " + i,
                    "2020-01-0" + (i % 9 + 1),
                    100 + i,
                    Mpa.G
            );
            Film createdFilm = filmStorage.create(film);

            for (int j = 0; j < (13 - i); j++) {
                filmStorage.addLike(createdFilm.getId(), createdUser1.getId());
                break;
            }
        }

        List<Film> topFilms = filmStorage.findTopPopular(10);

        assertThat(topFilms).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    void testGetFilmsByGenre() {
        Film comedyFilm1 = createFilm("Comedy 1", "Funny movie", "2020-01-01", 90, Mpa.G);
        comedyFilm1.setGenres(Set.of(Genre.COMEDY));

        Film comedyFilm2 = createFilm("Comedy 2", "Very funny", "2021-02-02", 95, Mpa.PG);
        comedyFilm2.setGenres(Set.of(Genre.COMEDY));

        Film dramaFilm = createFilm("Drama 1", "Sad movie", "2022-03-03", 120, Mpa.PG_13);
        dramaFilm.setGenres(Set.of(Genre.DRAMA));

        filmStorage.create(comedyFilm1);
        filmStorage.create(comedyFilm2);
        filmStorage.create(dramaFilm);

        List<Film> comedyFilms = filmStorage.findByGenre(Genre.COMEDY);

        assertThat(comedyFilms)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Comedy 1", "Comedy 2");
    }

    @Test
    void testGetFilmsByMpa() {
        Film filmG1 = createFilm("Film G1", "For all ages", "2020-01-01", 90, Mpa.G);
        Film filmG2 = createFilm("Film G2", "Family friendly", "2021-02-02", 95, Mpa.G);
        Film filmPG = createFilm("Film PG", "Parental guidance", "2022-03-03", 100, Mpa.PG);

        filmStorage.create(filmG1);
        filmStorage.create(filmG2);
        filmStorage.create(filmPG);

        List<Film> filmsG = filmStorage.findByMpa(Mpa.G);

        assertThat(filmsG)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Film G1", "Film G2");
    }

    @Test
    void testGetCommonFriends() {
        User user1 = createUser("user1@example.com", "user1", "User One", "1990-01-01");
        User user2 = createUser("user2@example.com", "user2", "User Two", "1991-02-02");
        User commonFriend = createUser("common@example.com", "common", "Common Friend", "1992-03-03");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);
        User createdCommonFriend = userStorage.create(commonFriend);

        String insertFriendship = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'UNCONFIRMED')";
        jdbcTemplate.update(insertFriendship, createdUser1.getId(), createdCommonFriend.getId());
        jdbcTemplate.update(insertFriendship, createdUser2.getId(), createdCommonFriend.getId());

        List<User> commonFriends = userStorage.findCommonFriends(
                createdUser1.getId(),
                createdUser2.getId()
        );

        assertThat(commonFriends)
                .hasSize(1)
                .extracting(User::getLogin)
                .containsExactly("common");
    }

    @Test
    void testGetUserFriends() {
        User user1 = createUser("user1@example.com", "user1", "User One", "1990-01-01");
        User friend1 = createUser("friend1@example.com", "friend1", "Friend One", "1991-02-02");
        User friend2 = createUser("friend2@example.com", "friend2", "Friend Two", "1992-03-03");

        User createdUser1 = userStorage.create(user1);
        User createdFriend1 = userStorage.create(friend1);
        User createdFriend2 = userStorage.create(friend2);

        String insertFriendship = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'UNCONFIRMED')";
        jdbcTemplate.update(insertFriendship, createdUser1.getId(), createdFriend1.getId());
        jdbcTemplate.update(insertFriendship, createdUser1.getId(), createdFriend2.getId());

        List<User> friends = userStorage.findFriends(createdUser1.getId());

        assertThat(friends)
                .hasSize(2)
                .extracting(User::getLogin)
                .containsExactlyInAnyOrder("friend1", "friend2");
    }

    @Test
    void testFilmsWithMultipleGenres() {
        Film film = createFilm("Mixed Genre Film", "Action comedy", "2020-01-01", 120, Mpa.PG_13);
        film.setGenres(Set.of(Genre.ACTION, Genre.COMEDY));

        Film createdFilm = filmStorage.create(film);

        Film retrievedFilm = filmStorage.findById(createdFilm.getId()).orElseThrow();

        assertThat(retrievedFilm.getGenres())
                .hasSize(2)
                .containsExactlyInAnyOrder(Genre.ACTION, Genre.COMEDY);
    }

    @Test
    void testPopularFilmsWithNoLikes() {
        Film film1 = createFilm("Film 1", "Description 1", "2020-01-01", 90, Mpa.G);
        Film film2 = createFilm("Film 2", "Description 2", "2021-02-02", 95, Mpa.PG);
        Film film3 = createFilm("Film 3", "Description 3", "2022-03-03", 100, Mpa.PG_13);

        filmStorage.create(film1);
        filmStorage.create(film2);
        filmStorage.create(film3);

        List<Film> popularFilms = filmStorage.findTopPopular(10);

        assertThat(popularFilms).hasSize(3);
    }


    private User createUser(String email, String login, String name, String birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.parse(birthday));
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
