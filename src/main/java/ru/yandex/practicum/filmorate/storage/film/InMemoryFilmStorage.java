package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 1;

    @Override
    public Film create(Film film) {
        log.debug("Создание фильма: {}", film);
        film.setId(currentId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно создан с id={}: {}", film.getId(), film.getName());
        log.debug("Текущее количество фильмов в хранилище: {}", films.size());
        return film;
    }

    @Override
    public Film update(Film film) {
        log.debug("Попытка обновления фильма с id={}", film.getId());
        if (!films.containsKey(film.getId())) {
            log.error("Фильм с id={} не найден при попытке обновления", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        Film oldFilm = films.get(film.getId());
        log.debug("Обновление фильма. Старые данные: {}", oldFilm);
        films.put(film.getId(), film);
        log.info("Фильм с id={} успешно обновлён: {}", film.getId(), film.getName());
        log.debug("Новые данные фильма: {}", film);
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        log.debug("Поиск фильма по id={}", id);
        Optional<Film> film = Optional.ofNullable(films.get(id));
        if (film.isPresent()) {
            log.debug("Фильм с id={} найден: {}", id, film.get().getName());
        } else {
            log.debug("Фильм с id={} не найден", id);
        }
        return film;
    }

    @Override
    public List<Film> findAll() {
        log.debug("Получение всех фильмов. Количество: {}", films.size());
        List<Film> allFilms = new ArrayList<>(films.values());
        log.trace("Список всех фильмов: {}", allFilms);
        return allFilms;
    }

    @Override
    public void delete(Long id) {
        log.debug("Попытка удаления фильма с id={}", id);
        Film removed = films.remove(id);
        if (removed != null) {
            log.info("Фильм с id={} успешно удалён: {}", id, removed.getName());
            log.debug("Текущее количество фильмов в хранилище: {}", films.size());
        } else {
            log.error("Фильм с id={} не найден при попытке удаления", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }
}
