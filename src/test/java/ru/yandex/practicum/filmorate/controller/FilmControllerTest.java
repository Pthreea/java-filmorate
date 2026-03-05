package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    private Film validFilm;

    @BeforeEach
    void setUp() {
        validFilm = new Film();
        validFilm.setId(1L);
        validFilm.setName("Test Film");
        validFilm.setDescription("Test Description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void createFilm_Valid_ReturnsCreatedFilm() throws Exception {
        when(filmService.createFilm(any(Film.class))).thenReturn(validFilm);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Film"));
    }

    @Test
    void createFilm_EmptyName_ReturnsBadRequest() throws Exception {
        validFilm.setName("");

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_DescriptionTooLong_ReturnsBadRequest() throws Exception {
        validFilm.setDescription("a".repeat(201));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_NegativeDuration_ReturnsBadRequest() throws Exception {
        validFilm.setDuration(-1);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFilm_Valid_ReturnsUpdatedFilm() throws Exception {
        when(filmService.updateFilm(any(Film.class))).thenReturn(validFilm);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllFilms_ReturnsListOfFilms() throws Exception {
        List<Film> films = Arrays.asList(validFilm);
        when(filmService.getAllFilms()).thenReturn(films);

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Film"));
    }

    @Test
    void getFilmById_ExistingFilm_ReturnsFilm() throws Exception {
        when(filmService.getFilmById(1L)).thenReturn(validFilm);

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Film"));
    }

    @Test
    void addLike_ValidIds_ReturnsOk() throws Exception {
        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());
    }

    @Test
    void removeLike_ValidIds_ReturnsOk() throws Exception {
        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getPopularFilms_ReturnsPopularFilmsList() throws Exception {
        List<Film> popularFilms = Arrays.asList(validFilm);
        when(filmService.getPopularFilms(anyInt())).thenReturn(popularFilms);

        mockMvc.perform(get("/films/popular?count=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getPopularFilms_DefaultCount_ReturnsPopularFilmsList() throws Exception {
        List<Film> popularFilms = Arrays.asList(validFilm);
        when(filmService.getPopularFilms(10)).thenReturn(popularFilms);

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
