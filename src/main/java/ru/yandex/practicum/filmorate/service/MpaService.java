package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaStorage mpaStorage;

    public List<Mpa> getAllMpa() {
        log.debug("Getting all MPA ratings");
        return mpaStorage.getAllMpa();
    }

    public Mpa getMpaById(Integer id) {
        log.debug("Getting MPA rating with id={}", id);
        return mpaStorage.getMpaById(id)
                .orElseThrow(() -> new NotFoundException("MPA рейтинг с id=" + id + " не найден"));
    }
}
