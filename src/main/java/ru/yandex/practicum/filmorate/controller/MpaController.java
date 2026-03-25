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
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.dto.MpaDto;
import ru.yandex.practicum.filmorate.storage.dao.MpaDao;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaDao mpaDao;

    @GetMapping
    public List<MpaDto> getAllMpa() {
        log.debug("Получен запрос GET /mpa");
        List<Mpa> mpaList = mpaDao.findAll();
        List<MpaDto> result = mpaList.stream()
                .map(MpaDto::from)
                .collect(Collectors.toList());
        log.info("Возвращено {} рейтингов MPA", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public MpaDto getMpaById(@PathVariable Integer id) {
        log.debug("Получен запрос GET /mpa/{}", id);
        Mpa mpa = mpaDao.findById(id)
                .orElseThrow(() -> {
                    log.error("Рейтинг MPA с id={} не найден", id);
                    return new NotFoundException("Рейтинг MPA с id " + id + " не найден");
                });
        MpaDto result = MpaDto.from(mpa);
        log.info("Найден рейтинг MPA с id={}: {}", id, result.getName());
        return result;
    }
}