#!/bin/bash
set -e

SCRIPT_DIR=$(dirname $(realpath $0))

# Pull the postgres docker image
docker pull postgres:14.5-bullseye

# Postgres config file
PGCONFIG=""
NAME="apiary-postgres"
PORT=5432

# Parse command-line arguments if passed
if [[ $# -eq 1 ]]; then
    PGCONFIG="$PWD/$1"
fi

if [[ $# -eq 3 ]]; then
    PGCONFIG="$PWD/$1"
    NAME="$2"
    PORT="$3"
fi

echo "[postgres] Running postgres docker container"
docker run -d \
    -p $PORT:5432 \
    --rm \
    --name "$NAME" \
    --env PGDATA=/var/lib/postgresql-static/data\
    --env POSTGRES_PASSWORD=dbos postgres:14.5-bullseye \

echo "[postgres] Sleeping for 10s"
sleep 10

docker exec -i $NAME psql -h localhost -U postgres -p $PORT -t < ${SCRIPT_DIR}/init_postgres.sql

echo "PostgreSQL initialized and running in Docker container named $NAME on port $PORT"