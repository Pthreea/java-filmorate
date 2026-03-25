package ru.yandex.practicum.filmorate.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;
import ru.yandex.practicum.filmorate.storage.dao.GenreDao;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreDao genreDao;

    @GetMapping
    public List<GenreDto> getAllGenres() {
        log.debug("Получен запрос GET /genres");
        List<Genre> genres = genreDao.findAll();
        List<GenreDto> result = genres.stream()
                .map(GenreDto::from)
                .collect(Collectors.toList());
        log.info("Возвращено {} жанров", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public GenreDto getGenreById(@PathVariable Integer id) {
        log.debug("Получен запрос GET /genres/{}", id);
        Genre genre = genreDao.findById(id)
                .orElseThrow(() -> {
                    log.error("Жанр с id={} не найден", id);
                    return new NotFoundException("Жанр с id " + id + " не найден");
                });
        GenreDto result = GenreDto.from(genre);
        log.info("Найден жанр с id={}: {}", id, result.getName());
        return result;
    }
}
