package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private int lastId = 0;

    @PostMapping("/users")
    public User addUser(@Valid @RequestBody User.SimpleUser user) {
        String userName = user.getName();
        if (!StringUtils.hasText(userName)) {
            userName = user.getLogin();
        }

        User newUser = User.builder()
                .id(++lastId)
                .login(user.getLogin())
                .email(user.getEmail())
                .name(userName)
                .birthday(user.getBirthday())
                .build();

        log.info("Добавлен пользователь: " + user.getLogin());
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @PutMapping("/users")
    public User updateUser(@RequestBody User user) throws ValidationException {
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("Пользователя с id " + user.getId() + " не существует.");
        }

        log.info("Обновлен пользователь: " + user.getName());
        users.put(user.getId(), user);
        return user;
    }

    @GetMapping("/users")
    public Collection<User> getUsers() {
        return users.values();
    }
}
