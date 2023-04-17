package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
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

    private Film getFilmById(Long id) throws ValidationException {
        if (id == null) {
            throw new ValidationException("Не передан id фильма.");
        } else if (!films.containsKey(id)) {
            throw new NotFoundException("Фильма с id " + id + " не существует.");
        }

        return films.get(id);
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }
}
