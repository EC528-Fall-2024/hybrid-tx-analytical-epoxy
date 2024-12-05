package org.dbos.apiary.etldemo.clickhouse;

import ru.yandex.clickhouse.ClickHouseDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import java.sql.Connection;
import java.sql.SQLException;
import com.clickhouse.jdbc.ClickHouseDataSource;

public class ClickHouseConnection {

    private String clickhouseUrl = "jdbc:clickhouse://localhost:8123/default"; // Default URL

    public ClickHouseConnection() {
        // Default constructor with default URL
    }

    public ClickHouseConnection(String clickhouseUrl) {
        // Constructor to set custom URL
        this.clickhouseUrl = clickhouseUrl;
    }

    public void setClickHouseUrl(String clickhouseUrl) {
        // Setter to update URL dynamically
        this.clickhouseUrl = clickhouseUrl;
    }

    public String getClickHouseUrl() {
        // Getter to retrieve current URL
        return this.clickhouseUrl;
    }

    public Connection getClickHouseConnection() throws SQLException {
        ClickHouseDataSource dataSource = new ClickHouseDataSource(clickhouseUrl);
        return dataSource.getConnection();
    }
}
}
