package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
public class User {
    @NotNull(message = "Id пользователя должен быть заполнен.")
    private final int id;
    @NotNull
    @Email(message = "Неверно указан адрес электронной почты.")
    private final String email;
    @NotNull
    @NotBlank(message = "Логин не может быть пустым.")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы.")
    private final String login;
    private final String name;
    @NotNull
    @PastOrPresent(message = "Дата рождения не может быть в будущем.")
    private final LocalDate birthday;

    @Data
    @Builder
    public static class SimpleUser {
        @NotNull
        @Email(message = "Неверно указан адрес электронной почты.")
        private final String email;
        @NotNull
        @NotBlank(message = "Логин не может быть пустым.")
        @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы.")
        private final String login;
        private final String name;
        @NotNull
        @PastOrPresent(message = "Дата рождения не может быть в будущем.")
        private final LocalDate birthday;
    }
}
