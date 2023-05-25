package ru.yandex.practicum.filmorate.storage.film;

import ch.qos.logback.core.pattern.parser.OptionTokenizer;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Optional<Film> addFilm(Film film);

    Optional<Film> updateFilm(Film film)  throws ValidationException;

    Optional<Film> getFilm(Long id) throws ValidationException;

    Collection<Film> getFilms();

    void addLike(Long id, Long userId) throws ValidationException;

    void removeLike(Long id, Long userId) throws ValidationException;

    Collection<Film> getPopularFilms(Integer count);

    Collection<Mpa> getAllMpa();

    Optional<Mpa> getMpaById(int id);

    Collection<Genre> getAllGenres();

    Optional<Genre> getGenreById(int id);
}
