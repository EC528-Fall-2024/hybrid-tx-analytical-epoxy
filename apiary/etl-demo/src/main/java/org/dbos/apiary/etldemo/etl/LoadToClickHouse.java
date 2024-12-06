package org.dbos.apiary.etldemo.etl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
// import java.sql.ResultSet;
import java.sql.Statement;

import java.text.SimpleDateFormat;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


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
            // System.out.println("data: " + transformedData);
            
            // Create database
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);

            // Get the first row to analyze structure and number of columns
            Map<String, List<Object>> firstRow = transformedData.get(0);
            List<String> columnNames = new ArrayList<>(firstRow.keySet());
            int numRows = firstRow.values().iterator().next().size();

            // Create table dynamically with numbered columns
            StringBuilder createTableSQL = new StringBuilder(
                "CREATE TABLE IF NOT EXISTS " + databaseName + "." + tableName + " (");

            for (int i = 0; i < columnNames.size(); i++) {
                if (i > 0) {
                    createTableSQL.append(", ");
                }
                createTableSQL.append(columnNames.get(i)).append(" String");
            }
            
            createTableSQL.append(") ENGINE = MergeTree() ORDER BY tuple()");
            statement.executeUpdate(createTableSQL.toString());

            statement.executeUpdate("TRUNCATE TABLE " + databaseName + "." + tableName);
            
            System.out.println("Database: [" + databaseName + "], table: [" + tableName + "] checked/created.");
        
            // Prepare insert query
            StringBuilder insertQuery = new StringBuilder();
            insertQuery.append("INSERT INTO ")
                      .append(databaseName)
                      .append(".")
                      .append(tableName)
                      .append(" (")
                      .append(String.join(", ", columnNames))
                      .append(") VALUES ");

            // Insert data row by row
            for (Map<String, List<Object>> dataMap : transformedData) {
                List<Object> firstColumnValues = dataMap.get(columnNames.get(0));
                int rowCount = firstColumnValues.size();
                
                for (int row = 0; row < rowCount; row++) {
                    if (row > 0 || transformedData.indexOf(dataMap) > 0) {
                        insertQuery.append(", ");
                    }
                    
                    insertQuery.append("(");
                    for (int col = 0; col < columnNames.size(); col++) {
                        if (col > 0) {
                            insertQuery.append(", ");
                        }
                        Object value = dataMap.get(columnNames.get(col)).get(row);
                        
                        if (value == null) {
                            insertQuery.append("NULL");
                        } else if (value instanceof Number) {
                            insertQuery.append("'" + value + "'");
                        } else if (value instanceof java.sql.Timestamp || value instanceof java.util.Date) {
                            String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(value);
                            insertQuery.append("'" + formattedDate + "'");
                        } else {
                            insertQuery.append("'" + value.toString().replace("'", "\\'") + "'");
                        }
                    }
                    insertQuery.append(")");
                }
            }

            statement.execute(insertQuery.toString());
            System.out.println("Data successfully inserted into ClickHouse.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}