#!/bin/bash
set -ex

SCRIPT_DIR=$(dirname $(realpath $0))

# Start Postgres Docker image.
docker pull postgres:14.5-bullseye

PGCONFIG=""  # Postgres config file.
NAME="apiary-postgres"
PORT=5432
if [[ $# -eq 1 ]]; then
    PGCONFIG="$PWD/$1"
fi

if [[ $# -eq 3 ]]; then
    PGCONFIG="$PWD/$1"
    NAME="$2"
    PORT="$3"
fi

# Set the password to dbos, default user is postgres.
if [[ -z "$PGCONFIG" ]]; then
	docker run -d -p 5432:5432 --rm --name="$NAME" --env PGDATA=/var/lib/postgresql-static/data --env POSTGRES_PASSWORD=dbos postgres:14.5-bullseye
    # docker run -d --network host --rm --name="$NAME" --env PGDATA=/var/lib/postgresql-static/data --env POSTGRES_PASSWORD=dbos postgres:14.5-bullseye
else
	# Use customized config file
	docker run -d -p 5432:5432 --rm --name="$NAME" --env PGDATA=/var/lib/postgresql-static/data --env POSTGRES_PASSWORD=dbos \
    -v "$PGCONFIG":/etc/postgresql/postgresql.conf \
    postgres:14.5-bullseye -c 'config_file=/etc/postgresql/postgresql.conf'

    # docker run -d --network host --rm --name="$NAME" --env PGDATA=/var/lib/postgresql-static/data --env POSTGRES_PASSWORD=dbos \
#     -v "$PGCONFIG":/etc/postgresql/postgresql.conf \
#     postgres:14.5-bullseye -c 'config_file=/etc/postgresql/postgresql.conf'
fi

sleep 10

docker exec -i $NAME psql -h localhost -U postgres -p $PORT -t < ${SCRIPT_DIR}/init_postgres.sql
