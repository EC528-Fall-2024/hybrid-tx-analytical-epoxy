import psycopg2
from psycopg2 import sql
from faker import Faker
import random
import time
from threading import Thread
from flask import Flask, request, jsonify, send_from_directory

# Database configuration
DB_CONFIG = {
    'dbname': 'social_media',
    'user': 'postgres',
    'password': 'dbos',  # Replace with your actual password
    'host': 'localhost',  # Change to your database host
    'port': 5432  # Default PostgreSQL port
}

# Initialize Faker
faker = Faker()

# Default ranges for random data generation
ranges = {
    "USER_MIN": 1,
    "USER_MAX": 3,
    "POST_MIN": 1,
    "POST_MAX": 15,
    "LIKE_MIN": 1,
    "LIKE_MAX": 30,
}

# State variable to toggle generation
is_generating = False

# Flask app
app = Flask(__name__)

@app.route('/')
def index():
    return send_from_directory('', 'index.html')

@app.route('/update_ranges', methods=['POST'])
def update_ranges():
    global ranges
    data = request.json
    for key in data:
        if key in ranges:
            ranges[key] = int(data[key])
    return jsonify({"message": "Ranges updated", "ranges": ranges})

@app.route('/toggle_generation', methods=['POST'])
def toggle_generation():
    global is_generating
    is_generating = request.json.get("is_generating", is_generating)
    return jsonify({"message": "Generation toggled", "is_generating": is_generating})

@app.route('/get_status', methods=['GET'])
def get_status():
    return jsonify({"is_generating": is_generating, "ranges": ranges})

def generate_users(num):
    return [(faker.user_name(), faker.email()) for _ in range(num)]

def generate_posts(num, user_ids):
    return [(random.choice(user_ids), faker.text(max_nb_chars=200)) for _ in range(num)]

def generate_likes(num, user_ids, post_ids):
    return [(random.choice(user_ids), random.choice(post_ids)) for _ in range(num)]

def data_generation_thread():
    global is_generating
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()

        while True:
            if not is_generating:
                time.sleep(1)
                continue

            # Randomize the number of users, posts, and likes
            NUM_USERS = random.randint(ranges["USER_MIN"], ranges["USER_MAX"])
            NUM_POSTS = random.randint(ranges["POST_MIN"], ranges["POST_MAX"])
            NUM_LIKES = random.randint(ranges["LIKE_MIN"], ranges["LIKE_MAX"])

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

            # Sleep for a while before the next cycle
            time.sleep(5)

    except psycopg2.Error as e:
        print("Error:", e)
        conn.rollback()

    finally:
        if cursor:
            cursor.close()
        if conn:
            conn.close()

# Run the data generation in a separate thread
thread = Thread(target=data_generation_thread)
thread.daemon = True
thread.start()

# Run Flask webserver
if __name__ == "__main__":
    app.run(port=5000)
