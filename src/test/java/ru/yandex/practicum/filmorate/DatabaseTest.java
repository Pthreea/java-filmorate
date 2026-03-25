package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatabaseTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDatabaseConnection() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertThat(result).isEqualTo(1);
    }

    @Test
    void testMpaRatingsInitialized() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mpa_ratings",
                Integer.class
        );
        assertThat(count).isEqualTo(5);
    }

    @Test
    void testGenresInitialized() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM genres",
                Integer.class
        );
        assertThat(count).isEqualTo(6);
    }

    @Test
    void testAllTablesExist() {
        // Проверяем что все таблицы созданы
        String[] tables = {
                "users", "films", "mpa_ratings", "genres",
                "friendships", "likes", "film_genres"
        };

        for (String table : tables) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?",
                    Integer.class,
                    table.toUpperCase()
            );
            assertThat(count)
                    .withFailMessage("Таблица " + table + " не найдена")
                    .isEqualTo(1);
        }
    }
}