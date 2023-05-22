package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        Map<String, Object> filmMap = film.toMap();

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        long filmId = simpleJdbcInsert.executeAndReturnKey(filmMap).longValue();

        Set<Genre> genres = film.getGenres();
        if (genres != null) {
            genres.forEach(genre -> addGenreToFilm(filmId, genre.getId()));
        }

        Film newFilm = Film.builder()
                .id(filmId)
                .name(film.getName())
                .description(film.getDescription())
                .duration(film.getDuration())
                .releaseDate(film.getReleaseDate())
                .mpa(film.getMpa())
                .genres(getFilmGenres(filmId))
                .likes(film.getLikes())
                .build();

        log.info("Добавлен фильм: " + newFilm.getName());
        return newFilm;
    }

    @Override
    public Film updateFilm(Film film) throws ValidationException {
        Film foundFilm = getFilmById(film.getId());

        String sqlQuery =
                "update films set " +
                "name = ?, description = ?, duration = ?, release_date = ?, mpa_id = ? " +
                "where film_id = ?";

        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getMpa().getId(),
                film.getId());

        jdbcTemplate.update("delete from film_genres where film_id = ?", film.getId());

        Set<Genre> genres = film.getGenres();
        if (genres != null) {
            genres.forEach(genre -> addGenreToFilm(film.getId(), genre.getId()));
        }

        Film newFilm = Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .duration(film.getDuration())
                .releaseDate(film.getReleaseDate())
                .mpa(film.getMpa())
                .genres(getFilmGenres(film.getId()))
                .likes(film.getLikes())
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
        String sqlQuery =
                "select " +
                "   films.film_id, " +
                "   films.name, " +
                "   films.description, " +
                "   films.release_date, " +
                "   films.duration, " +
                "   films.mpa_id, " +
                "   mpa.name as mpa_name, " +
                "   mpa.description as mpa_description " +
                "from films " +
                "   left join mpa " +
                "   on films.mpa_id = mpa.mpa_id";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
    }

    @Override
    public void addLike(Long id, Long userId) throws ValidationException {
        Film film = getFilmById(id);

        String sqlQuery = "insert into film_likes(film_id, user_id) " +
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
        String sqlQuery =
                "select " +
                "   films.film_id, " +
                "   films.name, " +
                "   films.description, " +
                "   films.release_date, " +
                "   films.duration, " +
                "   films.mpa_id, " +
                "   mpa.name as mpa_name, " +
                "   mpa.description as mpa_description " +
                "from films " +
                "   left join mpa " +
                "   on films.mpa_id = mpa.mpa_id " +
                "where film_id in (" +
                "   select top ? " +
                "       films.film_id " +
                "   from films " +
                "       left join film_likes " +
                "       on films.film_id = film_likes.film_id " +
                "   group by films.film_id " +
                "   order by count(film_likes.user_id) desc" +
                ")";

        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, count);
    }

    @Override
    public Collection<Mpa> getAllMpa() {
        String sqlQuery = "select * from mpa";
        return jdbcTemplate.query(sqlQuery, this::mapRowToMpa);
    }

    @Override
    public Mpa getMpaById(int id) {
        String sqlQuery = "select * from mpa where mpa_id = ?";

        try {
            Mpa mpa = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToMpa, id);
            return mpa;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NotFoundException("Рейтинга с id " + id + " не существует.");
        }
    }

    @Override
    public Collection<Genre> getAllGenres() {
        String sqlQuery = "select * from genres";
        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre);
    }

    @Override
    public Genre getGenreById(int id) {
        String sqlQuery = "select * from genres where genre_id = ?";

        try {
            Genre genre = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToGenre, id);
            return genre;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NotFoundException("Жанра с id " + id + " не существует.");
        }
    }

    private void addGenreToFilm(Long filmId, int genreId) {
        String sqlQuery = "insert into film_genres(film_id, genre_id) values(?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, genreId);
    }

    private Set<Genre> getFilmGenres(Long id) {
        String sqlQuery =
                "select " +
                "   film_genres.genre_id, " +
                "   genres.name " +
                "from film_genres " +
                "   left join genres " +
                "   on film_genres.genre_id = genres.genre_id " +
                "where film_genres.film_id = ? " +
                "order by film_genres.genre_id";

        return new HashSet<>(jdbcTemplate.query(sqlQuery, this::mapRowToGenre, id));
    }

    private Film getFilmById(Long id) throws ValidationException {
        String sqlQuery =
                "select " +
                "   films.film_id, " +
                "   films.name, " +
                "   films.description, " +
                "   films.release_date, " +
                "   films.duration, " +
                "   films.mpa_id, " +
                "   mpa.name as mpa_name, " +
                "   mpa.description as mpa_description " +
                "from films " +
                "   left join mpa " +
                "   on films.mpa_id = mpa.mpa_id " +
                "where film_id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, id);
            return film;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NotFoundException("Фильма с id " + id + " не существует.");
        }
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getLong("film_id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .duration(resultSet.getInt("duration"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .mpa(new Mpa(resultSet.getInt("mpa_id"),
                        resultSet.getString("mpa_name"),
                        resultSet.getString("mpa_description")))
                .genres(getFilmGenres(resultSet.getLong("film_id")))
                .likes(new HashSet<>())
                .build();
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return new Genre(resultSet.getInt("genre_id"), resultSet.getString("name"));
    }

    private Mpa mapRowToMpa(ResultSet resultSet, int rowNum) throws SQLException {
        return new Mpa(resultSet.getInt("mpa_id"), resultSet.getString("name"), resultSet.getString("description"));
    }
}
