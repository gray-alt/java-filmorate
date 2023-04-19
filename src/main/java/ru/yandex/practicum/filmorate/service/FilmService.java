package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) throws ValidationException {
        return filmStorage.updateFilm(film);
    }

    public Film getFilm(Long id) throws ValidationException {
        return filmStorage.getFilm(id);
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public void addLike(Long id, Long userId) throws ValidationException {
        Film film = filmStorage.getFilm(id);
        User user = userStorage.getUser(userId);
        film.addLike(user.getId());
        log.info("Фильму с id " + id + " поставил лайк пользователь с id " + userId);
    }

    public void removeLike(Long id, Long userId) throws ValidationException {
        Film film = filmStorage.getFilm(id);
        User user = userStorage.getUser(userId);
        film.removeLike(user.getId());
        log.info("У фильма с id " + id + " удален лайк пользователя с id " + userId);
    }

    public Collection<Film> getPopularFilms(Integer count) {
        return filmStorage.getFilms().stream()
                .sorted((f0, f1) -> f1.getLikes().size() - f0.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }
}
