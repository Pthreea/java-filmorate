package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAllGenres() {
        String sql = "SELECT genre_id, name FROM genres ORDER BY genre_id";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre);
        log.debug("Found {} genres", genres.size());
        return genres;
    }

    @Override
    public Optional<Genre> getGenreById(Integer id) {
        String sql = "SELECT genre_id, name FROM genres WHERE genre_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre, id);

        if (genres.isEmpty()) {
            log.debug("Genre with id={} not found", id);
            return Optional.empty();
        }

        log.debug("Found genre: {}", genres.get(0).getName());
        return Optional.of(genres.get(0));
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("genre_id"))
                .name(rs.getString("name"))
                .build();
    }
}
