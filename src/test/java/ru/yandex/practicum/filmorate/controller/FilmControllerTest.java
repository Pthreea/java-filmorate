package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;
    private FilmStorage filmStorage;
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);
        filmController = new FilmController(filmStorage, filmService);
    }

    @Test
    void shouldCreateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.createFilm(film);

        assertNotNull(createdFilm.getId());
        assertEquals("Test Film", createdFilm.getName());
        assertEquals(1, filmController.getAllFilms().size());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film();
        film.setName("Original Name");
        film.setDescription("Original Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.createFilm(film);

        createdFilm.setName("Updated Name");
        Film updatedFilm = filmController.updateFilm(createdFilm);

        assertEquals("Updated Name", updatedFilm.getName());
        assertEquals(createdFilm.getId(), updatedFilm.getId());
    }

    @Test
    void shouldThrowExceptionWhenUpdateNonExistentFilm() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(NotFoundException.class, () -> filmController.updateFilm(film));
    }

    @Test
    void shouldGetFilmById() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.createFilm(film);
        Film foundFilm = filmController.getFilmById(createdFilm.getId());

        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals("Test Film", foundFilm.getName());
    }

    @Test
    void shouldThrowExceptionWhenGetNonExistentFilm() {
        assertThrows(NotFoundException.class, () -> filmController.getFilmById(999L));
    }

    @Test
    void shouldGetAllFilms() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(130);

        filmController.createFilm(film1);
        filmController.createFilm(film2);

        assertEquals(2, filmController.getAllFilms().size());
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateBeforeCinemaBirthday() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void shouldAllowReleaseDateOnCinemaBirthday() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120);

        assertDoesNotThrow(() -> filmController.createFilm(film));
    }

    @Test
    void shouldAllowReleaseDateAfterCinemaBirthday() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 29));
        film.setDuration(120);

        Film createdFilm = filmController.createFilm(film);
        assertNotNull(createdFilm.getId());
    }
}
