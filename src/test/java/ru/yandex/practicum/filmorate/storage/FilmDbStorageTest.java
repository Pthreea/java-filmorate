package ru.yandex.practicum.filmorate.storage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private FilmDbStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new FilmDbStorage(jdbcTemplate);
    }

    @Test
    void testCreateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(Mpa.PG);
        film.setGenres(Set.of(Genre.COMEDY, Genre.DRAMA));

        Film createdFilm = filmStorage.create(film);

        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Test Film");
        assertThat(createdFilm.getMpa()).isEqualTo(Mpa.PG);
    }

    @Test
    void testFindFilmById() {
        Film film = new Film();
        film.setName("Find Film");
        film.setDescription("Find Description");
        film.setReleaseDate(LocalDate.of(2010, 5, 15));
        film.setDuration(90);
        film.setMpa(Mpa.G);

        Film createdFilm = filmStorage.create(film);
        Optional<Film> foundFilm = filmStorage.findById(createdFilm.getId());

        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getId()).isEqualTo(createdFilm.getId());
        assertThat(foundFilm.get().getName()).isEqualTo("Find Film");
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Update Film");
        film.setDescription("Update Description");
        film.setReleaseDate(LocalDate.of(2015, 3, 20));
        film.setDuration(100);
        film.setMpa(Mpa.PG_13);

        Film createdFilm = filmStorage.create(film);
        createdFilm.setName("Updated Film Name");

        Film updatedFilm = filmStorage.update(createdFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film Name");
    }

    @Test
    void testFindAllFilms() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        film1.setMpa(Mpa.G);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2010, 5, 15));
        film2.setDuration(90);
        film2.setMpa(Mpa.PG);

        filmStorage.create(film1);
        filmStorage.create(film2);

        List<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    void testFilmWithGenres() {
        Film film = new Film();
        film.setName("Film with Genres");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(150);
        film.setMpa(Mpa.R);
        film.setGenres(Set.of(Genre.ACTION, Genre.THRILLER));

        Film createdFilm = filmStorage.create(film);
        Optional<Film> foundFilm = filmStorage.findById(createdFilm.getId());

        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getGenres()).containsExactlyInAnyOrder(Genre.ACTION, Genre.THRILLER);
    }
}
