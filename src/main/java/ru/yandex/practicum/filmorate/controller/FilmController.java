package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.debug("Получен запрос POST /films: {}", film);
        validateFilm(film);
        Film createdFilm = filmStorage.create(film);
        log.info("Создан фильм с id={}", createdFilm.getId());
        return createdFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.debug("Получен запрос PUT /films: {}", film);
        validateFilm(film);
        Film updatedFilm = filmStorage.update(film);
        log.info("Обновлён фильм с id={}", updatedFilm.getId());
        return updatedFilm;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Получен запрос GET /films");
        List<Film> films = filmStorage.findAll();
        log.info("Возвращено {} фильмов", films.size());
        return films;
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.debug("Получен запрос GET /films/{}", id);
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id {} не найден", id);
                    return new NotFoundException("Фильм с id " + id + " не найден");
                });
        log.info("Найден фильм с id={}", id);
        return film;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("Получен запрос PUT /films/{}/like/{}", id, userId);
        filmService.addLike(id, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("Получен запрос DELETE /films/{}/like/{}", id, userId);
        filmService.removeLike(id, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, id);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") Integer count) {
        log.debug("Получен запрос GET /films/popular?count={}", count);
        List<Film> popularFilms = filmService.getPopularFilms(count);
        log.info("Возвращено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Попытка создать фильм с датой релиза до {}: {}", CINEMA_BIRTHDAY, film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше " + CINEMA_BIRTHDAY);
        }
    }
}
