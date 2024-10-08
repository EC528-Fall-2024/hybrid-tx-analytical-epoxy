#!/bin/bash
set -ex

SCRIPT_DIR=$(dirname $(realpath $0))
cd ${SCRIPT_DIR}

# Start Postgres Docker image.
docker pull mysql

# Set the password to dbos, default user is .
docker run -d -p 8000:8000 --rm --name="apiary-mysql" --env MYSQL_ROOT_PASSWORD=dbos mysql:latest

# Wait a bit.
sleep 10

# Create DBOS database.
docker exec -i apiary-mysql mysql -hlocalhost -uroot -pdbos -t < init_mysql.sql
