package org.dbos.apiary.etldemo.clickhouse;

import org.dbos.apiary.etldemo.clickhouse.ClickHouseConnection;
import org.springframework.stereotype.Service;
import java.sql.ResultSetMetaData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ClickHouseService {

    private final ClickHouseConnection clickHouseConnection;

    public ClickHouseService() {
        this.clickHouseConnection = new ClickHouseConnection();
    }

    public void setConnectionParams(String url, String user, String password) {
        clickHouseConnection.setConnectionParams(url, user, password);
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
            // e.printStackTrace();
            System.out.println("Failed getDatabases");
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
            // e.printStackTrace();
            System.out.println("Failed getTables");
        }
        return tables;
    }

    // Method to fetch contents of a specific table
    public Map<String, List<String>> getTableContents(String database, String table) {
        Map<String, List<String>> tableContents = new HashMap<>();
        try (Connection connection = clickHouseConnection.getClickHouseConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + database + "." + table + " LIMIT 10")) {
    
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
    
            // Initialize the map with column names as keys and empty lists as values
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                tableContents.put(columnName, new ArrayList<>());
            }
    
            // Loop through each row in the ResultSet
            while (resultSet.next()) {
                // Append each column value to the respective list in the map
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String value = resultSet.getString(i);
    
                    // Add the value to the corresponding column list
                    tableContents.get(columnName).add(value);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("Failed getTableContents");
        }
        return tableContents;
    }
}
