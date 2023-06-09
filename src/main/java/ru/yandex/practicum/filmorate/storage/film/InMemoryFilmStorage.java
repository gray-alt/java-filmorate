package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component("inMemoryFilmStorage")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    @Autowired
    InMemoryUserStorage inMemoryUserStorage;
    private final Map<Long, Film> films = new ConcurrentHashMap<>();
    private final Map<Long, Director> directors = new ConcurrentHashMap<>();
    private long lastId = 0;
    private long directorId = 0;

    @Override
    public Optional<Film> addFilm(Film film) {
        Film newFilm = Film.builder()
                .id(++lastId)
                .name(film.getName())
                .description(film.getDescription())
                .duration(film.getDuration())
                .releaseDate(film.getReleaseDate())
                .likes(new HashSet<>())
                .build();

        log.info("Добавлен фильм: " + newFilm.getName());
        films.put(newFilm.getId(), newFilm);
        return Optional.of(newFilm);
    }

    @Override
    public Optional<Film> updateFilm(Film film) throws ValidationException {
        Optional<Film> foundFilm = getFilmById(film.getId());
        Film newFilm = Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .duration(film.getDuration())
                .releaseDate(film.getReleaseDate())
                .likes(foundFilm.get().getLikes())
                .build();
        log.info("Обновлен фильм: " + newFilm.getName());
        films.put(newFilm.getId(), newFilm);
        return Optional.of(newFilm);
    }

    @Override
    public Optional<Film> getFilm(Long id) throws ValidationException {
        return getFilmById(id);
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public boolean filmExist(Long id) {
        return films.containsKey(id);
    }

    @Override
    public boolean filmNotExist(Long id) {
        return !films.containsKey(id);
    }

    @Override
    public void addLike(Long id, Long userId) throws ValidationException {
        Optional<Film> film = getFilmById(id);
        film.get().addLike(userId);
        inMemoryUserStorage.addEvent(userId, EventType.LIKE, Operation.ADD, id);
        log.info("Фильму с id " + id + " поставил лайк пользователь с id " + userId);
    }

    @Override
    public void removeLike(Long id, Long userId) throws ValidationException {
        Optional<Film> film = getFilmById(id);
        film.get().removeLike(userId);
        inMemoryUserStorage.addEvent(userId, EventType.LIKE, Operation.REMOVE, id);
        log.info("У фильма с id " + id + " удален лайк пользователя с id " + userId);
    }

    @Override
    public Collection<Film> getPopularFilms(Integer count) {
        return films.values().stream()
                .sorted((f0, f1) -> f1.getLikes().size() - f0.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Mpa> getAllMpa() {
        return null;
    }

    @Override
    public Optional<Mpa> getMpaById(int id) {
        return Optional.empty();
    }

    @Override
    public Collection<Genre> getAllGenres() {
        return null;
    }

    @Override
    public Optional<Genre> getGenreById(int id) {
        return Optional.empty();
    }

    @Override
    public void deleteFilmById(Long id) {

    }


    private Optional<Film> getFilmById(Long id) throws ValidationException {
        if (id == null) {
            throw new ValidationException("Не передан id фильма.");
        } else if (!films.containsKey(id)) {
            throw new NotFoundException("Фильма с id " + id + " не существует.");
        }

        return Optional.of(films.get(id));
    }

    @Override
    public Optional<Director> addDirector(Director director) {
        Director newDirector = Director.builder().id(++directorId).name(director.getName()).build();
        directors.put(newDirector.getId(), newDirector);
        return Optional.of(newDirector);
    }

    @Override
    public Optional<Director> updateDirector(Director director) {
        directors.replace(director.getId(), director);
        return Optional.of(director);
    }

    @Override
    public boolean directorExist(Long id) {
        return directors.containsKey(id);
    }

    @Override
    public boolean directorNotExist(Long id) {
        return !directors.containsKey(id);
    }

    @Override
    public Collection<Director> getAllDirectors() {
        return directors.values();
    }

    @Override
    public Optional<Director> getDirector(Long id) {
        return Optional.of(directors.get(id));
    }

    @Override
    public Collection<Film> getDirectorFilms(Long directorId, String sort) {
        if (sort.equals("year")) {
            return films.values().stream()
                    .sorted((f0, f1) -> f1.getReleaseDate().getYear() - f0.getReleaseDate().getYear())
                    .collect(Collectors.toList());
        } else {
            return films.values().stream()
                    .sorted((f0, f1) -> f1.getLikes().size() - f0.getLikes().size())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void removeDirector(Long id) {
        directors.remove(id);
        log.info("Режиссёр с id " + id + " удалён.");
    }
}
