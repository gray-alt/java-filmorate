package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.DateEqualsOrAfter;

import javax.validation.constraints.*;

@Data
@Builder
public class Film {
    private final Long id;
    @NotNull
    @NotBlank(message = "Название не может быть пустым.")
    private final String name;
    @Size(max = 200, message = "Максимальная длина описания 200 символов.")
    private final String description;
    @NotNull
    @DateEqualsOrAfter(value = "1895-12-28", message = "Дата релиза должна быть не раньше 28 декабря 1895 года")
    private final LocalDate releaseDate;
    @NotNull
    @Positive(message = "Продолжительность фильма должна быть положительной.")
    private final int duration;
    private final Mpa mpa;
    private final Set<Long> likes;
    private final Set<Genre> genres;

    public void addLike(Long userId) {
        likes.add(userId);
    }

    public void removeLike(Long userId) {
        likes.removeIf(p -> p.equals(userId));
    }

    public Integer getMpaId() {
        return (mpa == null ? null : mpa.getId());
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", name);
        values.put("description", description);
        values.put("release_date", releaseDate);
        values.put("duration", duration);
        values.put("mpa_id", getMpaId());
        return values;
    }
}
