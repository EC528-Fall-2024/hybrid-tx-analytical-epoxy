#!/bin/bash
set -e

SCRIPT_DIR=$(dirname $(realpath $0))

# Pull the DuckDB docker image
docker pull datacatering/duckdb:v1.1.0

# DuckDB config file
DUCKDB_FILE=""
NAME="apiary-duckdb"
PORT=4000

# Parse command-line arguments if passed
if [[ $# -eq 1 ]]; then
    DUCKDB_FILE="$PWD/$1"
fi

if [[ $# -eq 3 ]]; then
    DUCKDB_FILE="$PWD/$1"
    NAME="$2"
    PORT="$3"
fi

echo "[duckdb] Running DuckDB docker container"
docker run -d \
    -p $PORT:4000 \
    --rm \
    --name "$NAME" \
    -v "$DUCKDB_FILE:/data/db.duckdb" \
    datacatering/duckdb:v1.1.0

echo "[duckdb] Sleeping for 10s"
sleep 10

docker exec -i $NAME duckdb /data/db.duckdb < ${SCRIPT_DIR}/init_duckdb.sql

echo "DuckDB initialized and running in Docker container named $NAME on port $PORT"