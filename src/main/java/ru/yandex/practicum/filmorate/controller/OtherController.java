package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.Optional;

@RestController
@Slf4j
public class OtherController {
    private final FilmService filmService;

    @Autowired
    public OtherController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/mpa")
    public Collection<Mpa> getAllMpa() {
        return filmService.getAllMpa();
    }

    @GetMapping("/mpa/{id}")
    public Optional<Mpa> getMpa(@PathVariable int id) {
        return filmService.getMpaById(id);
    }

    @GetMapping("/genres")
    public Collection<Genre> getAllGenres() {
        return filmService.getAllGenres();
    }

    @GetMapping("/genres/{id}")
    public Optional<Genre> getGenre(@PathVariable int id) {
        return filmService.getGenreById(id);
    }
}
