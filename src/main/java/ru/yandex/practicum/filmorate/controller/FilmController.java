package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new ConcurrentHashMap<>();
    private int lastId = 0;

    @PostMapping("/films")
    public Film addFilm(@Valid @RequestBody Film.SimpleFilm film) {
        Film newFilm = Film.builder()
                .id(++lastId)
                .name(film.getName())
                .description(film.getDescription())
                .duration(film.getDuration())
                .releaseDate(film.getReleaseDate())
                .build();

        log.info("Добавлен фильм: " + film.getName());
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @PutMapping("/films")
    public Film updateFilm(@Valid @RequestBody Film film) throws ValidationException {
        if (!films.containsKey(film.getId())) {
            throw new ValidationException("Фильма с id " + film.getId() + " не существует.");
        }

        log.info("Обновлен фильм: " + film.getName());
        films.put(film.getId(), film);
        return film;
    }

    @GetMapping("/films")
    public Collection<Film> getFilms() {
        return films.values();
    }
}
