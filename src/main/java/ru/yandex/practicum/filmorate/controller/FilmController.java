package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController()
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Optional<Film> addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Optional<Film> updateFilm(@Valid @RequestBody Film film) throws ValidationException {
        return filmService.updateFilm(film);
    }

    @GetMapping("/{id}")
    public Optional<Film> getFilm(@PathVariable Long id) throws ValidationException {
        return filmService.getFilm(id);
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) throws ValidationException {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) throws ValidationException {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") @Positive Integer count) {
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilms(@RequestParam String query,
                                        @RequestParam List<String> by ) {
        return filmService.searchFilms(query, by);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getDirectorFilms(@PathVariable Long directorId,
                                             @RequestParam(value = "sortBy") String sort) {
        return filmService.getDirectorFilms(directorId, sort);
    }
}
