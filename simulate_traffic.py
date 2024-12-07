import psycopg2
from psycopg2 import sql
from faker import Faker
import random
import time

# Database configuration
DB_CONFIG = {
    'dbname': 'social_media',
    'user': 'postgres',
    'password': 'dbos',  # Replace with your actual password
    'host': 'localhost',  # Change to your database host
    'port': 5432  # Default PostgreSQL port
}

# Randomize range for each category
USER_MIN = 1
USER_MAX = 3

POST_MIN = 1
POST_MAX = 15

LIKE_MIN = 1
LIKE_MAX = 30

# Initialize Faker
faker = Faker()

def generate_users(num):
    return [(faker.user_name(), faker.email()) for _ in range(num)]

def generate_posts(num, user_ids):
    return [(random.choice(user_ids), faker.text(max_nb_chars=200)) for _ in range(num)]

def generate_likes(num, user_ids, post_ids):
    return [(random.choice(user_ids), random.choice(post_ids)) for _ in range(num)]

# Connect to the database
try:
    conn = psycopg2.connect(**DB_CONFIG)
    cursor = conn.cursor()

    while True:
        # Randomize the number of users, posts, and likes for each iteration
        NUM_USERS = random.randint(USER_MIN, USER_MAX)
        NUM_POSTS = random.randint(POST_MIN, POST_MAX)
        NUM_LIKES = random.randint(LIKE_MIN, LIKE_MAX)

        print(f"Generating {NUM_USERS} users, {NUM_POSTS} posts, {NUM_LIKES} likes.")

        # Generate and insert users
        users = generate_users(NUM_USERS)
        cursor.executemany("""
            INSERT INTO users (username, email) 
            VALUES (%s, %s);
        """, users)
        conn.commit()

        # Select user_ids after insertion
        cursor.execute("""
            SELECT user_id FROM users WHERE username IN %s;
        """, (tuple(user[0] for user in users),))
        user_ids = [row[0] for row in cursor.fetchall()]

        # Generate and insert posts
        posts = generate_posts(NUM_POSTS, user_ids)
        cursor.executemany("""
            INSERT INTO posts (user_id, content) 
            VALUES (%s, %s);
        """, posts)
        conn.commit()

        # Select post_ids after insertion
        cursor.execute("""
            SELECT post_id FROM posts WHERE user_id IN %s;
        """, (tuple(user_ids),))
        post_ids = [row[0] for row in cursor.fetchall()]

        # Generate and insert likes
        likes = generate_likes(NUM_LIKES, user_ids, post_ids)
        cursor.executemany("""
            INSERT INTO likes (user_id, post_id) 
            VALUES (%s, %s);
        """, likes)
        conn.commit()

        print(f"Inserted {NUM_USERS} users, {NUM_POSTS} posts, {NUM_LIKES} likes.")

        # Sleep for a while before the next cycle (e.g., 5 seconds)
        time.sleep(5)

except psycopg2.Error as e:
    print("Error:", e)
    conn.rollback()

finally:
    # Close the connection
    if cursor:
        cursor.close()
    if conn:
        conn.close()
