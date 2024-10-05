#!/bin/bash
set -x

SCRIPT_DIR=$(dirname $(realpath $0))

cd ${SCRIPT_DIR}/../

# Start ClickHouse Docker image
docker pull clickhouse/clickhouse-server

# Run ClickHouse container
docker run -d --rm -p 9000:9000 -p 8123:8123 \
           --name clickhouse_server \
           clickhouse/clickhouse-server

# Wait until ClickHouse is ready
MAX_TRY=30
cnt=0
while [[ -z "${ready}" ]]; do
  docker logs clickhouse_server | grep -q "Ready for connections" && ready="ready"
  cnt=$[$cnt+1]
  if [[ $cnt -eq ${MAX_TRY} ]]; then
    echo "Wait timed out. ClickHouse failed to start."
    exit 1
  fi
  sleep 5 # avoid busy loop
done

echo "ClickHouse is now running."

# Create a database
docker exec -it clickhouse_server clickhouse-client -q "CREATE DATABASE IF NOT EXISTS apiary_provenance"

# Populate schema (DDL)
docker exec -i clickhouse_server clickhouse-client --query="SOURCE sql/provenance_ddl.sql"

echo "ClickHouse database 'apiary_provenance' initialized and schema populated."
