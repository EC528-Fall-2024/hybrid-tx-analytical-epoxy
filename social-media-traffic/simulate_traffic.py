import psycopg2
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
    return [(faker.user_name(), faker.email()) for _ in range(num)]

def generate_posts(num, user_ids):
    return [(random.choice(user_ids), faker.text(max_nb_chars=200)) for _ in range(num)]

def generate_likes(num, user_ids, post_ids):
    return [(random.choice(user_ids), random.choice(post_ids)) for _ in range(num)]

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

        def get_next_user_id(cursor):
            cursor.execute("SELECT MAX(user_id) FROM users")
            max_id = cursor.fetchone()[0]
            return (max_id or 0) + 1

        def generate_users_temp(num_users, start_id):
            return [(start_id + i, f"user{start_id + i}", f"user{start_id + i}@example.com") 
                    for i in range(num_users)]

        # Generate and insert users
        next_id = get_next_user_id(cursor)
        users = generate_users_temp(NUM_USERS, next_id)
        cursor.executemany("""
            INSERT INTO users (user_id, username, email, created_at) 
            VALUES (%s, %s, %s, CURRENT_TIMESTAMP);
        """, users)
        conn.commit()

        # print("test1")

        # Select user_ids after insertion
        cursor.execute("""
            SELECT user_id FROM users 
            WHERE username IN %s;
        """, (tuple(f'user{user[0]}' for user in users),))
        user_ids = [row[0] for row in cursor.fetchall()]


    except psycopg2.Error as e:
        print("Error:", e)
        conn.rollback()

    finally:
        if cursor:
            cursor.close()
        if conn:
            conn.close()

if __name__ == "__main__":
    # while True:
    generate_data()
    
    print('done')
        # time.sleep(10)  # Sleep for 5 seconds before generating data again
