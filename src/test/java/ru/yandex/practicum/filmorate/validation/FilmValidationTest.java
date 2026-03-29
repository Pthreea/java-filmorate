package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FilmValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidFilm() {
        // Given
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(Mpa.G);

        // When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Then
        assertThat(violations)
                .as("Валидный фильм не должен иметь ошибок валидации")
                .isEmpty();
    }

    @Test
    void shouldFailValidationWhenNameIsEmpty() {
        // Given
        Film film = new Film();
        film.setName("");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(Mpa.G);

        // When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Then
        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldFailValidationWhenDescriptionTooLong() {
        // Given
        String longDescription = "a".repeat(201);

        Film film = new Film();
        film.setName("Film");
        film.setDescription(longDescription);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(Mpa.G);

        // When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldPassValidationWithDescriptionLength200() {
        // Given
        String description = "a".repeat(200);

        Film film = new Film();
        film.setName("Film");
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(Mpa.G);

        // When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Then
        assertThat(violations)
                .as("Описание длиной 200 символов должно быть валидным")
                .isEmpty();
    }

    @Test
    void shouldPassValidationWithEmptyDescription() {
        // Given
        Film film = new Film();
        film.setName("Film");
        film.setDescription("");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(Mpa.G);

        // When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Then
        assertThat(violations)
                .as("Пустое описание должно быть валидным")
                .isEmpty();
    }

    @Test
    void shouldPassValidationWithReleaseDateAtCinemaBirthday() {
        // Given
        Film film = new Film();
        film.setName("First Film");
        film.setDescription("Historic film");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(1);
        film.setMpa(Mpa.G);

        // When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Then
        assertThat(violations)
                .as("Дата релиза 28.12.1895 должна быть валидной")
                .isEmpty();
    }

    @Test
    void shouldPassValidationWithDurationOne() {
        // Given
        Film film = new Film();
        film.setName("Short Film");
        film.setDescription("Very short");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(1);
        film.setMpa(Mpa.G);

        // When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Then
        assertThat(violations)
                .as("Продолжительность 1 минута должна быть валидной")
                .isEmpty();
    }

    @Test
    void shouldFailValidationWhenDurationIsNegative() {
        // Given
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(-10);
        film.setMpa(Mpa.G);

        // When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailValidationWhenDurationIsZero() {
        // Given
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(0);
        film.setMpa(Mpa.G);

        // When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailValidationWhenMpaIsNull() {
        // Given
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(null);

        // When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Then
        assertThat(violations).isNotEmpty();
    }
}