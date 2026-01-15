package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film createFilm(Film film) {
        log.debug("Создание фильма: {}", film);
        validateFilm(film);
        Film createdFilm = filmStorage.create(film);
        log.info("Фильм успешно создан с id={}", createdFilm.getId());
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        log.debug("Обновление фильма: {}", film);
        validateFilm(film);
        Film updatedFilm = filmStorage.update(film);
        log.info("Фильм с id={} успешно обновлен", updatedFilm.getId());
        return updatedFilm;
    }

    public List<Film> getAllFilms() {
        log.debug("Получение всех фильмов");
        List<Film> films = filmStorage.findAll();
        log.info("Найдено {} фильмов", films.size());
        return films;
    }

    public Film getFilmById(Long id) {
        log.debug("Получение фильма по id={}", id);
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.error("Фильм с id={} не найден", id);
                    return new NotFoundException("Фильм с id " + id + " не найден");
                });
        log.info("Фильм с id={} найден", id);
        return film;
    }

    public void addLike(Long filmId, Long userId) {
        log.debug("Добавление лайка: filmId={}, userId={}", filmId, userId);

        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.error("Фильм с id={} не найден при попытке добавить лайк", filmId);
                    return new NotFoundException("Фильм с id " + filmId + " не найден");
                });

        userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден при попытке поставить лайк", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        boolean added = film.getLikes().add(userId);
        filmStorage.update(film);

        if (added) {
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
            log.debug("Количество лайков у фильма {}: {}", filmId, film.getLikes().size());
        } else {
            log.debug("Пользователь {} уже ставил лайк фильму {}", userId, filmId);
        }
    }

    public void removeLike(Long filmId, Long userId) {
        log.debug("Удаление лайка: filmId={}, userId={}", filmId, userId);

        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.error("Фильм с id={} не найден при попытке удалить лайк", filmId);
                    return new NotFoundException("Фильм с id " + filmId + " не найден");
                });

        userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден при попытке удалить лайк", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        boolean removed = film.getLikes().remove(userId);
        filmStorage.update(film);

        if (removed) {
            log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
            log.debug("Количество лайков у фильма {}: {}", filmId, film.getLikes().size());
        } else {
            log.debug("Пользователь {} не ставил лайк фильму {}", userId, filmId);
        }
    }

    public List<Film> getPopularFilms(int limit) {
        log.debug("Получение {} популярных фильмов", limit);

        List<Film> popularFilms = filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        log.info("Запрошено {} популярных фильмов, найдено {}", limit, popularFilms.size());

        return popularFilms;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Попытка создать фильм с датой релиза до {}: {}", CINEMA_BIRTHDAY, film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше " + CINEMA_BIRTHDAY);
        }
    }
}
