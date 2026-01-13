package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

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

        int likesBefore = film.getLikes().size();
        boolean wasAdded = film.getLikes().add(userId);

        if (wasAdded) {
            filmStorage.update(film);
            log.info("Пользователь {} поставил лайк фильму {}. Всего лайков: {} -> {}",
                    userId, filmId, likesBefore, film.getLikes().size());
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

        int likesBefore = film.getLikes().size();
        boolean wasRemoved = film.getLikes().remove(userId);

        if (wasRemoved) {
            filmStorage.update(film);
            log.info("Пользователь {} удалил лайк у фильма {}. Всего лайков: {} -> {}",
                    userId, filmId, likesBefore, film.getLikes().size());
        } else {
            log.debug("Пользователь {} не ставил лайк фильму {}", userId, filmId);
        }
    }

    public List<Film> getPopularFilms(Integer count) {
        int limit = count != null && count > 0 ? count : 10;
        log.debug("Получение популярных фильмов: limit={}", limit);

        List<Film> popularFilms = filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(limit)
                .collect(Collectors.toList());

        log.info("Запрошено {} популярных фильмов, найдено {}", limit, popularFilms.size());
        log.trace("Популярные фильмы: {}", popularFilms);

        return popularFilms;
    }
}