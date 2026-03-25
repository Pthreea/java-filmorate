package ru.yandex.practicum.filmorate.storage.dao;

import java.util.List;
import java.util.Optional;

import ru.yandex.practicum.filmorate.model.Genre;

public interface GenreDao {
    List<Genre> findAll();
    Optional<Genre> findById(Integer id);
}
