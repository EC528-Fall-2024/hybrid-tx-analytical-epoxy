package org.dbos.apiary.postgresdemo.clickhouse;

import org.dbos.apiary.postgresdemo.clickhouse.ClickHouseConnection;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

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
    public List<Map<String, Object>> getTableContents(String database, String table) {
        List<Map<String, Object>> tableContents = new ArrayList<>();
        try (Connection connection = clickHouseConnection.getClickHouseConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + database + "." + table + " LIMIT 10")) {
            
            // Get metadata to fetch column names
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
    
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);  // Get value as Object to handle various types
                    row.put(columnName, value);  // Add column name and value to the row
                }
                tableContents.add(row);  // Add the row (as a dictionary) to the list
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableContents;
    }
}
