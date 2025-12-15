package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 1;

    @GetMapping
    public List<Film> getAllFilms(){
        log.info("Получен запрос на получение всех фильмов. Количество: {}\", films.size()");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film){
        film.setId(currentId++);
        films.put(film.getId(),film);
        log.info("Создан новый фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film){
        if(!films.containsKey(film.getId())){
            log.warn("Попытка обновить несуществующий фильм с id: {}", film.getId());
            throw new RuntimeException("Фильм с id " + film.getId() + " не найден.");
        }

        films.put(film.getId(), film);
        log.info("Обновлен фильм: {}", film);
        return film;
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id){
        Film film = films.get(id);
        if(film == null){
            log.warn("Фильм с id {} не найден",id);
            throw new RuntimeException("Фильм с id " + id + " не найден.");
        }

        log.info("Получен фильм: {}",film);
        return film;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Дата релиза не может быть раньше {}", CINEMA_BIRTHDAY);
            throw new IllegalArgumentException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
