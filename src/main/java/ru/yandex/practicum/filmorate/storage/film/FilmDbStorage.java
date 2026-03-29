package ru.yandex.practicum.filmorate.storage.film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        log.debug("Создан фильм с ID: {}", film.getId());


        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film);
        }


        if (film.getLikes() != null && !film.getLikes().isEmpty()) {
            saveLikes(film);
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = ? WHERE film_id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        if (rowsAffected == 0) {
            log.error("Фильм с ID {} не найден для обновления", film.getId());
            throw new RuntimeException("Фильм с ID " + film.getId() + " не найден");
        }

        log.debug("Обновлен фильм с ID: {}", film.getId());


        updateGenres(film);


        updateLikes(film);

        return film;
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                "FROM films f " +
                "ORDER BY f.film_id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper());


        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }

        log.debug("Найдено {} фильмов", films.size());
        return films;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                "FROM films f " +
                "WHERE f.film_id = ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper(), id);

        if (films.isEmpty()) {
            log.debug("Фильм с ID {} не найден", id);
            return Optional.empty();
        }

        Film film = films.get(0);
        loadGenres(film);
        loadLikes(film);

        log.debug("Найден фильм с ID: {}", id);
        return Optional.of(film);
    }


    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }

        log.debug("Сохранено {} жанров для фильма {}", film.getGenres().size(), film.getId());
    }

    private void updateGenres(Film film) {

        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());


        saveGenres(film);
    }

    private void loadGenres(Film film) {
        String sql = "SELECT genre_id FROM film_genres WHERE film_id = ? ORDER BY genre_id";

        Set<Genre> genres = new HashSet<>();

        jdbcTemplate.query(sql, rs -> {
            int genreId = rs.getInt("genre_id");
            Genre genre = Genre.fromId(genreId);
            genres.add(genre);
        }, film.getId());

        film.setGenres(genres);
        log.trace("Загружено {} жанров для фильма {}", genres.size(), film.getId());
    }


    private void saveLikes(Film film) {
        if (film.getLikes() == null || film.getLikes().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

        for (Long userId : film.getLikes()) {
            jdbcTemplate.update(sql, film.getId(), userId);
        }

        log.debug("Сохранено {} лайков для фильма {}", film.getLikes().size(), film.getId());
    }

    private void updateLikes(Film film) {

        String deleteSql = "DELETE FROM likes WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());


        saveLikes(film);
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";

        Set<Long> likes = new HashSet<>();

        jdbcTemplate.query(sql, rs -> {
            likes.add(rs.getLong("user_id"));
        }, film.getId());

        film.setLikes(likes);
        log.trace("Загружено {} лайков для фильма {}", likes.size(), film.getId());
    }


    private RowMapper<Film> filmRowMapper() {
        return (rs, rowNum) -> mapRowToFilm(rs);
    }

    private Film mapRowToFilm(ResultSet rs) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));


        int mpaId = rs.getInt("mpa_id");
        film.setMpa(Mpa.fromId(mpaId));


        film.setGenres(new HashSet<>());
        film.setLikes(new HashSet<>());

        return film;
    }


    public List<Film> findTopPopular(int count) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, " +
                "COUNT(l.user_id) as likes_count " +
                "FROM films f " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper(), count);


        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }

        log.debug("Найдено {} популярных фильмов", films.size());
        return films;
    }


    public List<Film> findByGenre(Genre genre) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                "FROM films f " +
                "JOIN film_genres fg ON f.film_id = fg.film_id " +
                "WHERE fg.genre_id = ? " +
                "ORDER BY f.film_id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper(), genre.getId());


        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }

        log.debug("Найдено {} фильмов с жанром {}", films.size(), genre.getName());
        return films;
    }


    public List<Film> findByMpa(Mpa mpa) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                "FROM films f " +
                "WHERE f.mpa_id = ? " +
                "ORDER BY f.film_id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper(), mpa.getId());

        // Загружаем жанры и лайки для каждого фильма
        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }

        log.debug("Найдено {} фильмов с рейтингом {}", films.size(), mpa.getName());
        return films;
    }


    public void addLike(Long filmId, Long userId) {
        String checkSql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count != null && count > 0) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            return;
        }

        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }


    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, filmId, userId);

        if (rowsAffected > 0) {
            log.debug("Пользователь {} удалил лайк у фильма {}", userId, filmId);
        } else {
            log.warn("Лайк пользователя {} для фильма {} не найден", userId, filmId);
        }
    }


    public int getLikesCount(Long filmId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return count != null ? count : 0;
    }

    public boolean hasUserLiked(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        return count != null && count > 0;
    }
}