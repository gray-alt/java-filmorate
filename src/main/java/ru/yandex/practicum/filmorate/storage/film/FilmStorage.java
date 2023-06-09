package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Optional<Film> addFilm(Film film);

    Optional<Film> updateFilm(Film film) throws ValidationException;

    Optional<Film> getFilm(Long id) throws ValidationException;

    Collection<Film> getFilms();

    boolean filmExist(Long id);

    boolean filmNotExist(Long id);

    void addLike(Long id, Long userId) throws ValidationException;

    void removeLike(Long id, Long userId) throws ValidationException;

    Collection<Film> getPopularFilms(Integer count);

    Collection<Mpa> getAllMpa();

    Optional<Mpa> getMpaById(int id);

    Collection<Genre> getAllGenres();

    Optional<Genre> getGenreById(int id);

    Optional<Director> addDirector(Director director);

    Optional<Director> updateDirector(Director director);

    boolean directorExist(Long id);

    boolean directorNotExist(Long id);

    Collection<Director> getAllDirectors();

    Optional<Director> getDirector(Long id);

    Collection<Film> getDirectorFilms(Long directorId, String sort);

    void removeDirector(Long id);

    void deleteFilmById(Long id);

    Collection<Film> getFilmsRecommendation(long userId);
}
