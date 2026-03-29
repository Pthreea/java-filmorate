package ru.yandex.practicum.filmorate.storage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Genre;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreDaoImpl implements GenreDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY genre_id";
        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper());
        log.debug("Найдено {} жанров", genres.size());
        return genres;
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper(), id);

        if (genres.isEmpty()) {
            log.debug("Жанр с ID {} не найден", id);
            return Optional.empty();
        }

        log.debug("Найден жанр с ID: {}", id);
        return Optional.of(genres.get(0));
    }

    private RowMapper<Genre> genreRowMapper() {
        return (rs, rowNum) -> mapRowToGenre(rs);
    }

    private Genre mapRowToGenre(ResultSet rs) throws SQLException {
        int id = rs.getInt("genre_id");
        return Genre.fromId(id);
    }
}