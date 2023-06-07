# java-filmorate
### Схема
![Filmorate DB structure](/src/main/resources/images/filmorate.png)

### Описание

**`films`**  
Содержит информацию о фильмах.

- `film_id` - идентификатор фильма (первичный ключ);
- `name` - название фильма;
- `description` - описание фильма;
- `relase_date` - дата выпуска фильма;
- `duration` - продолжительность фильма;
- `mpa_id` - идентификатор рейтинга (внешний ключ).

**`user`**  
Содержит информацию о пользователях.

- `user_id` — идентификатор рейтинга (первичный ключ);
- `email` - адрес электронной почты пользователя;
- `login` - логин пользователя;
- `name` - имя пользователя;
- `birthday` - дата рождения пользователя.

**`mpa`**  
Содержит информацию о рейтинге Ассоциации кинокомпаний (МРА) фильма.

- `mpa_id` — идентификатор рейтинга (первичный ключ);
- `name` — название рейтинга;
- `description` - описание аббревиатуры рейтинга.

**`genres`**  
Содержит информацию о жанрах фильмов.

- `genre_id` — идентификатор жанра (первичный ключ);
- `name` — название жанра.

**`friends`**  
Содержит данные о дружеских отношениях между пользователями.

- `user_id` - идентификатор пользователя (внешний ключ);
- `friend_id` — идентификатор пользователя друга (внешний ключ);
- `status` - статус связи, может принимать значения 0 или 1.

**`film_likes`**  
Содержит информацию о фильмах и лайках пользователей.

- `film_id` - идентификатор фильма (внешний ключ);
- `user_id` - идентификатор пользователя (внешний ключ); 

**`film_genres`**  
Содержит информацию о жанрах каждого фильма.

- `film_id` - идентификатор фильма (внешний ключ);
- `genre_id` - идентификатор жанра(внешний ключ);

### Примеры запросов

**Получение всех пользователей:**
```
SELECT *
FROM users;
```

**Получение пользователя по id:**
```
SELECT *
FROM users
WHERE user_id = 1;
```

**Получение всех друзей пользователя:**
```
SELECT *
FROM users
WHERE user_id IN (SELECT friend_id
                  FROM friends
                  WHERE user_id = 1)

```

**Получение общих друзей двух пользователей:**
```
SELECT *
FROM users
WHERE user_id IN (SELECT 
                    friends.friend_id 
                  FROM friends as friends
                    INNER JOIN friends as other_friends
                    ON friends.friend_id = other_friends.friend_id
                  WHERE friends.user_id = 1 and other_friends.user_id = 2)              
```

**Получение всех фильмов:**
```
SELECT *
FROM films;
```

**Получение фильма по id:**
```
SELECT *
FROM films
WHERE film_id = 1;
```

**Вывод 10 популярных фильмов:**
```
SELECT *
FROM films
WHERE film_id in (SELECT TOP 10
                    films.film_id
                  FROM films
                  LEFT FOIN film_likes
                    ON films.film_id = film_likes.film_id
                  GROUP BY films.film_id
                  ORDER BY COUNT(film_likes.user_id) DESC)
```