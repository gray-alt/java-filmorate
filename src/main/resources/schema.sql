CREATE TABLE IF NOT EXISTS mpa (
	mpa_id INTEGER NOT NULL PRIMARY KEY,
	name VARCHAR
);

CREATE TABLE IF NOT EXISTS films (
	film_id BIGINT NOT NULL PRIMARY KEY,
	name VARCHAR,
	description TEXT,
	release_date DATE,
	duration INTEGER,
	mpa_id INTEGER REFERENCES mpa (mpa_id)
);

CREATE TABLE IF NOT EXISTS users (
	user_id BIGINT NOT NULL PRIMARY KEY,
	email VARCHAR,
	login VARCHAR,
	name VARCHAR,
	birthday DATE
);

CREATE TABLE IF NOT EXISTS genres (
	genre_id INTEGER NOT NULL PRIMARY KEY,
	name VARCHAR
);

CREATE TABLE IF NOT EXISTS film_genres (
	film_id BIGINT REFERENCES films (film_id),
	genre_id INTEGER REFERENCES genres (genre_id)
);

CREATE TABLE IF NOT EXISTS film_likes (
	film_id BIGINT REFERENCES films (film_id),
	user_id BIGINT REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS friends (
	user_id BIGINT REFERENCES users (user_id),
	friend_id BIGINT REFERENCES users (user_id)
);