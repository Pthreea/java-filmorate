DROP TABLE IF EXISTS film_genres CASCADE;
DROP TABLE IF EXISTS likes CASCADE;
DROP TABLE IF EXISTS friendships CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS mpa_ratings CASCADE;

CREATE TABLE IF NOT EXISTS mpa_ratings
(
    mpa_id
    INTEGER
    PRIMARY
    KEY,
    name
    VARCHAR
(
    10
) NOT NULL UNIQUE,
    description VARCHAR
(
    255
)
    );

CREATE TABLE IF NOT EXISTS genres
(
    genre_id
    INTEGER
    PRIMARY
    KEY,
    name
    VARCHAR
(
    50
) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS users
(
    user_id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    email
    VARCHAR
(
    255
) NOT NULL UNIQUE,
    login VARCHAR
(
    100
) NOT NULL UNIQUE,
    name VARCHAR
(
    255
),
    birthday DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS films
(
    film_id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    255
) NOT NULL,
    description VARCHAR
(
    200
),
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL,
    mpa_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(
    mpa_id
) REFERENCES mpa_ratings
(
    mpa_id
)
    );

CREATE TABLE IF NOT EXISTS film_genres
(
    film_id
    BIGINT
    NOT
    NULL,
    genre_id
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    film_id,
    genre_id
),
    FOREIGN KEY
(
    film_id
) REFERENCES films
(
    film_id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    genre_id
) REFERENCES genres
(
    genre_id
)
    );

CREATE TABLE IF NOT EXISTS friendships
(
    user_id
    BIGINT
    NOT
    NULL,
    friend_id
    BIGINT
    NOT
    NULL,
    status
    VARCHAR
(
    20
) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    user_id,
    friend_id
),
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    user_id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    friend_id
) REFERENCES users
(
    user_id
)
  ON DELETE CASCADE,
    CHECK
(
    user_id
    <>
    friend_id
)
    );

CREATE TABLE IF NOT EXISTS likes
(
    film_id
    BIGINT
    NOT
    NULL,
    user_id
    BIGINT
    NOT
    NULL,
    created_at
    TIMESTAMP
    DEFAULT
    CURRENT_TIMESTAMP,
    PRIMARY
    KEY
(
    film_id,
    user_id
),
    FOREIGN KEY
(
    film_id
) REFERENCES films
(
    film_id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    user_id
)
  ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_films_mpa ON films(mpa_id);
CREATE INDEX IF NOT EXISTS idx_films_release_date ON films(release_date);
CREATE INDEX IF NOT EXISTS idx_film_genres_film ON film_genres(film_id);
CREATE INDEX IF NOT EXISTS idx_film_genres_genre ON film_genres(genre_id);
CREATE INDEX IF NOT EXISTS idx_friendships_user ON friendships(user_id);
CREATE INDEX IF NOT EXISTS idx_friendships_friend ON friendships(friend_id);
CREATE INDEX IF NOT EXISTS idx_likes_film ON likes(film_id);
CREATE INDEX IF NOT EXISTS idx_likes_user ON likes(user_id);