package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component("inMemoryFilmStorage")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new ConcurrentHashMap<>();
    private long lastId = 0;

    @Override
    public Film addFilm(Film film) {
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
        return newFilm;
    }

    @Override
    public Film updateFilm(Film film) throws ValidationException {
        Film foundFilm = getFilmById(film.getId());
        Film newFilm = Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .duration(film.getDuration())
                .releaseDate(film.getReleaseDate())
                .likes(foundFilm.getLikes())
                .build();
        log.info("Обновлен фильм: " + newFilm.getName());
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @Override
    public Film getFilm(Long id) throws ValidationException {
        return getFilmById(id);
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public void addLike(Long id, Long userId) throws ValidationException {
        Film film = getFilmById(id);
        film.addLike(userId);
        log.info("Фильму с id " + id + " поставил лайк пользователь с id " + userId);
    }

    @Override
    public void removeLike(Long id, Long userId) throws ValidationException {
        Film film = getFilmById(id);
        film.removeLike(userId);
        log.info("У фильма с id " + id + " удален лайк пользователя с id " + userId);
    }

    @Override
    public Collection<Film> getPopularFilms(Integer count) {
        return films.values().stream()
                .sorted((f0, f1) -> f1.getLikes().size() - f0.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    private Film getFilmById(Long id) throws ValidationException {
        if (id == null) {
            throw new ValidationException("Не передан id фильма.");
        } else if (!films.containsKey(id)) {
            throw new NotFoundException("Фильма с id " + id + " не существует.");
        }

        return films.get(id);
    }
}
