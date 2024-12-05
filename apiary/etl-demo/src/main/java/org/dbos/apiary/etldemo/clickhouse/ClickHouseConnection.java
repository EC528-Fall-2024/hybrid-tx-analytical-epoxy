package org.dbos.apiary.etldemo.clickhouse;

import com.clickhouse.jdbc.ClickHouseDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class ClickHouseConnection {

    private String clickhouseUrl = "jdbc:clickhouse://localhost:8123/default"; // Default URL
    private String clickhouseUser = ""; // Default username
    private String clickhousePassword = ""; // Default password

    public ClickHouseConnection() {
        // Default constructor with default URL, username, and password
    }

    public ClickHouseConnection(String clickhouseUrl) {
        // Constructor to set custom URL
        this.clickhouseUrl = clickhouseUrl;
    }

    public ClickHouseConnection(String clickhouseUrl, String username, String password) {
        // Constructor to set custom URL, username, and password
        this.clickhouseUrl = clickhouseUrl;
        this.clickhouseUser = username;
        this.clickhousePassword = password;
    }

    public void setConnectionParams(String url, String user, String password) {
        this.clickhouseUrl = url;
        this.clickhouseUser = user;
        this.clickhousePassword = password;
    }

    public Connection getClickHouseConnection() throws SQLException {
        // Use Properties to set username and password
        Properties properties = new Properties();
        properties.setProperty("user", this.clickhouseUser);
        properties.setProperty("password", this.clickhousePassword);

        ClickHouseDataSource dataSource = new ClickHouseDataSource(clickhouseUrl, properties);
        return dataSource.getConnection();
    }
}
