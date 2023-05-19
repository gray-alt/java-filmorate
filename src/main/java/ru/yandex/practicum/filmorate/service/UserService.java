package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) throws ValidationException {
        return userStorage.updateUser(user);
    }

    public User getUser(Long id) throws ValidationException {
        return userStorage.getUser(id);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public void addFriend(Long userId, Long friendId) throws ValidationException {
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) throws ValidationException {
        userStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getFriends(Long id) throws ValidationException {
        return userStorage.getFriends(id);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) throws ValidationException {
        return userStorage.getCommonFriends(id, otherId);
    }
}
