package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate CINEMA_BIRTH_DATE = LocalDate.of(1895, 12, 28);

    private final FilmDbStorage filmStorage;
    private final UserStorage userStorage;

    public Film createFilm(Film film) {
        validateFilm(film);
        Film createdFilm = filmStorage.create(film);
        log.info("Создан фильм: {}", createdFilm);
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        getFilmById(film.getId()); // Проверка существования

        Film updatedFilm = filmStorage.update(film);
        log.info("Обновлен фильм: {}", updatedFilm);
        return updatedFilm;
    }

    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.findAll();
        log.debug("Получен список из {} фильмов", films.size());
        return films;
    }

    public Film getFilmById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.error("Фильм с id={} не найден", id);
                    return new NotFoundException("Фильм с id " + id + " не найден");
                });
    }

    public void addLike(Long filmId, Long userId) {
        // Проверяем существование фильма и пользователя
        getFilmById(filmId);
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        // Проверяем существование фильма и пользователя
        getFilmById(filmId);
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть положительным");
        }

        List<Film> popularFilms = filmStorage.findTopPopular(count);
        log.debug("Получен список из {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(CINEMA_BIRTH_DATE)) {
            throw new ValidationException(
                    "Дата релиза не может быть раньше " + CINEMA_BIRTH_DATE);
        }
    }
}
