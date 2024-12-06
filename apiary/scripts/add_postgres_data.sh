#!/bin/bash

# Get container ID (assuming postgres container is running)
CONTAINER_ID=$(docker ps | grep postgres | awk '{print $1}')

# Copy CSV files to container
ROOT_DIR="$(dirname "$(dirname "$(readlink -f "$0")")")"
USERS_CSV="$ROOT_DIR/sql/dummy-data/users.csv"
POSTS_CSV="$ROOT_DIR/sql/dummy-data/posts.csv"
LIKES_CSV="$ROOT_DIR/sql/dummy-data/likes.csv"

docker cp $USERS_CSV $CONTAINER_ID:/users.csv
docker cp $POSTS_CSV $CONTAINER_ID:/posts.csv
docker cp $LIKES_CSV $CONTAINER_ID:/likes.csv

# Create and populate tables
docker exec -i $CONTAINER_ID psql -U postgres << EOF

CREATE DATABASE social_media;
\c social_media

-- Create users table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create posts table
CREATE TABLE posts (
    post_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create likes table
CREATE TABLE likes (
    like_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id),
    post_id INTEGER REFERENCES posts(post_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Copy data from CSV files
COPY users(user_id, username, email, created_at)
FROM '/users.csv'
DELIMITER ','
CSV HEADER;

COPY posts(post_id, user_id, content, created_at)
FROM '/posts.csv'
DELIMITER ','
CSV HEADER;

COPY likes(like_id, user_id, post_id, created_at)
FROM '/likes.csv'
DELIMITER ','
CSV HEADER;

EOF