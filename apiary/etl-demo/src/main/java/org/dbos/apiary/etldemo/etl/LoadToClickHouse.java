package org.dbos.apiary.etldemo.etl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import java.text.SimpleDateFormat;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public class LoadToClickHouse {

    // Helper function returning SQL command for creating desired table
    public static StringBuilder generateFeatures(int rowCount, String databaseName) {
        // Start building the SQL query
        StringBuilder generatedFeatures = new StringBuilder();

        // Dynamically add 'row_i' columns based on the number of rows
        for (int i = 1; i <= rowCount; i++) {
            generatedFeatures.append("row_").append(i).append(" String, ");
        }
    
        // Remove the trailing comma and space
        generatedFeatures.setLength(generatedFeatures.length() - 2);
        
        return generatedFeatures;
    }

    // Helper function to determine ClickHouse data types
    private static String getClickHouseType(Object value) {
        if (value == null) return "String";
        
        if (value instanceof Integer) return "UInt32";
        if (value instanceof Long) return "UInt64";
        if (value instanceof String) return "String";
        if (value instanceof Double || value instanceof Float) return "Float64";
        if (value instanceof Boolean) return "UInt8";
        if (value instanceof java.sql.Timestamp 
            || value instanceof java.util.Date) return "DateTime";
        
        // Default to String for unknown types
        return "String";
    }

    // ClickHouse connection parameters
    // private static final String CLICKHOUSE_URL = "jdbc:clickhouse://localhost:8123";
    // private static final String CLICKHOUSE_USER = "default";
    // private static final String CLICKHOUSE_PASSWORD = "";

    public static void loadData(List<Map<String, List<Object>>> transformedData,
                          String clickhouseUrl, String clickhouseUser, String clickhousePassword,
                          String tableName, String databaseName) {
        try (Connection clickhouseConn = DriverManager.getConnection(clickhouseUrl, clickhouseUser, clickhousePassword);
            Statement statement = clickhouseConn.createStatement()) {
            
            System.out.println("Connected to ClickHouse!");
            
            // Create database
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);

            // Get the first row to analyze structure
            Map<String, List<Object>> firstRow = transformedData.get(0);
            
            // Create table dynamically
            StringBuilder createTableSQL = new StringBuilder(
                "CREATE TABLE IF NOT EXISTS " + databaseName + "." + tableName + " (\n"
                + "feature_name String");

            // Add columns based on the first value of each feature
            for (Map.Entry<String, List<Object>> entry : firstRow.entrySet()) {
                List<Object> values = entry.getValue();
                if (!values.isEmpty()) {
                    Object firstValue = values.get(0);
                    String columnType = getClickHouseType(firstValue);
                    createTableSQL.append(",\n").append(entry.getKey())
                                .append(" ").append(columnType);
                }
            }
            
            createTableSQL.append(") ENGINE = MergeTree() ORDER BY feature_name");
            
            statement.executeUpdate(createTableSQL.toString());

            statement.executeUpdate("TRUNCATE TABLE " + databaseName + "." + tableName);
            
            System.out.println("Database: [" + databaseName + "], table: [" + tableName + "] checked/created.");

            // Process the transformed data
            for (Map<String, List<Object>> row : transformedData) {
                for (Map.Entry<String, List<Object>> entry : row.entrySet()) {
                    String columnName = entry.getKey();
                    List<Object> values = entry.getValue();

                    // Format datetime values properly
                    StringBuilder valuesStr = new StringBuilder();
                    valuesStr.append("('").append(columnName).append("'");
                    
                    for (Object value : values) {
                        if (value instanceof java.sql.Timestamp || value instanceof java.util.Date) {
                            // Format datetime as 'YYYY-MM-DD HH:MM:SS'
                            String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(value);
                            valuesStr.append(",'").append(formattedDate).append("'");
                        } else {
                            valuesStr.append(",'").append(value).append("'");
                        }
                    }
                    valuesStr.append(")");

                    String insertSQL = "INSERT INTO " + databaseName + "." + tableName + 
                                    " VALUES " + valuesStr.toString();
                    
                    statement.executeUpdate(insertSQL);
                }
            }

            System.out.println("Data successfully inserted into ClickHouse.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}