package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

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
                .positive(review.getPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(0)
                .build();

        String sqlForEvent = "insert into events(user_id, event_type, operation, entity_id) values(?, ?, ?, ?)";
        jdbcTemplate.update(sqlForEvent, review.getUserId(), EventType.REVIEW.toString(), Operation.ADD.toString(),
                reviewId);

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
                review.getPositive(),
                review.getId());

        if (rowCount == 0) {
            return Optional.empty();
        }

        Optional<Review> newReview = getReview(review.getId());

        if (newReview.isEmpty()) {
            return newReview;
        }

        String sqlForFirstReviewUserId = "select user_id from reviews where review_id = ?";
        Collection<Long> collectionUserId = jdbcTemplate.query(sqlForFirstReviewUserId, this::mapRowReviewUserId,
                review.getId());
        Long userId = collectionUserId.stream().findFirst().get();

        String sqlForEvent = "insert into events(user_id, event_type, operation, entity_id) values(?, ?, ?, ?)";
        jdbcTemplate.update(sqlForEvent, userId, EventType.REVIEW.toString(), Operation.UPDATE.toString(),
                review.getId());

        log.info("Обновлен отзыв к фильму с id: " + newReview.get().getFilmId());
        return newReview;
    }

    @Override
    public void deleteReview(Long id) {
        Review review = getReview(id).get();
        Long userId = review.getUserId();

        String sqlQuery = "delete from reviews where review_id = ?";
        jdbcTemplate.update(sqlQuery, id);

        String sqlForEvent = "insert into events(user_id, event_type, operation, entity_id) values(?, ?, ?, ?)";
        jdbcTemplate.update(sqlForEvent, userId, EventType.REVIEW.toString(), Operation.REMOVE.toString(), id);

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
                "   SUM(review_useful.score) as useful " +
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
                "   SUM(IFNULL(review_useful.score, 0)) as useful " +
                "from reviews " +
                "   left join review_useful " +
                "   on reviews.review_id = review_useful.review_id " +
                "where ? is NULL or reviews.film_id = ? " +
                "group by reviews.review_id " +
                "order by SUM(IFNULL(review_useful.score, 0)) desc";

        return jdbcTemplate.query(sqlQuery, this::mapRowToReview, count, filmId, filmId);
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
        log.info("Отзыву с id " + id + " поставил " + (isLike ? "лайк" : "дизлайк") + " пользователь с id " + userId);
    }

    @Override
    public void deleteLikeDislike(Long id, Long userId, boolean isLike) {
        String sqlQuery = "delete from review_useful where review_id = ? and user_id = ? and score = ?";
        jdbcTemplate.update(sqlQuery, id, userId, isLike ? 1 : -1);
        log.info("У отзыва с id " + id + " удален " + (isLike ? "лайк" : "дизлайк") + " пользователя с id " + userId);
    }

    private Review mapRowToReview(ResultSet resultSet, int rowNum) throws SQLException {
        return Review.builder()
                .id(resultSet.getLong("review_id"))
                .content(resultSet.getString("content"))
                .positive(resultSet.getBoolean("positive"))
                .userId(resultSet.getLong("user_id"))
                .filmId(resultSet.getLong("film_id"))
                .useful(resultSet.getInt("useful"))
                .build();
    }

    private Long mapRowReviewUserId(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getLong("user_id");
    }
}
