package org.dbos.apiary.etldemo.etl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
// import java.sql.ResultSet;
import java.sql.Statement;

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

    // ClickHouse connection parameters
    // private static final String CLICKHOUSE_URL = "jdbc:clickhouse://localhost:8123";
    // private static final String CLICKHOUSE_USER = "default";
    // private static final String CLICKHOUSE_PASSWORD = "";

    public static void loadData(List<Map<String, List<Object>>> transformedData, String table,
                                String clickhouseUrl, String clickhouseUser, String clickhousePassword) {
        Connection clickhouseConn = null;
        PreparedStatement clickhouseStmt = null;
        Statement statement = null;

        try {
            // Step 1: Connect to ClickHouse
            try {
                // clickhouseConn = DriverManager.getConnection(CLICKHOUSE_URL, CLICKHOUSE_USER, CLICKHOUSE_PASSWORD);
                clickhouseConn = DriverManager.getConnection(clickhouseUrl, clickhouseUser, clickhousePassword);
                System.out.println("Connected to ClickHouse!");
                if (clickhouseConn != null) {
                    statement = clickhouseConn.createStatement();
                } else {
                    System.err.println("ClickHouse connection is null!");
                    return;
                }
            } catch (SQLException e) {
                System.err.println("Failed to connect to ClickHouse: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            // Step 2: Check if the ClickHouse database exists, if not, create it
            String databaseName = System.getenv("campaign_product_subcategory");
            if (databaseName == null || databaseName.isEmpty()) {
                System.err.println("Database name is not provided!");
                databaseName = "campaign_product_subcategory";
                // return;
            }
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);

            // Create table if not exist
            StringBuilder createTableSQL = new StringBuilder(
                "CREATE TABLE IF NOT EXISTS " + databaseName + "." + table + " ("
                + "feature_name String, ");
            // Generate feature SQL command
            StringBuilder generatedFeaturesSQL = generateFeatures(transformedData.get(0).entrySet().iterator().next().getValue().size(), databaseName);
            // Complete the SQL query with the ClickHouse engine configuration
            createTableSQL.append(generatedFeaturesSQL);
            createTableSQL.append(") ENGINE = MergeTree() ORDER BY feature_name");
            statement.executeUpdate(createTableSQL.toString());
            
            System.out.println("Database, table checked/created: " + databaseName);

            // Step 3: Process the transformed data
            for (Map<String, List<Object>> row : transformedData) {
                for (Map.Entry<String, List<Object>> entry : row.entrySet()) {
                    String columnName = entry.getKey();
                    List<Object> values = entry.getValue();

                    // Dynamically construct a SQL insert statement based on the number of values
                    StringBuilder placeholders = new StringBuilder();
                    for (int i = 0; i < values.size(); i++) {
                        placeholders.append("?,");
                    }
                    // Remove trailing comma
                    if (placeholders.length() > 0) {
                        placeholders.deleteCharAt(placeholders.length() - 1);
                    }

                    // String insertSQL = "INSERT INTO " + databaseName + ".campaign_product_subcategory (feature_name, " 
                    //         + generatedFeaturesSQL.toString().replace(" String", "") 
                    //         + ") VALUES (" + columnName + "," + placeholders.toString() + ")";
                    
                    String insertSQL = "INSERT INTO " + databaseName + "." + table + " (feature_name, " 
                            + generatedFeaturesSQL.toString().replace(" String", "") 
                            + ") VALUES ('" + columnName + "', " + placeholders.toString() + ")";

                    // System.out.println(insertSQL);
                    clickhouseStmt = clickhouseConn.prepareStatement(insertSQL);

                    // Dynamically set the values in the prepared statement
                    for (int i = 0; i < values.size(); i++) {
                        clickhouseStmt.setObject(i + 1, values.get(i)); // Generalized to handle any data type
                    }

                    // clickhouseStmt.addBatch(); // Add to batch for efficiency 
                    clickhouseStmt.executeUpdate();
                }
            }

            // Step 4: Execute the batch
            // clickhouseStmt.executeBatch();
            System.out.println("Data successfully inserted into ClickHouse.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close the connections and statements
            try {
                if (clickhouseStmt != null) clickhouseStmt.close();
                if (statement != null) statement.close();
                if (clickhouseConn != null) clickhouseConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
