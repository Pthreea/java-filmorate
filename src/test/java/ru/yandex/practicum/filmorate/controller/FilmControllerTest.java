package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    @Test
    void shouldCreateFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Научно-фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Film created = filmController.createFilm(film);

        assertNotNull(created.getId(), "ID должен быть присвоен");
        assertEquals("Интерстеллар", created.getName());
        assertEquals(1, filmController.getAllFilms().size());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Научно-фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Film created = filmController.createFilm(film);

        created.setName("Интерстеллар (обновлено)");
        Film updated = filmController.updateFilm(created);

        assertEquals("Интерстеллар (обновлено)", updated.getName());
        assertEquals(1, filmController.getAllFilms().size());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentFilm() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        assertThrows(RuntimeException.class, () -> filmController.updateFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateBeforeCinemaBirthday() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // День до рождения кино
        film.setDuration(169);

        assertThrows(IllegalArgumentException.class, () -> filmController.createFilm(film));
    }

    @Test
    void shouldCreateFilmWithReleaseDateAtCinemaBirthday() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 28)); // День рождения кино
        film.setDuration(169);

        Film created = filmController.createFilm(film);

        assertNotNull(created.getId());
        assertEquals(LocalDate.of(1895, 12, 28), created.getReleaseDate());
    }

    @Test
    void shouldGetAllFilms() {
        Film film1 = new Film();
        film1.setName("Фильм 1");
        film1.setDescription("Описание 1");
        film1.setReleaseDate(LocalDate.of(2014, 11, 6));
        film1.setDuration(120);

        Film film2 = new Film();
        film2.setName("Фильм 2");
        film2.setDescription("Описание 2");
        film2.setReleaseDate(LocalDate.of(2015, 11, 6));
        film2.setDuration(130);

        filmController.createFilm(film1);
        filmController.createFilm(film2);

        assertEquals(2, filmController.getAllFilms().size());
    }
}
