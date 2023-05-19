package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class User {
    private final Long id;
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
    private final Set<Long> friends;

    public void addFriend(Long userId) {
        friends.add(userId);
    }

    public void removeFriend(Long userId) {
       friends.removeIf(p -> p.equals(userId));
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("email", email);
        values.put("login", login);
        values.put("name", name);
        values.put("birthday", birthday);
        return values;
    }
}
