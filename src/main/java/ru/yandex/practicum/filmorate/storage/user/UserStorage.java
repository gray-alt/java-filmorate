package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Set;

public interface UserStorage {
    User addUser(User user);
    User updateUser(User user) throws ValidationException;
    User getUser(Long id) throws ValidationException;
    Collection<User> getUsers();
    Collection<User> getUsersByIds(Set<Long> usersId);
}
