package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
class DatabaseSchemaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testAllTablesExist() {
        String[] expectedTables = {
                "USERS",
                "FILMS",
                "MPA_RATINGS",
                "GENRES",
                "FRIENDSHIPS",
                "LIKES",
                "FILM_GENRES"
        };

        for (String tableName : expectedTables) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = ?",
                    Integer.class,
                    tableName
            );
            assertThat(count)
                    .withFailMessage("Таблица " + tableName + " не найдена")
                    .isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void testUserTableStructure() {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'USERS' " +
                "ORDER BY ORDINAL_POSITION";

        List<String> columns = jdbcTemplate.queryForList(sql, String.class);

        assertThat(columns)
                .as("Структура таблицы USERS")
                .containsExactly(
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
                "WHERE TABLE_NAME = 'FILMS' " +
                "ORDER BY ORDINAL_POSITION";

        List<String> columns = jdbcTemplate.queryForList(sql, String.class);

        assertThat(columns)
                .as("Структура таблицы FILMS")
                .containsExactly(
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
    void testMpaRatingsTableStructure() {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'MPA_RATINGS' " +
                "ORDER BY ORDINAL_POSITION";

        List<String> columns = jdbcTemplate.queryForList(sql, String.class);

        assertThat(columns)
                .as("Структура таблицы MPA_RATINGS")
                .containsExactly(
                        "MPA_ID",
                        "NAME",
                        "DESCRIPTION"
                );
    }

    @Test
    void testGenresTableStructure() {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'GENRES' " +
                "ORDER BY ORDINAL_POSITION";

        List<String> columns = jdbcTemplate.queryForList(sql, String.class);

        assertThat(columns)
                .as("Структура таблицы GENRES")
                .containsExactly(
                        "GENRE_ID",
                        "NAME"
                );
    }

    @Test
    void testFriendshipsTableStructure() {

        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'FRIENDSHIPS' " +
                "ORDER BY ORDINAL_POSITION";


        List<String> columns = jdbcTemplate.queryForList(sql, String.class);


        assertThat(columns)
                .as("Структура таблицы FRIENDSHIPS")
                .contains(
                        "USER_ID",
                        "FRIEND_ID",
                        "STATUS"
                );
    }

    @Test
    void testLikesTableStructure() {

        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'LIKES' " +
                "ORDER BY ORDINAL_POSITION";


        List<String> columns = jdbcTemplate.queryForList(sql, String.class);

        assertThat(columns)
                .as("Структура таблицы LIKES")
                .contains(
                        "FILM_ID",
                        "USER_ID"
                );
    }

    @Test
    void testFilmGenresTableStructure() {

        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'FILM_GENRES' " +
                "ORDER BY ORDINAL_POSITION";


        List<String> columns = jdbcTemplate.queryForList(sql, String.class);


        assertThat(columns)
                .as("Структура таблицы FILM_GENRES")
                .containsExactly(
                        "FILM_ID",
                        "GENRE_ID"
                );
    }

    @Test
    void testForeignKeyConstraints() {


        String sqlFilmsMpa = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.CONSTRAINTS " +
                "WHERE TABLE_NAME = 'FILMS' " +
                "AND CONSTRAINT_TYPE = 'REFERENTIAL' " +
                "AND CONSTRAINT_NAME LIKE '%MPA%'";

        Integer filmsMpaFk = jdbcTemplate.queryForObject(sqlFilmsMpa, Integer.class);
        assertThat(filmsMpaFk)
                .withFailMessage("Foreign key FILMS -> MPA_RATINGS не найден")
                .isGreaterThanOrEqualTo(1);


        String sqlLikesFilm = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.CONSTRAINTS " +
                "WHERE TABLE_NAME = 'LIKES' " +
                "AND CONSTRAINT_TYPE = 'REFERENTIAL' " +
                "AND CONSTRAINT_NAME LIKE '%FILM%'";

        Integer likesFilmFk = jdbcTemplate.queryForObject(sqlLikesFilm, Integer.class);
        assertThat(likesFilmFk)
                .withFailMessage("Foreign key LIKES -> FILMS не найден")
                .isGreaterThanOrEqualTo(1);

        String sqlLikesUser = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.CONSTRAINTS " +
                "WHERE TABLE_NAME = 'LIKES' " +
                "AND CONSTRAINT_TYPE = 'REFERENTIAL' " +
                "AND CONSTRAINT_NAME LIKE '%USER%'";

        Integer likesUserFk = jdbcTemplate.queryForObject(sqlLikesUser, Integer.class);
        assertThat(likesUserFk)
                .withFailMessage("Foreign key LIKES -> USERS не найден")
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    void testPrimaryKeys() {

        String[] tablesWithPk = {"USERS", "FILMS", "MPA_RATINGS", "GENRES"};

        for (String tableName : tablesWithPk) {
            String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.CONSTRAINTS " +
                    "WHERE TABLE_NAME = ? " +
                    "AND CONSTRAINT_TYPE = 'PRIMARY KEY'";

            Integer pkCount = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            assertThat(pkCount)
                    .withFailMessage("Primary key для таблицы " + tableName + " не найден")
                    .isEqualTo(1);
        }
    }

    @Test
    void testUniqueConstraints() {

        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.CONSTRAINTS " +
                "WHERE TABLE_NAME = 'USERS' " +
                "AND CONSTRAINT_TYPE = 'UNIQUE'";

        Integer uniqueCount = jdbcTemplate.queryForObject(sql, Integer.class);
        assertThat(uniqueCount)
                .withFailMessage("Уникальные ограничения для EMAIL и LOGIN не найдены")
                .isGreaterThanOrEqualTo(2); // EMAIL и LOGIN должны быть уникальными
    }

    @Test
    void testIndexesExist() {

        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES " +
                "WHERE TABLE_NAME = 'LIKES'";

        Integer indexCount = jdbcTemplate.queryForObject(sql, Integer.class);
        assertThat(indexCount)
                .withFailMessage("Индексы для таблицы LIKES не найдены")
                .isGreaterThan(0);
    }
}
