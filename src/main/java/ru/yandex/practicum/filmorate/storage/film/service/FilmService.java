package ru.yandex.practicum.filmorate.storage.film.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Optional<Film> addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Optional<Film> updateFilm(Film film) {
        Optional<Film> foundFilm = filmStorage.getFilm(film.getId());
        if (foundFilm.isEmpty()) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден.");
        }
        return filmStorage.updateFilm(film);
    }

    public Optional<Film> getFilm(Long id) {
        Optional<Film> foundFilm = filmStorage.getFilm(id);
        if (foundFilm.isEmpty()) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
        return foundFilm;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public void addLike(Long id, Long userId) {
        Optional<Film> foundFilm = filmStorage.getFilm(id);
        if (foundFilm.isEmpty()) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
        Optional<User> foundUser = userStorage.getUser(userId);
        if (foundUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        filmStorage.addLike(id, userId);
    }

    public void removeLike(Long id, Long userId) {
        Optional<Film> foundFilm = filmStorage.getFilm(id);
        if (foundFilm.isEmpty()) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
        Optional<User> foundUser = userStorage.getUser(userId);
        if (foundUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        filmStorage.removeLike(id, userId);
    }

    public Collection<Film> getPopularFilms(Integer count) {
        return filmStorage.getPopularFilms(count);
    }

    public Collection<Mpa> getAllMpa() {
        return filmStorage.getAllMpa();
    }

    public Optional<Mpa> getMpaById(int id) {
        Optional<Mpa> mpa = filmStorage.getMpaById(id);
        if (mpa.isEmpty()) {
            throw new NotFoundException("Рейтинга с id " + id + " не существует.");
        }
        return mpa;
    }

    public Collection<Genre> getAllGenres() {
        return filmStorage.getAllGenres();
    }

    public Optional<Genre> getGenreById(int id) {
        Optional<Genre> genre = filmStorage.getGenreById(id);
        if (genre.isEmpty()) {
            throw new NotFoundException("Жанра с id " + id + " не существует.");
        }
        return genre;
    }
}
