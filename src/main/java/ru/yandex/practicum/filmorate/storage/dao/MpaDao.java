package ru.yandex.practicum.filmorate.storage.dao;

import java.util.List;
import java.util.Optional;

import ru.yandex.practicum.filmorate.model.Mpa;

public interface MpaDao {
    List<Mpa> findAll();
    Optional<Mpa> findById(Integer id);
}
