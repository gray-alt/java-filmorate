package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    public void testAddUser() {
        User newUser = User.builder()
                .login("NewUser")
                .email("user email")
                .name("New user")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> userOptional = userStorage.addUser(newUser);

        assertThat(userOptional)
                .isPresent();
    }

    @Test
    public void testAddUserWithoutName() {
        User newUser = User.builder()
                .login("NewUserWithoutName")
                .email("user email")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> userOptional = userStorage.addUser(newUser);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "NewUserWithoutName")
                );
    }

    @Test
    public void testUpdateUser() {
        User newUser = User.builder()
                .login("NewUser")
                .email("user email")
                .name("New user")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> userOptional = userStorage.addUser(newUser);

        assertThat(userOptional)
                .isPresent();

        newUser = userOptional.get();

        User userForUpdate = User.builder()
                .id(newUser.getId())
                .login(newUser.getLogin())
                .name("Updated user name")
                .email(newUser.getEmail())
                .birthday(newUser.getBirthday())
                .build();

        userOptional = userStorage.updateUser(userForUpdate);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "Updated user name")
                );
    }

    @Test
    public void testUpdateUserWithWrongId() {
        User userForUpdate = User.builder()
                .id(555L)
                .login("NewUser")
                .email("user email")
                .name("New user")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> userOptional = userStorage.updateUser(userForUpdate);

        assertThat(userOptional)
                .isEmpty();
    }

    @Test
    public void testGetUser() {
        User newUser = User.builder()
                .login("NewUser")
                .email("user email")
                .name("New user")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> userOptional = userStorage.addUser(newUser);

        assertThat(userOptional)
                .isPresent();

        Optional<User> getUserOptional = userStorage.getUser(userOptional.get().getId());

        assertThat(getUserOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", userOptional.get().getId())
                );
    }

    @Test
    public void testGetUserWithWrongId() {
        Optional<User> getUserOptional = userStorage.getUser(555L);

        assertThat(getUserOptional)
                .isEmpty();
    }

    @Test
    public void testGetUsers() {
        Collection<User> users = userStorage.getUsers();

        User newUser = User.builder()
                .login("NewUser")
                .email("user email")
                .name("New user")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        userStorage.addUser(newUser);

        Collection<User> usersPlusOne = userStorage.getUsers();

        assertThat(usersPlusOne)
                .size()
                .isEqualTo(users.size() + 1);
    }

    @Test
    public void testAddFriend() {
        User user1 = User.builder()
                .login("User1")
                .email("UserMail")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> optionalUser1 = userStorage.addUser(user1);

        assertThat(optionalUser1)
                .isPresent();

        User user2 = User.builder()
                .login("User2")
                .email("UserMail")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> optionalUser2 = userStorage.addUser(user2);

        assertThat(optionalUser2)
                .isPresent();

        userStorage.addFriend(optionalUser1.get().getId(), optionalUser2.get().getId());

        Collection<User> friendsUser1 = userStorage.getFriends(optionalUser1.get().getId());

        assertThat(friendsUser1)
                .size()
                .isEqualTo(1);

        Collection<User> friendsUser2 = userStorage.getFriends(optionalUser2.get().getId());

        assertThat(friendsUser2)
                .size()
                .isEqualTo(0);
    }

    @Test
    public void testConfirmFriend() {
        User user1 = User.builder()
                .login("User1")
                .email("UserMail")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> optionalUser1 = userStorage.addUser(user1);

        assertThat(optionalUser1)
                .isPresent();

        User user2 = User.builder()
                .login("User2")
                .email("UserMail")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> optionalUser2 = userStorage.addUser(user2);

        assertThat(optionalUser2)
                .isPresent();

        userStorage.addFriend(optionalUser1.get().getId(), optionalUser2.get().getId());
        userStorage.confirmFriend(optionalUser1.get().getId(), optionalUser2.get().getId());

        Collection<User> friendsUser1 = userStorage.getFriends(optionalUser1.get().getId());

        assertThat(friendsUser1)
                .size()
                .isEqualTo(1);

        Collection<User> friendsUser2 = userStorage.getFriends(optionalUser2.get().getId());

        assertThat(friendsUser2)
                .size()
                .isEqualTo(1);
    }

    @Test
    public void testRemoveFriend() {
        User user1 = User.builder()
                .login("User1")
                .email("UserMail")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> optionalUser1 = userStorage.addUser(user1);

        assertThat(optionalUser1)
                .isPresent();

        User user2 = User.builder()
                .login("User2")
                .email("UserMail")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> optionalUser2 = userStorage.addUser(user2);

        assertThat(optionalUser2)
                .isPresent();

        userStorage.addFriend(optionalUser1.get().getId(), optionalUser2.get().getId());

        Collection<User> friendsUser1 = userStorage.getFriends(optionalUser1.get().getId());

        assertThat(friendsUser1)
                .size()
                .isEqualTo(1);

        userStorage.removeFriend(optionalUser1.get().getId(), optionalUser2.get().getId());

        friendsUser1 = userStorage.getFriends(optionalUser1.get().getId());

        assertThat(friendsUser1)
                .size()
                .isEqualTo(0);
    }

    @Test
    public void testGetCommonsFriends() {
        User user1 = User.builder()
                .login("User1")
                .email("UserMail")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> optionalUser1 = userStorage.addUser(user1);

        assertThat(optionalUser1)
                .isPresent();

        User user2 = User.builder()
                .login("User2")
                .email("UserMail")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> optionalUser2 = userStorage.addUser(user2);

        assertThat(optionalUser2)
                .isPresent();

        User user3 = User.builder()
                .login("User3")
                .email("UserMail")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> optionalUser3 = userStorage.addUser(user3);

        assertThat(optionalUser3)
                .isPresent();

        userStorage.addFriend(optionalUser1.get().getId(), optionalUser3.get().getId());
        userStorage.addFriend(optionalUser2.get().getId(), optionalUser3.get().getId());

        Collection<User> friends = userStorage.getCommonFriends(optionalUser1.get().getId(),
                optionalUser2.get().getId());

        assertThat(friends)
                .size()
                .isEqualTo(1);
    }
}
