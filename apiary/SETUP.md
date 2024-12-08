# Social Media Mock App Setup

This is a step-by-step tutorial on creating and adding mock data onto your Postgres database to test ETL. If you have an existing dataset you want to test, find instructions on using it in [UPLOAD_DS.md](https://github.com/EC528-Fall-2024/hybrid-tx-analytical-epoxy/blob/main/apiary/UPLOAD_DS.md).

## Create Mock Data

We created a python script to create three SQL files with mock data. The schemas is as follows:

```
Users
    user_id (INTEGER)
    username (STRING)
    email (STRING)
    created_at (DATETIME)
    
Posts
    post_id (INTEGER)
    user_id (INTEGER) (Reference: Users.user_id)
    content (STRING)
    created_at (DATETIME)
    
Likes
    like_id (INTEGER)
    user_id (INTEGER) (Reference: Users.user_id)
    post_id (INTEGER) (Reference: Posts.post_id)
    created_at (DATETIME)
```

To propogate these three files onto your local environment, first go to the `sql/dummy-data` directory. This step is important; we want the data to reside in this directory.

    ```
    cd apiary/sql/dummy-data
    ```
    
Next, we can run `testing_data_generator.py`:

```
py testing_data_generator.py
```

## Add Mock Data to Postgres

First, ensure Postgres is deployed onto a Docker container:

```
scripts/initialize_postgres_docker.sh
```

You can check if it is running, and identify the database container ID

```
docker ps
```

To add your newly created data into the Postgres docker container, run the `add_postgres_data.sh` bash script:

```
scripts/add_postgres_data.sh
```

## Batching through mock traffic

To test if this ETL process works, you can simulate traffic within our mock application. This will randomly add/remove a number of rows to all three tables. This number is set to be random, but you can manually set a number of rows to change in `social-media-traffic/simulate_traffic.py`

```
cd ../social-media-traffic
py simulate_traffic.py
```

After a change has been made to the postgres data, you can rerun ETL to see that the process is faster and more efficient.