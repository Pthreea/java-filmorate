package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
class DatabaseTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testAllTablesExist() {
        String[] tables = {
                "USERS",
                "FILMS",
                "MPA_RATINGS",
                "GENRES",
                "FRIENDSHIPS",
                "LIKES",
                "FILM_GENRES"
        };

        for (String table : tables) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = ?",
                    Integer.class,
                    table
            );
            assertThat(count)
                    .withFailMessage("Таблица " + table + " не найдена")
                    .isGreaterThanOrEqualTo(1);
        }
    }
}