package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film)  throws ValidationException;

    Film getFilm(Long id) throws ValidationException;

    Collection<Film> getFilms();

    void addLike(Long id, Long userId) throws ValidationException;

    void removeLike(Long id, Long userId) throws ValidationException;

    Collection<Film> getPopularFilms(Integer count);

    Collection<Mpa> getAllMpa();

    Mpa getMpaById(int id);

    Collection<Genre> getAllGenres();

    Genre getGenreById(int id);
}
