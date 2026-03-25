package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.RequiredArgsConstructor;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DatabaseSchemaTest {

    private final JdbcTemplate jdbcTemplate;

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
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?",
                    Integer.class,
                    table
            );
            assertThat(count)
                    .withFailMessage("Таблица " + table + " не найдена")
                    .isEqualTo(1);
        }
    }

    @Test
    void testMpaRatingsDataInitialized() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mpa_ratings",
                Integer.class
        );
        assertThat(count).isEqualTo(5);
    }

    @Test
    void testGenresDataInitialized() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM genres",
                Integer.class
        );
        assertThat(count).isEqualTo(6);
    }

    @Test
    void testUserTableStructure() {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'USERS' ORDER BY ORDINAL_POSITION";

        var columns = jdbcTemplate.queryForList(sql, String.class);

        assertThat(columns).containsExactly(
                "USER_ID",
                "EMAIL",
                "LOGIN",
                "NAME",
                "BIRTHDAY",
                "CREATED_AT"
        );
    }

    @Test
    void testFilmTableStructure() {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'FILMS' ORDER BY ORDINAL_POSITION";

        var columns = jdbcTemplate.queryForList(sql, String.class);

        assertThat(columns).containsExactly(
                "FILM_ID",
                "NAME",
                "DESCRIPTION",
                "RELEASE_DATE",
                "DURATION",
                "MPA_ID",
                "CREATED_AT"
        );
    }

    @Test
    void testFriendshipsTableStructure() {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'FRIENDSHIPS' ORDER BY ORDINAL_POSITION";

        var columns = jdbcTemplate.queryForList(sql, String.class);

        assertThat(columns).containsExactly(
                "USER_ID",
                "FRIEND_ID",
                "STATUS",
                "CREATED_AT"
        );
    }

    @Test
    void testLikesTableStructure() {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'LIKES' ORDER BY ORDINAL_POSITION";

        var columns = jdbcTemplate.queryForList(sql, String.class);

        assertThat(columns).containsExactly(
                "FILM_ID",
                "USER_ID",
                "CREATED_AT"
        );
    }

    @Test
    void testIndexesExist() {
        String sql = "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES " +
                "WHERE TABLE_NAME = 'FILMS' AND INDEX_NAME LIKE 'IDX_%'";

        var indexes = jdbcTemplate.queryForList(sql, String.class);

        assertThat(indexes).contains(
                "IDX_FILMS_MPA",
                "IDX_FILMS_RELEASE_DATE"
        );
    }

    @Test
    void testForeignKeyConstraints() {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.CONSTRAINTS " +
                "WHERE TABLE_NAME = 'FILMS' AND CONSTRAINT_TYPE = 'REFERENTIAL'";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertThat(count).isGreaterThan(0);
    }
}
