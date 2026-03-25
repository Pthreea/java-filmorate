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
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.MpaDaoImpl;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDaoTest {

    private final JdbcTemplate jdbcTemplate;
    private MpaDaoImpl mpaDao;

    @BeforeEach
    void setUp() {
        mpaDao = new MpaDaoImpl(jdbcTemplate);
    }

    @Test
    void testFindAllMpa() {
        List<Mpa> mpaList = mpaDao.findAll();

        assertThat(mpaList)
                .isNotNull()
                .hasSize(5)
                .containsExactly(Mpa.G, Mpa.PG, Mpa.PG_13, Mpa.R, Mpa.NC_17);
    }

    @Test
    void testFindMpaById_G() {
        Optional<Mpa> mpa = mpaDao.findById(1);

        assertThat(mpa)
                .isPresent()
                .hasValue(Mpa.G);
    }

    @Test
    void testFindMpaById_PG() {
        Optional<Mpa> mpa = mpaDao.findById(2);

        assertThat(mpa)
                .isPresent()
                .hasValue(Mpa.PG);
    }

    @Test
    void testFindMpaById_PG13() {
        Optional<Mpa> mpa = mpaDao.findById(3);

        assertThat(mpa)
                .isPresent()
                .hasValue(Mpa.PG_13);
    }

    @Test
    void testFindMpaById_R() {
        Optional<Mpa> mpa = mpaDao.findById(4);

        assertThat(mpa)
                .isPresent()
                .hasValue(Mpa.R);
    }

    @Test
    void testFindMpaById_NC17() {
        Optional<Mpa> mpa = mpaDao.findById(5);

        assertThat(mpa)
                .isPresent()
                .hasValue(Mpa.NC_17);
    }

    @Test
    void testFindMpaById_NotFound() {
        Optional<Mpa> mpa = mpaDao.findById(999);

        assertThat(mpa).isEmpty();
    }

    @Test
    void testMpaProperties() {
        Optional<Mpa> mpaG = mpaDao.findById(1);

        assertThat(mpaG).isPresent();
        assertThat(mpaG.get().getCode()).isEqualTo("G");
        assertThat(mpaG.get().getDescription()).isEqualTo("У фильма нет возрастных ограничений");
    }
}
