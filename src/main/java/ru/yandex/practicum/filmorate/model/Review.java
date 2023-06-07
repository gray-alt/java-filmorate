package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class Review {
    private final Long id;
    @NotNull
    @NotBlank(message = "Текст обзора не может быть пустым.")
    private final String content;
    private final boolean isPositive;
    @NotNull
    private final Long userId;
    @NotNull
    private final Long filmId;
    private final int useful;

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("content", content);
        values.put("positive", isPositive);
        values.put("user_id", userId);
        values.put("film_id", filmId);
        return values;
    }
}
