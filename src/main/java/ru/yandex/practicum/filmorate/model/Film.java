package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.DateEqualsOrAfter;

import javax.validation.constraints.*;

@Data
@Builder
public class Film {
    @NotNull(message = "Id фильма должен быть заполнен.")
    private final int id;
    @NotNull
    @NotBlank(message = "Название не может быть пустым.")
    private final String name;
    @Size(max = 200, message = "Максимальная длина описания 200 символов.")
    private final String description;
    @NotNull
    @DateEqualsOrAfter(value = "1985-12-28", message = "Дата релиза должна быть не раньше 28 декабря 1985 года")
    private final LocalDate releaseDate;
    @NotNull
    @Positive(message = "Продолжительность фильма должна быть положительной.")
    private final int duration;

    @Data
    @Builder
    public static class SimpleFilm {
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
    }
}
