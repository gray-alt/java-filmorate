package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Year;
import java.util.ArrayList;
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
        if (filmStorage.filmNotExist(film.getId())) {
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
        if (filmStorage.filmNotExist(id)) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
        if (userStorage.userNotExist(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        filmStorage.addLike(id, userId);
    }

    public void removeLike(Long id, Long userId) {
        if (filmStorage.filmNotExist(id)) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
        if (userStorage.userNotExist(userId)) {
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

    public Optional<Director> addDirector(Director director) {
        return filmStorage.addDirector(director);
    }

    public Optional<Director> updateDirector(Director director) {
        Optional<Director> directorForUpdate = filmStorage.getDirector(director.getId());
        if (directorForUpdate.isEmpty()) {
            throw new NotFoundException("Режиссёра с id " + director.getId() + " не существует.");
        }
        return filmStorage.updateDirector(director);
    }

    public Collection<Director> getAllDirectors() {
        return filmStorage.getAllDirectors();
    }

    public Optional<Director> getDirector(Long id) {
        Optional<Director> director = filmStorage.getDirector(id);
        if (director.isEmpty()) {
            throw new NotFoundException("Режиссёра с id " + id + " не существует.");
        }
        return director;
    }

    public void removeDirector(Long id) {
        Optional<Director> director = filmStorage.getDirector(id);
        if (director.isEmpty()) {
            throw new NotFoundException("Режиссёра с id " + id + " не существует.");
        }
        filmStorage.removeDirector(id);
    }

    public Collection<Film> getDirectorFilms(Long directorId, String sort) {
        if (filmStorage.directorNotExist(directorId)) {
            throw new NotFoundException("Фильм с id " + directorId + " не найден.");
        }
        if (!(sort.equals("year") || sort.equals("likes"))) {
            throw new ValidationException("Не верно введённый параметр сортировки : " + sort);
        }
        return filmStorage.getDirectorFilms(directorId, sort);
    }

    public void deleteFilmById(Long id) {
        if (filmStorage.filmNotExist(id)) {
            throw new NotFoundException("Нет фильма с таким id");
        }
        filmStorage.deleteFilmById(id);
    }

    // Получение топ N фильмов по количеству лайков. В случае если genreId и/или year > 0 применяем фильтрацию по ним.
    public Collection<Film> getTopByLikes(Integer count, Integer genreId, Integer year) {
        Collection<Film> topFilms = new ArrayList<Film>();
        if (genreId > 0 || year > 0) {
            topFilms = getTopFilmsByFilter(count, genreId, year);
        } else if (genreId == 0 && year == 0) {
            topFilms = filmStorage.getPopularFilms(count);
        }
        return topFilms;
    }

    // Получение фильмов с учетом жанра и/или года
    private Collection<Film> getTopFilmsByFilter(Integer count, Integer genreId, Integer year) {
        Collection<Film> topFilms = new ArrayList<Film>();
        // Если год задан, получаем список с учетом года. Иначе - только по жанрам.
        if (year > 0) {
            // Если год раньше 1895 года или позже текущего, выдается сообщение об ошибке
            if (year < 1895 || year > Year.now().getValue()) {
                throw new ValidationException("Указан некорректный год.");
            }
            // Если задан жанр, получаем список с учетом жанра. Иначе - только по году.
            if (genreId > 0) {
                topFilms = filmStorage.getTopFilmsByGenreAndYear(genreId, year, count);
            } else {
                topFilms = filmStorage.getTopFilmsByYear(count, year);
            }
        } else {
            topFilms = filmStorage.getTopFilmsByGenre(genreId, count);
        }
        return topFilms;
    }


}
