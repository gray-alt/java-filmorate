package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

@Component("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        Map<String, Object> userMap = film.toMap();

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        long filmId = simpleJdbcInsert.executeAndReturnKey(userMap).longValue();

        Film newFilm = Film.builder()
                .id(filmId)
                .name(film.getName())
                .description(film.getDescription())
                .duration(film.getDuration())
                .releaseDate(film.getReleaseDate())
                .mpaId(film.getMpaId())
                .build();

        log.info("Добавлен фильм: " + newFilm.getName());
        return newFilm;
    }

    @Override
    public Film updateFilm(Film film) throws ValidationException {
        Film foundFilm = getFilmById(film.getId());

        String sqlQuery =
                "update films set " +
                "name = ?, description = ?, duration = ?, releaseDate = ?, mpa_id = ? " +
                "where film_id = ?";

        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getMpaId(),
                film.getId());

        Film newFilm = Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .duration(film.getDuration())
                .releaseDate(film.getReleaseDate())
                .mpaId(film.getMpaId())
                .build();

        log.info("Обновлен фильм: " + newFilm.getName());
        return newFilm;
    }

    @Override
    public Film getFilm(Long id) throws ValidationException {
        return getFilmById(id);
    }

    @Override
    public Collection<Film> getFilms() {
        String sqlQuery = "select * from films";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
    }

    @Override
    public void addLike(Long id, Long userId) throws ValidationException {
        Film film = getFilmById(id);

        String sqlQuery = "insert into film_likes(film_id, user_id)" +
                "values(?, ?)";

        jdbcTemplate.update(sqlQuery, film.getId(), userId);
        log.info("Фильму с id " + id + " поставил лайк пользователь с id " + userId);
    }

    @Override
    public void removeLike(Long id, Long userId) throws ValidationException {
        Film film = getFilmById(id);
        String sqlQuery = "delete from film_likes where film_id = ? and user_id = ?";
        jdbcTemplate.update(sqlQuery, film.getId(), userId);
        log.info("У фильма с id " + id + " удален лайк пользователя с id " + userId);
    }

    @Override
    public Collection<Film> getPopularFilms(Integer count) {
        String sqlQuery = "select * from films where film_id in (" +
                "select first ? film_id from film_likes group by film_id order by count(user_id)" +
                ")";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, count);
    }

    private Film getFilmById(Long id) throws ValidationException {
        String sqlQuery = "select * from films where film_id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, id);
            return film;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NotFoundException("Фильма с id " + id + " не существует.");
        }
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getLong("user_id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .duration(resultSet.getInt("duration"))
                .releaseDate(resultSet.getDate("releaseDate").toLocalDate())
                .mpaId(resultSet.getInt("mpa_id"))
                .build();
    }
}
