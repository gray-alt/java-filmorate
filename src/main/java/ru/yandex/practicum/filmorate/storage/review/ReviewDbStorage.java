package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("reviewDbStorage")
@Slf4j
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Review> addReview(Review review) {
        Map<String, Object> reviewMap = review.toMap();

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");

        long reviewId = simpleJdbcInsert.executeAndReturnKey(reviewMap).longValue();

        Review newReview = Review.builder()
                .id(reviewId)
                .content(review.getContent())
                .isPositive(review.isPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(0)
                .build();

        log.info("Добавлен отзыв к фильму с id: " + review.getFilmId());
        return Optional.of(newReview);
    }

    @Override
    public Optional<Review> updateReview(Review review) {
        String sqlQuery =
                "update reviews set " +
                        "content = ?, positive = ? " +
                        "where review_id = ?";

        int rowCount = jdbcTemplate.update(sqlQuery,
                review.getContent(),
                review.isPositive(),
                review.getId());

        if (rowCount == 0) {
            return Optional.empty();
        }

        Review newReview = Review.builder()
                .id(review.getId())
                .content(review.getContent())
                .isPositive(review.isPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(review.getUseful())
                .build();

        log.info("Обновлен отзыв к фильму с id: " + newReview.getFilmId());
        return Optional.of(newReview);
    }

    @Override
    public void deleteReview(Long id) {
        String sqlQuery = "delete from reviews where review_id = ?";
        jdbcTemplate.update(sqlQuery, id);
        log.info("Удален отзыв с id " + id);
    }

    @Override
    public Optional<Review> getReview(Long id) {
        String sqlQuery = "" +
                "select " +
                "   reviews.review_id, " +
                "   reviews.content, " +
                "   reviews.positive, " +
                "   reviews.user_id, " +
                "   reviews.film_id, " +
                "   SUM(review_useful.score) " +
                "from reviews " +
                "   left join review_useful " +
                "   on reviews.review_id = review_useful.review_id " +
                "where reviews.review_id = ? " +
                "group by reviews.review_id";

        Collection<Review> reviews = jdbcTemplate.query(sqlQuery, this::mapRowToReview, id);
        return reviews.stream().findFirst();
    }

    @Override
    public Collection<Review> getReviews(Long filmId, int count) {
        String sqlQuery = "" +
                "select top ?" +
                "   reviews.review_id, " +
                "   reviews.content, " +
                "   reviews.positive, " +
                "   reviews.user_id, " +
                "   reviews.film_id, " +
                "   SUM(review_useful.score) as useful " +
                "from reviews " +
                "   left join review_useful " +
                "   on reviews.review_id = review_useful.review_id " +
                "where ? is NULL or reviews.film_id = ? " +
                "group by reviews.review_id";

        return jdbcTemplate.query(sqlQuery, this::mapRowToReview, count);
    }

    @Override
    public boolean reviewExist(Long id) {
        String sqlQuery = "select 1 from reviews where review_id = ? limit 1";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sqlQuery, id);
        return result.next();
    }

    @Override
    public boolean reviewNotExist(Long id) {
        return !reviewExist(id);
    }

    @Override
    public void addLikeDislike(Long id, Long userId, boolean isLike) {
        String sqlQuery = "merge into review_useful(review_id, user_id, score) key(review_id, user_id) values(?, ?, ?)";
        jdbcTemplate.update(sqlQuery, id, userId, isLike ? 1 : -1);
        log.info("Обзору с id " + id + " поставил лайк/дизлайк пользователь с id " + userId);
    }

    @Override
    public void deleteLikeDislike(Long id, Long userId, boolean isLike) {
        String sqlQuery = "delete from review_useful where review_id = ? and user_id = ? and score = ?";
        jdbcTemplate.update(sqlQuery, id, userId, isLike ? 1 : -1);
        log.info("У обзора с id " + id + " удален лайк/дизлайк пользователя с id " + userId);
    }

    private Review mapRowToReview(ResultSet resultSet, int rowNum) throws SQLException {
        return Review.builder()
                .id(resultSet.getLong("review_id"))
                .content(resultSet.getString("content"))
                .isPositive(resultSet.getBoolean("positive"))
                .userId(resultSet.getLong("user_id"))
                .filmId(resultSet.getLong("film_id"))
                .useful(resultSet.getInt("useful"))
                .build();
    }
}
