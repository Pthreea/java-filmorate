package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public enum Genre {
    COMEDY(1, "Комедия"),
    DRAMA(2, "Драма"),
    CARTOON(3, "Мультфильм"),
    THRILLER(4, "Триллер"),
    DOCUMENTARY(5, "Документальный"),
    ACTION(6, "Боевик");

    private final int id;
    private final String name;

    Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @JsonCreator
    public static Genre fromJson(@JsonProperty("id") Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Genre id не может быть null");
        }
        for (Genre genre : values()) {
            if (genre.id == id) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Неизвестный Genre id: " + id);
    }

    public static Genre fromName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Genre name не может быть null");
        }
        for (Genre genre : values()) {
            if (genre.name.equalsIgnoreCase(name)) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Неизвестный жанр: " + name);
    }

    public static Genre fromId(int id) {
        for (Genre genre : values()) {
            if (genre.id == id) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Неизвестный Genre id: " + id);
    }

    @Override
    public String toString() {
        return name;
    }
}