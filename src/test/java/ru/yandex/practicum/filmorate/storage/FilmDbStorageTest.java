package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    private static final String SQL_DATA_FILE = "src" + File.separator + "main" + File.separator + "resources" +
            File.separator + "data.sql";

    @BeforeEach
    public void beforeEachTest() throws IOException {
        String sqlQuery = new String(Files.readAllBytes(Paths.get(SQL_DATA_FILE)));
        jdbcTemplate.execute(sqlQuery);
    }

    @Test
    public void testAddFilm() {
        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1, ""));
        genres.add(new Genre(2, ""));

        Film newFilm = Film.builder()
                .name("Film name")
                .description("Film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .genres(genres)
                .duration(60)
                .build();

        Optional<Film> optionalFilm = filmStorage.addFilm(newFilm);

        assertThat(optionalFilm)
                .isPresent();
    }

    @Test
    public void testUpdateFilm() {
        Film newFilm = Film.builder()
                .name("Film name")
                .description("Film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(60)
                .build();

        Optional<Film> optionalFilm = filmStorage.addFilm(newFilm);

        assertThat(optionalFilm)
                .isPresent();

        newFilm = optionalFilm.get();

        assertThat(newFilm.getGenres())
                .size()
                .isEqualTo(0);

        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1, ""));
        genres.add(new Genre(2, ""));

        Film filmForUpdate = Film.builder()
                .id(optionalFilm.get().getId())
                .name(newFilm.getName())
                .description(newFilm.getDescription())
                .releaseDate(newFilm.getReleaseDate())
                .duration(newFilm.getDuration())
                .mpa(newFilm.getMpa())
                .genres(genres)
                .build();

        optionalFilm = filmStorage.updateFilm(filmForUpdate);

        assertThat(optionalFilm)
                .isPresent();

        newFilm = optionalFilm.get();

        assertThat(newFilm.getGenres())
                .size()
                .isEqualTo(2);
    }

    @Test
    public void testUpdateFilmWithWrongId() {
        Film newFilm = Film.builder()
                .id(555L)
                .name("Film name")
                .description("Film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(60)
                .build();

        Optional<Film> optionalFilm = filmStorage.updateFilm(newFilm);

        assertThat(optionalFilm)
                .isEmpty();
    }

    @Test
    public void testGetFilm() {
        Film newFilm = Film.builder()
                .name("Film name")
                .description("Film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(60)
                .build();

        Optional<Film> optionalFilm = filmStorage.addFilm(newFilm);

        assertThat(optionalFilm)
                .isPresent();

        Optional<Film> optionalGetFilm = filmStorage.getFilm(optionalFilm.get().getId());

        assertThat(optionalGetFilm)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", optionalFilm.get().getId())
                );
    }

    @Test
    public void testGetFilmWithWrongId() {
        Optional<Film> optionalFilm = filmStorage.getFilm(555L);

        assertThat(optionalFilm)
                .isEmpty();
    }

    @Test
    public void testGetFilms() {
        Film newFilm = Film.builder()
                .name("Film name")
                .description("Film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(60)
                .build();

        Optional<Film> optionalFilm = filmStorage.addFilm(newFilm);

        assertThat(optionalFilm)
                .isPresent();

        Collection<Film> films = filmStorage.getFilms();
        assertThat(films)
                .isNotEmpty();
    }

    @Test
    public void testAddLike() {
        Film newFilm = Film.builder()
                .name("Film name")
                .description("Film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(60)
                .build();

        Optional<Film> optionalFilm = filmStorage.addFilm(newFilm);

        assertThat(optionalFilm)
                .isPresent();

        newFilm = optionalFilm.get();

        assertThat(newFilm.getLikes())
                .isNullOrEmpty();

        User newUser = User.builder()
                .login("NewUser")
                .email("user email")
                .name("New user")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> optionalUser = userStorage.addUser(newUser);

        assertThat(optionalUser)
                .isPresent();

        filmStorage.addLike(newFilm.getId(), optionalUser.get().getId());

        optionalFilm = filmStorage.getFilm(newFilm.getId());

        assertThat(optionalFilm)
                .isPresent();

        newFilm = optionalFilm.get();

        assertThat(newFilm.getLikes())
                .size()
                .isEqualTo(1);

        filmStorage.removeLike(newFilm.getId(), optionalUser.get().getId());
    }

    @Test
    public void testRemoveLike() {
        Film newFilm = Film.builder()
                .name("Film name")
                .description("Film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(60)
                .build();

        Optional<Film> optionalFilm = filmStorage.addFilm(newFilm);

        assertThat(optionalFilm)
                .isPresent();

        User newUser = User.builder()
                .login("NewUser")
                .email("user email")
                .name("New user")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> optionalUser = userStorage.addUser(newUser);

        assertThat(optionalUser)
                .isPresent();

        filmStorage.addLike(optionalFilm.get().getId(), optionalUser.get().getId());

        optionalFilm = filmStorage.getFilm(optionalFilm.get().getId());

        assertThat(optionalFilm)
                .isPresent();

        assertThat(optionalFilm.get().getLikes())
                .size()
                .isEqualTo(1);

        filmStorage.removeLike(optionalFilm.get().getId(), optionalUser.get().getId());

        optionalFilm = filmStorage.getFilm(optionalFilm.get().getId());

        assertThat(optionalFilm)
                .isPresent();

        assertThat(optionalFilm.get().getLikes())
                .isNullOrEmpty();
    }

    @Test
    public void testGetPopularFilms() {
        Film newFilm1 = Film.builder()
                .name("Film name")
                .description("Film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(60)
                .build();

        Optional<Film> optionalFilm1 = filmStorage.addFilm(newFilm1);

        assertThat(optionalFilm1)
                .isPresent();

        Film newFilm2 = Film.builder()
                .name("Film name")
                .description("Film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(60)
                .build();

        Optional<Film> optionalFilm2 = filmStorage.addFilm(newFilm2);

        assertThat(optionalFilm2)
                .isPresent();

        User newUser1 = User.builder()
                .login("NewUser")
                .email("user email")
                .name("New user")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> userOptional1 = userStorage.addUser(newUser1);

        assertThat(userOptional1)
                .isPresent();

        User newUser2 = User.builder()
                .login("NewUser")
                .email("user email")
                .name("New user")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Optional<User> userOptional2 = userStorage.addUser(newUser2);

        assertThat(userOptional2)
                .isPresent();

        filmStorage.addLike(optionalFilm1.get().getId(), userOptional1.get().getId());
        filmStorage.addLike(optionalFilm1.get().getId(), userOptional2.get().getId());
        filmStorage.addLike(optionalFilm2.get().getId(), userOptional1.get().getId());

        Collection<Film> films = filmStorage.getPopularFilms(2);
        List<Film> listFilms = (List<Film>)films;

        assertThat(films)
                .isNotEmpty()
                .size()
                .isEqualTo(2);

        assertThat(listFilms.get(0))
                .hasFieldOrPropertyWithValue("id", optionalFilm1.get().getId());
        assertThat(listFilms.get(1))
                .hasFieldOrPropertyWithValue("id", optionalFilm2.get().getId());
    }

    @Test
    public void testGetAllMpa() {
        Collection<Mpa> mpa = filmStorage.getAllMpa();
        assertThat(mpa)
                .size()
                .isEqualTo(5);
    }

    @Test
    public void testGetMpaById() {
        Optional<Mpa> mpa = filmStorage.getMpaById(1);
        assertThat(mpa)
                .isPresent()
                .hasValueSatisfying(mpa1 ->
                        assertThat(mpa1).hasFieldOrPropertyWithValue("name", "G")
                );
    }

    @Test
    public void testGetMpaByWrongId() {
        Optional<Mpa> mpa = filmStorage.getMpaById(555);
        assertThat(mpa)
                .isEmpty();
    }

    @Test
    public void testGetAllGenres() {
        Collection<Genre> genres = filmStorage.getAllGenres();
        assertThat(genres)
                .size()
                .isEqualTo(6);
    }

    @Test
    public void testGetGenreById() {
        Optional<Genre> genre = filmStorage.getGenreById(1);
        assertThat(genre)
                .isPresent()
                .hasValueSatisfying(genre1 ->
                        assertThat(genre1).hasFieldOrPropertyWithValue("name", "Комедия")
                );
    }

    @Test
    public void testGetGenreByWrongId() {
        Optional<Genre> genre = filmStorage.getGenreById(555);
        assertThat(genre)
                .isEmpty();
    }
}
