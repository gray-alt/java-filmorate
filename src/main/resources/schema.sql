DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS genres;

DROP TABLE IF EXISTS film_directors;
DROP TABLE IF EXISTS directors;

DROP TABLE IF EXISTS friends;

DROP TABLE IF EXISTS film_likes;
DROP TABLE IF EXISTS users;

DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS mpa;

CREATE TABLE IF NOT EXISTS mpa (
	mpa_id INTEGER PRIMARY KEY,
	name VARCHAR,
	description VARCHAR
);

CREATE TABLE IF NOT EXISTS films (
	film_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	name VARCHAR,
	description TEXT,
	release_date DATE,
	duration INTEGER,
	mpa_id INTEGER REFERENCES mpa (mpa_id)
);

CREATE TABLE IF NOT EXISTS users (
	user_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	email VARCHAR,
	login VARCHAR,
	name VARCHAR,
	birthday DATE
);

CREATE TABLE IF NOT EXISTS genres (
	genre_id INTEGER PRIMARY KEY,
	name VARCHAR
);

CREATE TABLE IF NOT EXISTS film_genres (
	film_id BIGINT REFERENCES films (film_id),
	genre_id INTEGER REFERENCES genres (genre_id),
	CONSTRAINT PK_FILM_GENRES PRIMARY KEY (film_id,genre_id)
);

CREATE TABLE IF NOT EXISTS directors (
	director_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	name VARCHAR
);

CREATE TABLE IF NOT EXISTS film_directors (
    film_id BIGINT REFERENCES films (film_id),
    director_id BIGINT REFERENCES directors (director_id),
    CONSTRAINT PK_FILM_DIRECTORS PRIMARY KEY (film_id,director_id)
);

CREATE TABLE IF NOT EXISTS film_likes (
	film_id BIGINT REFERENCES films (film_id),
	user_id BIGINT REFERENCES users (user_id),
	CONSTRAINT PK_FILM_LIKES PRIMARY KEY (film_id,user_id)
);

CREATE TABLE IF NOT EXISTS friends (
	user_id BIGINT REFERENCES users (user_id),
	friend_id BIGINT REFERENCES users (user_id),
	status BOOLEAN,
	CONSTRAINT PK_FRIENDS PRIMARY KEY (user_id,friend_id)
);