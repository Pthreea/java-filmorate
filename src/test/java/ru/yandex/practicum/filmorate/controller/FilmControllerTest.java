package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createFilm_Valid_ReturnsCreatedFilm() throws Exception {
        // Given
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(Mpa.G);

        String filmJson = objectMapper.writeValueAsString(film);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Film"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.duration").value(120));
    }

    @Test
    void createFilm_InvalidName_ReturnsBadRequest() throws Exception {
        // Given
        Film film = new Film();
        film.setName(""); // Пустое название
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(Mpa.G);

        String filmJson = objectMapper.writeValueAsString(film);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_NoMpa_ReturnsBadRequest() throws Exception {
        // Given
        Film film = new Film();
        film.setName("Film without MPA");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        // Не устанавливаем MPA

        String filmJson = objectMapper.writeValueAsString(film);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_DescriptionTooLong_ReturnsBadRequest() throws Exception {
        // Given
        String longDescription = "a".repeat(201); // 201 символ

        Film film = new Film();
        film.setName("Film");
        film.setDescription(longDescription);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(Mpa.G);

        String filmJson = objectMapper.writeValueAsString(film);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_NegativeDuration_ReturnsBadRequest() throws Exception {
        // Given
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(-10);
        film.setMpa(Mpa.G);

        String filmJson = objectMapper.writeValueAsString(film);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFilm_Valid_ReturnsUpdatedFilm() throws Exception {
        // Given - сначала создаем фильм
        Film film = new Film();
        film.setName("Original Film");
        film.setDescription("Original Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(Mpa.G);

        String createJson = objectMapper.writeValueAsString(film);

        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(response, Film.class);

        // Обновляем фильм
        createdFilm.setName("Updated Film");
        createdFilm.setDescription("Updated Description");
        String updateJson = objectMapper.writeValueAsString(createdFilm);

        // When & Then
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Film"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    void getAllFilms_ReturnsListOfFilms() throws Exception {
        // Given - создаем несколько фильмов
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(90);
        film1.setMpa(Mpa.G);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2021, 2, 2));
        film2.setDuration(100);
        film2.setMpa(Mpa.PG);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film2)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Film 1"))
                .andExpect(jsonPath("$[1].name").value("Film 2"));
    }

    @Test
    void getFilmById_ValidId_ReturnsFilm() throws Exception {
        // Given - создаем фильм
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(Mpa.G);

        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(response, Film.class);

        // When & Then
        mockMvc.perform(get("/films/" + createdFilm.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdFilm.getId()))
                .andExpect(jsonPath("$.name").value("Test Film"));
    }

    @Test
    void addLike_ValidIds_ReturnsNoContent() throws Exception {
        // Given - создаем фильм и пользователя
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(Mpa.G);

        String filmResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(filmResponse, Film.class);

        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = objectMapper.readValue(userResponse, User.class);

        // When & Then
        mockMvc.perform(put("/films/" + createdFilm.getId() + "/like/" + createdUser.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeLike_ValidIds_ReturnsNoContent() throws Exception {
        // Given - создаем фильм, пользователя и добавляем лайк
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(Mpa.G);

        String filmResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(filmResponse, Film.class);

        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = objectMapper.readValue(userResponse, User.class);

        // Добавляем лайк
        mockMvc.perform(put("/films/" + createdFilm.getId() + "/like/" + createdUser.getId()))
                .andExpect(status().isNoContent());

        // When & Then - удаляем лайк
        mockMvc.perform(delete("/films/" + createdFilm.getId() + "/like/" + createdUser.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getPopularFilms_ReturnsTopFilms() throws Exception {
        // Given - создаем несколько фильмов
        Film film1 = new Film();
        film1.setName("Least Popular");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(90);
        film1.setMpa(Mpa.G);

        Film film2 = new Film();
        film2.setName("Most Popular");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2021, 2, 2));
        film2.setDuration(100);
        film2.setMpa(Mpa.PG);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film2)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/films/popular?count=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}