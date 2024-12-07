echo "Initializing PostgreSQL database in docker"

./initialize_postgres_docker.sh >/dev/null 2>&1

echo "PostgreSQL initialized successfully"
echo "Initializing ClickHouse database in docker"

./initialize_clickhouse_docker.sh >/dev/null 2>&1

echo "ClickHouse initialized successfully"
