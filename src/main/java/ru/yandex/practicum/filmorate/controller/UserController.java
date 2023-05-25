package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;

@RestController()
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public Optional<User> addUser(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping
    public Optional<User> updateUser(@RequestBody User user) throws ValidationException {
        return userService.updateUser(user);
    }

    @GetMapping
    public Collection<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable Long id) throws ValidationException {
        return userService.getUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) throws ValidationException {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) throws ValidationException {
        userService.removeFriend(id, friendId);
    }

    @PutMapping("/{id}/friends/confirm/{friendId}")
    public void confirmFriend(@PathVariable Long id, @PathVariable Long friendId) throws ValidationException {
        userService.confirmFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable Long id) throws ValidationException {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Long id,
                                             @PathVariable Long otherId) throws ValidationException {
        return userService.getCommonFriends(id, otherId);
    }
}
