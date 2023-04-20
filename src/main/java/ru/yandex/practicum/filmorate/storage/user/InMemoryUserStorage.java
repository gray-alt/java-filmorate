package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private long lastId = 0;

    @Override
    public User addUser(User user) {
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
                .friends(new HashSet<>())
                .build();

        log.info("Добавлен пользователь: " + user.getLogin());
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public User updateUser(User user) throws ValidationException {
        User foundUser = getUserById(user.getId());
        User newUser = User.builder()
                .id(user.getId())
                .login(user.getLogin())
                .email(user.getEmail())
                .name(user.getName())
                .birthday(user.getBirthday())
                .friends(foundUser.getFriends())
                .build();
        log.info("Обновлен пользователь: " + newUser.getName());
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public User getUser(Long id) throws ValidationException {
        return getUserById(id);
    }

    private User getUserById(Long id) throws ValidationException {
        if (id == null) {
            throw new ValidationException("Не передан id пользователя.");
        } else if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователя с id " + id + " не существует.");
        }

        return users.get(id);
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    public Collection<User> getUsersByIds(Set<Long> usersId) {
        Collection<User> userList = new ArrayList<>();
        usersId.forEach(id -> userList.add(users.get(id)));
        return userList;
    }
}
