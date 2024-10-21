package org.dbos.apiary.etldemo.clickhouse;

import ru.yandex.clickhouse.ClickHouseDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ClickHouseConnection {

    private static final String CLICKHOUSE_URL = "jdbc:clickhouse://localhost:8123/default"; // Update URL if needed

    public Connection getClickHouseConnection() throws SQLException {
        ClickHouseDataSource dataSource = new ClickHouseDataSource(CLICKHOUSE_URL);
        return dataSource.getConnection();
    }
}
