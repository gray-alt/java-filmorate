package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public UserService(UserStorage userStorage) {
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
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);
        user.addFriend(friendId);
        friend.addFriend(userId);
        log.info("Пользователю с id " + userId + " добавлен друг с id " + friendId);
    }

    public void removeFriend(Long userId, Long friendId) throws ValidationException {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);
        user.removeFriend(friendId);
        friend.removeFriend(userId);
        log.info("У пользователю с id " + userId + " удален друг с id " + friendId);
    }

    public Collection<User> getFriends(Long id) throws ValidationException {
        Set<Long> friendsId = userStorage.getUser(id).getFriends();
        return userStorage.getUsersByIds(friendsId);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) throws ValidationException {
        Set<Long> friendsId = new HashSet<>(userStorage.getUser(id).getFriends());
        Set<Long> otherFriendsId = userStorage.getUser(otherId).getFriends();

        friendsId.retainAll(otherFriendsId);
        return userStorage.getUsersByIds(friendsId);
    }
}
