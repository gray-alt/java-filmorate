package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Event {
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    Timestamp timestamp;
    @NotNull
    Long userId;
    @NotBlank
    EventType eventType;
    @NotBlank
    Operation operation;
    @NotNull
    Long eventId;
    @NotNull
    Long entityId;
}
