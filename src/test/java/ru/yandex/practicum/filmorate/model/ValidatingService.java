package ru.yandex.practicum.filmorate.model;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

@Service
public class ValidatingService {

    private final Validator validator;

    public ValidatingService(Validator validator) {
        this.validator = validator;
    }

    public void validateSimpleUser(User.SimpleUser user) throws ValidationException {
        Set<ConstraintViolation<User.SimpleUser>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            throw new ValidationException("Ошибка валидации");
        }
    }

    public void validateSimpleFilm(Film.SimpleFilm film) throws ValidationException {
        Set<ConstraintViolation<Film.SimpleFilm>> violations = validator.validate(film);
        if (!violations.isEmpty()) {
            throw new ValidationException("Ошибка валидации");
        }
    }
}
