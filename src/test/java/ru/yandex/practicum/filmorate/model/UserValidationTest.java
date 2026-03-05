package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Валидный пользователь не должен иметь ошибок валидации");
    }

    @Test
    void shouldFailValidationWhenEmailIsEmpty() {
        User user = new User();
        user.setEmail("");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Пустой email должен вызвать ошибку валидации");
    }

    @Test
    void shouldFailValidationWhenEmailIsNull() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "null в email должен вызвать ошибку валидации");
    }

    @Test
    void shouldFailValidationWhenEmailWithoutAtSign() {
        User user = new User();
        user.setEmail("userexample.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Email без @ должен вызвать ошибку валидации");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email должен быть корректным")));
    }

    @Test
    void shouldPassValidationWithValidEmail() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Корректный email должен быть валидным");
    }

    @Test
    void shouldFailValidationWhenLoginIsEmpty() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Пустой логин должен вызвать ошибку валидации");
    }

    @Test
    void shouldFailValidationWhenLoginIsNull() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin(null);
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "null в логине должен вызвать ошибку валидации");
    }

    @Test
    void shouldFailValidationWhenLoginContainsSpaces() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Логин с пробелами должен вызвать ошибку валидации");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Логин не может содержать пробелы")));
    }

    @Test
    void shouldPassValidationWhenNameIsEmpty() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Пустое имя должно быть валидным");
    }

    @Test
    void shouldPassValidationWhenNameIsNull() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "null в имени должен быть валидным");
    }

    @Test
    void shouldFailValidationWhenBirthdayInFuture() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Дата рождения в будущем должна вызвать ошибку валидации");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Дата рождения не может быть в будущем")));
    }

    @Test
    void shouldPassValidationWhenBirthdayIsToday() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Сегодняшняя дата рождения должна быть валидной");
    }

    @Test
    void shouldPassValidationWhenBirthdayIsYesterday() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.now().minusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Вчерашняя дата рождения должна быть валидной");
    }
}
