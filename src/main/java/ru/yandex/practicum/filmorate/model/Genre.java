package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Модель жанра фильма
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Genre implements Comparable<Genre> {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    public Genre(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        return Objects.equals(id, genre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Genre other) {
        if (other == null) {
            return 1;
        }
        if (this.id == null && other.id == null) {
            return 0;
        }
        if (this.id == null) {
            return -1;
        }
        if (other.id == null) {
            return 1;
        }
        return this.id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}