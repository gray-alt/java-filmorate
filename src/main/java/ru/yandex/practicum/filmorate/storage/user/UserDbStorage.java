package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("userDbStorage")
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        Map<String, Object> userMap = user.toMap();

        String userName = user.getName();
        if (!StringUtils.hasText(userName)) {
            userName = user.getLogin();
            userMap.put("name", userName);
        }

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        long userId = simpleJdbcInsert.executeAndReturnKey(userMap).longValue();

        User newUser = User.builder()
                .id(userId)
                .login(user.getLogin())
                .email(user.getEmail())
                .name(userName)
                .birthday(user.getBirthday())
                .friends(new HashSet<>())
                .build();

        log.info("Добавлен пользователь: " + newUser.getName());
        return newUser;
    }

    @Override
    public User updateUser(User user) throws ValidationException {
        User foundUser = getUserById(user.getId());

        String sqlQuery =
                "update users set " +
                "login = ?, email = ?, name = ?, birthday = ? " +
                "where user_id = ?";

        jdbcTemplate.update(sqlQuery,
                user.getLogin(),
                user.getEmail(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        User newUser = User.builder()
                .id(user.getId())
                .login(user.getLogin())
                .email(user.getEmail())
                .name(user.getName())
                .birthday(user.getBirthday())
                .friends(foundUser.getFriends())
                .build();

        log.info("Обновлен пользователь: " + newUser.getName());
        return newUser;
    }

    @Override
    public User getUser(Long id) throws ValidationException {
        return getUserById(id);
    }

    @Override
    public Collection<User> getUsers() {
        String sqlQuery = "select * from users";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    @Override
    public void addFriend(Long userId, Long friendId) throws ValidationException {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        String sqlQuery = "insert into friends(user_id, friend_id, status)" +
                "values(?, ?, ?)";

        jdbcTemplate.update(sqlQuery, friend.getId(), user.getId(), 0);
        log.info("Пользователю с id " + friendId + " отправлена заявка в друзья от пользователя с id " + userId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) throws ValidationException {
        String sqlQuery = "delete from friends where user_id = ? and friend_id = ?";
        jdbcTemplate.update(sqlQuery, userId, friendId);
        log.info("У пользователю с id " + userId + " удален друг с id " + friendId);
    }

    @Override
    public Collection<User> getFriends(Long id) throws ValidationException {
        String sqlQuery = "select * from users where user_id in (select friend_id from friends where user_id = ?)";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser, id);
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) throws ValidationException {
        String sqlQuery = "select * from users where user_id in (" +
                    "select friend_id from friends as friends " +
                    "inner join friends as other_friends " +
                    "on friends.friend_id = other_friends.friend_id "  +
                    "where  friends.user_id = ? and other_friends.user_id = ?" +
                ")";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser, id, otherId);
    }

    private User getUserById(Long id) throws ValidationException {
        String sqlQuery = "select * from users where user_id = ?";

        try {
            User user = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, id);
            return user;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NotFoundException("Пользователя с id " + id + " не существует.");
        }
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("user_id"))
                .login(resultSet.getString("login"))
                .email(resultSet.getString("email"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .friends(new HashSet<>())
                .build();
    }
}
