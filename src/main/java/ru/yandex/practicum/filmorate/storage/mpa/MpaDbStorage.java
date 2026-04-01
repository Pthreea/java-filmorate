package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getAllMpa() {
        String sql = "SELECT mpa_id, name FROM mpa_ratings ORDER BY mpa_id";
        List<Mpa> mpaList = jdbcTemplate.query(sql, this::mapRowToMpa);
        log.debug("Found {} MPA ratings", mpaList.size());
        return mpaList;
    }

    @Override
    public Optional<Mpa> getMpaById(Integer id) {
        String sql = "SELECT mpa_id, name FROM mpa_ratings WHERE mpa_id = ?";
        List<Mpa> mpaList = jdbcTemplate.query(sql, this::mapRowToMpa, id);

        if (mpaList.isEmpty()) {
            log.debug("MPA rating with id={} not found", id);
            return Optional.empty();
        }

        log.debug("Found MPA rating: {}", mpaList.get(0).getName());
        return Optional.of(mpaList.get(0));
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return Mpa.builder()
                .id(rs.getInt("mpa_id"))
                .name(rs.getString("name"))
                .build();
    }
}
