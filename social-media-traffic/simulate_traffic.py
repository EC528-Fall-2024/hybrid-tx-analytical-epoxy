import psycopg2
from faker import Faker
import random
import time

# Database configuration
DB_CONFIG = {
    'dbname': 'social_media',
    'user': 'postgres',
    'password': 'dbos',  # Replace with your actual password
    'host': '10.130.12.97',  # Change to your database host
    'port': 5432  # Default PostgreSQL port
}

# Initialize Faker
faker = Faker()

# Default ranges for random data generation
ranges = {
    "USER_MIN": 1,
    "USER_MAX": 5,
    "POST_MIN": 1,
    "POST_MAX": 15,
    "LIKE_MIN": 1,
    "LIKE_MAX": 30,
}

def generate_users(num):
    # Generate users with random user_id between 60000 and 999999
    return [(random.randint(60000, 999999), faker.user_name(), faker.email()) for _ in range(num)]

def generate_posts(num, user_ids, max_post_id):
    # Generate posts with random user_id from the existing user_ids and unique post_id
    posts = []
    while len(posts) < num:
        post_id = max_post_id + random.randint(1, 10000)  # Ensure IDs are unique by starting from max_post_id
        posts.append((post_id, random.choice(user_ids), faker.text(max_nb_chars=200)))
        max_post_id = post_id  # Update max_post_id to avoid duplication
    return posts, max_post_id

def generate_likes(num, post_ids, user_ids, max_like_id):
    # Generate likes with random user_id from existing user_ids, random post_id and unique like_id
    likes = []
    while len(likes) < num:
        like_id = max_like_id + random.randint(1, 10000)  # Ensure IDs are unique by starting from max_like_id
        likes.append((like_id, random.choice(user_ids), random.choice(post_ids)))
        max_like_id = like_id  # Update max_like_id to avoid duplication
    return likes, max_like_id

def generate_data():
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        print("Connected to postgres!")

        # Randomize the number of users, posts, and likes
        NUM_USERS = random.randint(ranges["USER_MIN"], ranges["USER_MAX"])
        NUM_POSTS = random.randint(ranges["POST_MIN"], ranges["POST_MAX"])
        NUM_LIKES = random.randint(ranges["LIKE_MIN"], ranges["LIKE_MAX"])

        print(f"Generating {NUM_USERS} users, {NUM_POSTS} posts, {NUM_LIKES} likes.")

        # Generate and insert users
        users = generate_users(NUM_USERS)
        cursor.executemany("""
            INSERT INTO users (user_id, username, email) 
            VALUES (%s, %s, %s);
        """, users)
        conn.commit()

        # Fetch the list of user_ids from the users table to use in posts and likes
        cursor.execute("SELECT user_id FROM users;")
        user_ids = [row[0] for row in cursor.fetchall()]

        # Fetch the maximum post_id from the posts table to generate unique post_ids
        cursor.execute("SELECT MAX(post_id) FROM posts;")
        max_post_id = cursor.fetchone()[0] or 60000  # Start from 60000 if no posts exist

        # Generate and insert posts using the user_ids from the users table
        posts, max_post_id = generate_posts(NUM_POSTS, user_ids, max_post_id)
        cursor.executemany("""
            INSERT INTO posts (post_id, user_id, content) 
            VALUES (%s, %s, %s);
        """, posts)
        conn.commit()

        # Fetch the list of post_ids from the posts table to use in likes
        cursor.execute("SELECT post_id FROM posts;")
        post_ids = [row[0] for row in cursor.fetchall()]

        # Fetch the maximum like_id from the likes table to generate unique like_ids
        cursor.execute("SELECT MAX(like_id) FROM likes;")
        max_like_id = cursor.fetchone()[0] or 60000  # Start from 60000 if no likes exist

        # Generate and insert likes using the user_ids and post_ids
        likes, max_like_id = generate_likes(NUM_LIKES, post_ids, user_ids, max_like_id)
        cursor.executemany("""
            INSERT INTO likes (like_id, user_id, post_id) 
            VALUES (%s, %s, %s);
        """, likes)
        conn.commit()

        print(f"Inserted {NUM_USERS} users, {NUM_POSTS} posts, {NUM_LIKES} likes.")

    except psycopg2.Error as e:
        print("Error:", e)
        conn.rollback()

    finally:
        if cursor:
            cursor.close()
        if conn:
            conn.close()

if __name__ == "__main__":
    while True:
        generate_data()
        time.sleep(5)  # Sleep for 5 seconds before generating data again
