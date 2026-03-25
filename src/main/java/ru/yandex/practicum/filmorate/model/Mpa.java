package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public enum Mpa {
    G(1, "G", "У фильма нет возрастных ограничений"),
    PG(2, "PG", "Детям рекомендуется смотреть фильм с родителями"),
    PG_13(3, "PG-13", "Детям до 13 лет просмотр не желателен"),
    R(4, "R", "Лицам до 17 лет просматривать фильм можно только в присутствии взрослого"),
    NC_17(5, "NC-17", "Лицам до 18 лет просмотр запрещён");

    private final int id;
    private final String name;
    private final String description;

    Mpa(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Добавляем метод getCode() который возвращает name
    public String getCode() {
        return name;
    }

    @JsonCreator
    public static Mpa fromJson(@JsonProperty("id") Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("MPA id не может быть null");
        }
        for (Mpa mpa : values()) {
            if (mpa.id == id) {
                return mpa;
            }
        }
        throw new IllegalArgumentException("Неизвестный MPA id: " + id);
    }

    public static Mpa fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("MPA code не может быть null");
        }
        for (Mpa mpa : values()) {
            if (mpa.name.equalsIgnoreCase(code)) {
                return mpa;
            }
        }
        throw new IllegalArgumentException("Неизвестный код MPA: " + code);
    }

    public static Mpa fromId(int id) {
        for (Mpa mpa : values()) {
            if (mpa.id == id) {
                return mpa;
            }
        }
        throw new IllegalArgumentException("Неизвестный MPA id: " + id);
    }

    @Override
    public String toString() {
        return name;
    }
}
