import pandas as pd
import random
from datetime import datetime, timedelta

def random_date(start, end):
    return start + timedelta(seconds=random.randint(0, int((end - start).total_seconds())))

# Generate Users data
num_users = 500
users = []
for user_id in range(1, num_users + 1):
    users.append({
        'user_id': user_id,
        'username': f'user{user_id}',
        'email': f'user{user_id}@example.com',
        'created_at': random_date(datetime(2020, 1, 1), datetime(2023, 12, 31))
    })

# Generate Posts data
num_posts = 1000
posts = []
for post_id in range(1, num_posts + 1):
    posts.append({
        'post_id': post_id,
        'user_id': random.randint(1, num_users),
        'content': f'This is post {post_id}',
        'created_at': random_date(datetime(2020, 1, 1), datetime(2023, 12, 31))
    })

# Generate Likes data
num_likes = 2000
likes = []
for like_id in range(1, num_likes + 1):
    likes.append({
        'like_id': like_id,
        'user_id': random.randint(1, num_users),
        'post_id': random.randint(1, num_posts),
        'created_at': random_date(datetime(2020, 1, 1), datetime(2023, 12, 31))
    })

# Convert to DataFrames and export to CSV
users_df = pd.DataFrame(users)
posts_df = pd.DataFrame(posts)
likes_df = pd.DataFrame(likes)

users_df.to_csv('users.csv', index=False)
posts_df.to_csv('posts.csv', index=False)
likes_df.to_csv('likes.csv', index=False)