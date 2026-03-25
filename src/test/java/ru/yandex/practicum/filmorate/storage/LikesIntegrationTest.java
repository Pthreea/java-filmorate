package ru.yandex.practicum.filmorate.storage;

import java.time.LocalDate;
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
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LikesIntegrationTest {

    private final JdbcTemplate jdbcTemplate;
    private FilmDbStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new FilmDbStorage(jdbcTemplate);
    }

    @Test
    void testAddLikesToFilm() {
        Film film = createFilm("Popular Film", Mpa.G);
        Film createdFilm = filmStorage.create(film);

        createdFilm.getLikes().add(1L);
        createdFilm.getLikes().add(2L);
        createdFilm.getLikes().add(3L);
        filmStorage.update(createdFilm);

        Film foundFilm = filmStorage.findById(createdFilm.getId()).orElseThrow();
        assertThat(foundFilm.getLikes())
                .hasSize(3)
                .containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void testRemoveLikeFromFilm() {
        Film film = createFilm("Film", Mpa.PG);
        film.setLikes(Set.of(1L, 2L, 3L));
        Film createdFilm = filmStorage.create(film);

        createdFilm.getLikes().remove(2L);
        filmStorage.update(createdFilm);

        Film foundFilm = filmStorage.findById(createdFilm.getId()).orElseThrow();
        assertThat(foundFilm.getLikes())
                .hasSize(2)
                .containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    void testFilmsOrderedByLikes() {
        Film film1 = createFilm("Film 1", Mpa.G);
        film1.setLikes(Set.of(1L)); // 1 лайк

        Film film2 = createFilm("Film 2", Mpa.PG);
        film2.setLikes(Set.of(1L, 2L, 3L)); // 3 лайка

        Film film3 = createFilm("Film 3", Mpa.PG_13);
        film3.setLikes(Set.of(1L, 2L)); // 2 лайка

        filmStorage.create(film1);
        filmStorage.create(film2);
        filmStorage.create(film3);

        List<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(3);

        Film mostPopular = films.stream()
                .max((f1, f2) -> Integer.compare(f1.getLikes().size(), f2.getLikes().size()))
                .orElseThrow();

        assertThat(mostPopular.getName()).isEqualTo("Film 2");
        assertThat(mostPopular.getLikes()).hasSize(3);
    }

    @Test
    void testFilmWithNoLikes() {
        Film film = createFilm("Unpopular Film", Mpa.NC_17);

        Film createdFilm = filmStorage.create(film);

        Film foundFilm = filmStorage.findById(createdFilm.getId()).orElseThrow();
        assertThat(foundFilm.getLikes()).isEmpty();
    }

    private Film createFilm(String name, Mpa mpa) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);
        film.setGenres(new HashSet<>());
        film.setLikes(new HashSet<>());
        return film;
    }
}
