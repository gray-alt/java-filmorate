package ru.yandex.practicum.filmorate.storage.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

@Component("eventManager")
public class EventManager {

    private final JdbcTemplate jdbcTemplate;

    public EventManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void updateEvents(Long userId, EventType eventType, Operation operation, Long entityId) {
        String sqlForEvent = "insert into events(user_id, event_type, operation, entity_id) values(?, ?, ?, ?)";
        jdbcTemplate.update(sqlForEvent, userId, eventType.toString(), operation.toString(), entityId);
    }
}
