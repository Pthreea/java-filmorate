package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidUser() {
        // Given
        User user = new User();
        user.setEmail("valid@example.com");
        user.setLogin("validlogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenEmailIsInvalid() {
        // Given
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailValidationWhenEmailIsEmpty() {
        // Given
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailValidationWhenLoginIsEmpty() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailValidationWhenLoginContainsSpaces() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("login with spaces");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailValidationWhenBirthdayIsInFuture() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldPassValidationWhenBirthdayIsToday() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now());

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldPassValidationWithEmptyName() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("login");
        user.setName(""); // Пустое имя допустимо
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }
}
