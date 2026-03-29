package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private int duration;

    @NotNull(message = "Рейтинг MPA обязателен")
    private Mpa mpa;

    private Set<Genre> genres = new HashSet<>();

    @JsonIgnore
    private Set<Long> likes = new HashSet<>();

    public int getLikesCount() {
        return likes != null ? likes.size() : 0;
    }

    public Film() {
        this.genres = new HashSet<>();
        this.likes = new HashSet<>();
    }
}