package org.dbos.apiary.etldemo.clickhouse;

import org.dbos.apiary.etldemo.clickhouse.ClickHouseConnection;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClickHouseService {

    private final ClickHouseConnection clickHouseConnection;

    public ClickHouseService() {
        this.clickHouseConnection = new ClickHouseConnection();
    }

    // Method to fetch list of databases
    public List<String> getDatabases() {
        List<String> databases = new ArrayList<>();
        try (Connection connection = clickHouseConnection.getClickHouseConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW DATABASES")) {
            while (resultSet.next()) {
                databases.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return databases;
    }

    // Method to fetch tables for a specific database
    public List<String> getTables(String database) {
        List<String> tables = new ArrayList<>();
        try (Connection connection = clickHouseConnection.getClickHouseConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW TABLES FROM " + database)) {
            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tables;
    }

    // Method to fetch contents of a specific table
    public List<String> getTableContents(String database, String table) {
        List<String> tableContents = new ArrayList<>();
        try (Connection connection = clickHouseConnection.getClickHouseConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + database + "." + table + " LIMIT 10")) {
            while (resultSet.next()) {
                tableContents.add(resultSet.getString(1));  // Customize based on your table structure
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableContents;
    }
}
