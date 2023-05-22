package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user) throws ValidationException;

    User getUser(Long id) throws ValidationException;

    Collection<User> getUsers();

    void addFriend(Long userId, Long friendId) throws ValidationException;

    void removeFriend(Long userId, Long friendId) throws ValidationException;

    void confirmFriend(Long id, Long friendId) throws ValidationException;

    Collection<User> getFriends(Long id) throws ValidationException;

    Collection<User> getCommonFriends(Long id, Long otherId) throws ValidationException;
}
