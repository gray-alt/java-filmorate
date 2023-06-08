package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Film> addFilm(Film film) {
        Map<String, Object> filmMap = film.toMap();

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        long filmId = simpleJdbcInsert.executeAndReturnKey(filmMap).longValue();

        Set<Genre> genres = film.getGenres();
        if (genres != null) {
            genres = new HashSet<>(genres.stream().sorted(Genre::compareTo).collect(
                    Collectors.toCollection(LinkedHashSet::new)));
            genres.forEach(genre -> addGenreToFilm(filmId, genre.getId()));
        } else {
            genres = new HashSet<>();
        }

        Set<Director> directors = film.getDirectors();
        if (directors != null) {
            directors = new HashSet<>(directors.stream().sorted(Director::compareTo).collect(
                    Collectors.toCollection(LinkedHashSet::new)));
            directors.forEach(director -> addDirectorToFilm(filmId, director.getId()));
        } else {
            directors = new HashSet<>();
        }

        Film newFilm = Film.builder()
                .id(filmId)
                .name(film.getName())
                .description(film.getDescription())
                .duration(film.getDuration())
                .releaseDate(film.getReleaseDate())
                .mpa(film.getMpa())
                .genres(genres)
                .directors(directors)
                .likes(film.getLikes())
                .build();

        log.info("Добавлен фильм: " + newFilm.getName());
        return Optional.of(newFilm);
    }

    @Override
    public Optional<Film> updateFilm(Film film) {
        String sqlQuery =
                "update films set " +
                        "name = ?, description = ?, duration = ?, release_date = ?, mpa_id = ? " +
                        "where film_id = ?";

        int rowCount = jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getMpaId(),
                film.getId());

        if (rowCount == 0) {
            return Optional.empty();
        }

        jdbcTemplate.update("delete from film_genres where film_id = ?", film.getId());

        Set<Genre> genres = film.getGenres();
        if (genres != null) {
            genres.forEach(genre -> addGenreToFilm(film.getId(), genre.getId()));
        } else {
            genres = new HashSet<>();
        }

        jdbcTemplate.update("delete from film_directors where film_id = ?", film.getId());

        Set<Director> directors = film.getDirectors();
        if (directors != null) {
            directors.forEach(director -> addDirectorToFilm(film.getId(), director.getId()));
        } else {
            directors = new HashSet<>();
        }

        Film newFilm = Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .duration(film.getDuration())
                .releaseDate(film.getReleaseDate())
                .mpa(film.getMpa())
                .genres(genres)
                .directors(directors)
                .likes(film.getLikes())
                .build();

        log.info("Обновлен фильм: " + newFilm.getName());
        return Optional.of(newFilm);
    }

    @Override
    public Optional<Film> getFilm(Long id) {
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
    public boolean filmExist(Long id) {
        String sqlQuery = "select 1 from films where film_id = ? limit 1";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sqlQuery, id);
        return result.next();
    }

    @Override
    public boolean filmNotExist(Long id) {
        return !filmExist(id);
    }

    @Override
    public void addLike(Long id, Long userId) {
        String sqlQuery = "merge into film_likes(film_id, user_id) key(film_id, user_id) values(?, ?)";
        jdbcTemplate.update(sqlQuery, id, userId);
        log.info("Фильму с id " + id + " поставил лайк пользователь с id " + userId);
    }

    @Override
    public void removeLike(Long id, Long userId) {
        String sqlQuery = "delete from film_likes where film_id = ? and user_id = ?";
        jdbcTemplate.update(sqlQuery, id, userId);
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
    public Optional<Mpa> getMpaById(int id) {
        String sqlQuery = "select * from mpa where mpa_id = ?";
        Collection<Mpa> mpa = jdbcTemplate.query(sqlQuery, this::mapRowToMpa, id);
        return mpa.stream().findFirst();
    }

    @Override
    public Collection<Genre> getAllGenres() {
        String sqlQuery = "select * from genres";
        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre);
    }

    @Override
    public Optional<Genre> getGenreById(int id) {
        String sqlQuery = "select * from genres where genre_id = ?";
        Collection<Genre> genres = jdbcTemplate.query(sqlQuery, this::mapRowToGenre, id);
        return genres.stream().findFirst();
    }

    @Override
    public void deleteFilmById(Long id) {
        jdbcTemplate.update("delete from films where film_id = ?", id);
    }

    private void addGenreToFilm(Long filmId, int genreId) {
        String sqlQuery = "merge into film_genres(film_id, genre_id) key(film_id, genre_id) values(?, ?)";
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

    private Set<Director> getFilmDirectors(Long id) {
        String sqlQuery =
                "select " +
                        "   film_directors.director_id, " +
                        "   directors.name " +
                        "from film_directors " +
                        "   left join directors " +
                        "   on film_directors.director_id = directors.director_id " +
                        "where film_directors.film_id = ? " +
                        "order by film_directors.director_id";

        return new HashSet<>(jdbcTemplate.query(sqlQuery, this::mapRowToDirector, id));
    }

    private Set<Long> getFilmLikes(Long id) {
        String sqlQuery =
                "select " +
                        "   film_likes.user_id " +
                        "from film_likes " +
                        "where film_likes.film_id = ?";

        return new HashSet<>(jdbcTemplate.queryForList(sqlQuery, Long.class, id));
    }

    private Optional<Film> getFilmById(Long id) {
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

        Collection<Film> films = jdbcTemplate.query(sqlQuery, this::mapRowToFilm, id);
        return films.stream().findFirst();
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
                .directors(getFilmDirectors(resultSet.getLong("film_id")))
                .likes(getFilmLikes(resultSet.getLong("film_id")))
                .build();
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return new Genre(resultSet.getInt("genre_id"), resultSet.getString("name"));
    }

    private Director mapRowToDirector(ResultSet resultSet, int rowNum) throws SQLException {
        return Director.builder()
                .id(resultSet.getLong("director_id"))
                .name(resultSet.getString("name"))
                .build();
    }

    private Mpa mapRowToMpa(ResultSet resultSet, int rowNum) throws SQLException {
        return new Mpa(resultSet.getInt("mpa_id"), resultSet.getString("name"),
                resultSet.getString("description"));
    }

    private void addDirectorToFilm(Long filmId, Long directorId) {
        String sqlQuery = "merge into film_directors(film_id, director_id) key(film_id, director_id) values(?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, directorId);
    }

    @Override
    public Optional<Director> addDirector(Director director) {
        Map<String, Object> directorMap = director.toMap();

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("director_id");

        long directorId = simpleJdbcInsert.executeAndReturnKey(directorMap).longValue();
        Director newDirector = Director.builder().id(directorId).name(director.getName()).build();

        log.info("Добавлен режиссёр: " + director.getName());
        return Optional.of(newDirector);
    }

    @Override
    public boolean directorExist(Long id) {
        String sqlQuery = "select * from directors where director_id = ? limit 1";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sqlQuery, id);
        return result.next();
    }

    @Override
    public boolean directorNotExist(Long id) {
        return !directorExist(id);
    }

    @Override
    public Collection<Film> getDirectorFilms(Long directorId, String sort) {
        if (sort.equals("year")) {
            String sqlQuery = "select " +
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
                    "where films.film_id in (select film_id from film_directors where director_id = ?) " +
                    "order by films.release_date";
            return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, directorId);
        } else {
            String sqlQuery = "select " +
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
                    "   left join film_likes " +
                    "   on films.film_id = film_likes.film_id " +
                    "where films.film_id in ( " +
                    "                       select film_id " +
                    "                       from film_directors " +
                    "                       where director_id = ?) " +
                    "group by films.film_id " +
                    "order by count(user_id) desc";
            return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, directorId);
        }
    }

    @Override
    public Collection<Director> getAllDirectors() {
        String sqlQuery = "select * from directors";
        return jdbcTemplate.query(sqlQuery, this::mapRowToDirector);
    }

    @Override
    public Optional<Director> getDirector(Long id) {
        String sqlQuery = "select * from directors where director_id = ?";
        Collection<Director> directors = jdbcTemplate.query(sqlQuery, this::mapRowToDirector, id);
        return directors.stream().findFirst();
    }

    @Override
    public Optional<Director> updateDirector(Director director) {
        String sqlQuery = "update directors set name = ? where director_id = ?";
        int rowCount = jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        if (rowCount == 0) {
            return Optional.empty();
        }
        log.info("Обновлён режиссёр: " + director.getName());
        return Optional.of(director);
    }

    @Override
    public void removeDirector(Long id) {
        String sqlQuery = "delete from directors where director_id = ?";
        jdbcTemplate.update(sqlQuery, id);

        log.info("Режиссёр с id " + id + " удалён.");
    }
}