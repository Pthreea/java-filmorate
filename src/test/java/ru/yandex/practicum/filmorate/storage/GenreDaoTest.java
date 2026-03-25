package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreDaoImpl;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDaoTest {

    private final JdbcTemplate jdbcTemplate;
    private GenreDaoImpl genreDao;

    @BeforeEach
    void setUp() {
        genreDao = new GenreDaoImpl(jdbcTemplate);
    }

    @Test
    void testFindAllGenres() {
        List<Genre> genres = genreDao.findAll();

        assertThat(genres)
                .isNotNull()
                .hasSize(6)
                .containsExactly(
                        Genre.COMEDY,
                        Genre.DRAMA,
                        Genre.CARTOON,
                        Genre.THRILLER,
                        Genre.DOCUMENTARY,
                        Genre.ACTION
                );
    }

    @Test
    void testFindGenreById_Comedy() {
        Optional<Genre> genre = genreDao.findById(1);

        assertThat(genre)
                .isPresent()
                .hasValue(Genre.COMEDY);
    }

    @Test
    void testFindGenreById_Drama() {
        Optional<Genre> genre = genreDao.findById(2);

        assertThat(genre)
                .isPresent()
                .hasValue(Genre.DRAMA);
    }

    @Test
    void testFindGenreById_Cartoon() {
        Optional<Genre> genre = genreDao.findById(3);

        assertThat(genre)
                .isPresent()
                .hasValue(Genre.CARTOON);
    }

    @Test
    void testFindGenreById_Thriller() {
        Optional<Genre> genre = genreDao.findById(4);

        assertThat(genre)
                .isPresent()
                .hasValue(Genre.THRILLER);
    }

    @Test
    void testFindGenreById_Documentary() {
        Optional<Genre> genre = genreDao.findById(5);

        assertThat(genre)
                .isPresent()
                .hasValue(Genre.DOCUMENTARY);
    }

    @Test
    void testFindGenreById_Action() {
        Optional<Genre> genre = genreDao.findById(6);

        assertThat(genre)
                .isPresent()
                .hasValue(Genre.ACTION);
    }

    @Test
    void testFindGenreById_NotFound() {
        Optional<Genre> genre = genreDao.findById(999);

        assertThat(genre).isEmpty();
    }

    @Test
    void testGenreProperties() {
        Optional<Genre> comedy = genreDao.findById(1);

        assertThat(comedy).isPresent();
        assertThat(comedy.get().getName()).isEqualTo("Комедия");
    }
}