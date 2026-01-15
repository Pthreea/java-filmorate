package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.debug("Получен запрос POST /films: {}", film);
        Film createdFilm = filmService.createFilm(film);
        log.info("Создан фильм с id={}", createdFilm.getId());
        return createdFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.debug("Получен запрос PUT /films: {}", film);
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Обновлён фильм с id={}", updatedFilm.getId());
        return updatedFilm;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Получен запрос GET /films");
        List<Film> films = filmService.getAllFilms();
        log.info("Возвращено {} фильмов", films.size());
        return films;
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.debug("Получен запрос GET /films/{}", id);
        Film film = filmService.getFilmById(id);
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
}
