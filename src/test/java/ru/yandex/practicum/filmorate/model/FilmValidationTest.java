package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Научно-фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Валидный фильм не должен иметь ошибок валидации");
    }

    @Test
    void shouldFailValidationWhenNameIsEmpty() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Пустое название должно вызвать ошибку валидации");
        assertEquals(1, violations.size());
        assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailValidationWhenNameIsBlank() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Название из пробелов должно вызвать ошибку валидации");
    }

    @Test
    void shouldFailValidationWhenNameIsNull() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "null в названии должен вызвать ошибку валидации");
    }

    @Test
    void shouldPassValidationWithDescriptionLength200() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("А".repeat(200));
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Описание длиной 200 символов должно быть валидным");
    }

    @Test
    void shouldFailValidationWhenDescriptionLength201() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("А".repeat(201));
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Описание длиной 201 символ должно вызвать ошибку валидации");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Максимальная длина описания - 200 символов")));
    }

    @Test
    void shouldPassValidationWithEmptyDescription() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Пустое описание должно быть валидным");
    }

    @Test
    void shouldFailValidationWhenReleaseDateIsNull() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(null);
        film.setDuration(169);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "null в дате релиза должен вызвать ошибку валидации");
    }

    @Test
    void shouldPassValidationWithReleaseDateAtCinemaBirthday() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(169);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Дата релиза 28.12.1895 должна быть валидной");
    }

    @Test
    void shouldFailValidationWhenDurationIsZero() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Продолжительность 0 должна вызвать ошибку валидации");
    }

    @Test
    void shouldFailValidationWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(-1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Отрицательная продолжительность должна вызвать ошибку валидации");
        assertEquals("Продолжительность фильма должна быть положительной",
                violations.iterator().next().getMessage());
    }

    @Test
    void shouldPassValidationWithDurationOne() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Продолжительность 1 минута должна быть валидной");
    }
}
