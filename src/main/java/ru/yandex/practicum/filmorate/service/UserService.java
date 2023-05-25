package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Optional<User> addUser(User user) {
        return userStorage.addUser(user);
    }

    public Optional<User> updateUser(User user) {
        Optional<User> foundUser = userStorage.getUser(user.getId());
        if (foundUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден.");
        }
        return userStorage.updateUser(user);
    }

    public Optional<User> getUser(Long id) {
        Optional<User> foundUser = userStorage.getUser(id);
        if (foundUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + id + " не найден.");
        }
        return foundUser;
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public void addFriend(Long userId, Long friendId) {
        Optional<User> foundUser = userStorage.getUser(userId);
        if (foundUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        Optional<User> foundFriend = userStorage.getUser(friendId);
        if (foundFriend.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + friendId + " не найден.");
        }

        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        Optional<User> foundUser = userStorage.getUser(userId);
        if (foundUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        Optional<User> foundFriend = userStorage.getUser(friendId);
        if (foundFriend.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + friendId + " не найден.");
        }
        userStorage.removeFriend(userId, friendId);
    }

    public void confirmFriend(Long userId, Long friendId) {
        Optional<User> foundUser = userStorage.getUser(userId);
        if (foundUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        Optional<User> foundFriend = userStorage.getUser(friendId);
        if (foundFriend.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + friendId + " не найден.");
        }
        userStorage.confirmFriend(userId, friendId);
    }

    public Collection<User> getFriends(Long id) {
        return userStorage.getFriends(id);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }
}
