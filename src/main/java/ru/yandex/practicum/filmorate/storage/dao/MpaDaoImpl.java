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
import ru.yandex.practicum.filmorate.model.Mpa;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaDaoImpl implements MpaDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> findAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY mpa_id";
        List<Mpa> mpaList = jdbcTemplate.query(sql, mpaRowMapper());
        log.debug("Найдено {} рейтингов MPA", mpaList.size());
        return mpaList;
    }

    @Override
    public Optional<Mpa> findById(Integer id) {
        String sql = "SELECT * FROM mpa_ratings WHERE mpa_id = ?";
        List<Mpa> mpaList = jdbcTemplate.query(sql, mpaRowMapper(), id);

        if (mpaList.isEmpty()) {
            log.debug("Рейтинг MPA с ID {} не найден", id);
            return Optional.empty();
        }

        log.debug("Найден рейтинг MPA с ID: {}", id);
        return Optional.of(mpaList.get(0));
    }

    private RowMapper<Mpa> mpaRowMapper() {
        return (rs, rowNum) -> mapRowToMpa(rs);
    }

    private Mpa mapRowToMpa(ResultSet rs) throws SQLException {
        String code = rs.getString("code");
        return switch (code) {
            case "G" -> Mpa.G;
            case "PG" -> Mpa.PG;
            case "PG-13" -> Mpa.PG_13;
            case "R" -> Mpa.R;
            case "NC-17" -> Mpa.NC_17;
            default -> throw new IllegalArgumentException("Неизвестный код MPA: " + code);
        };
    }
}
