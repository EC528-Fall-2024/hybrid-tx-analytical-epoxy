package org.dbos.apiary.etldemo.etl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class LoadToClickHouse {

    public static void loadData(List<Map<String, List<Object>>> transformedData, 
                                 String clickhouseUrl, String clickhouseUser, String clickhousePassword) {
        Connection clickhouseConn = null;
        PreparedStatement clickhouseStmt = null;
        Statement statement = null;

        try {
            // Step 1: Connect to ClickHouse
            try {
                clickhouseConn = DriverManager.getConnection(clickhouseUrl, clickhouseUser, clickhousePassword);
                System.out.println("Connected to ClickHouse!");
                if (clickhouseConn != null) {
                    statement = clickhouseConn.createStatement();
                } else {
                    System.err.println("ClickHouse connection is null!");
                }
            } catch (SQLException e) {
                System.err.println("Failed to connect to ClickHouse: " + e.getMessage());
                e.printStackTrace();
            }

            // Step 2: Check if the ClickHouse database exists, if not, create it
            String databaseName = "campaign_product_subcategory"; // Name of OLAP database
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
            System.out.println("Database checked/created: " + databaseName);

            // Step 3: Check if the table exists, if not, create it
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + databaseName + ".campaign_product_subcategory (" +
                    "column_name String, " +
                    "value_1 String, " +
                    "value_2 String, " +
                    "value_3 String " +
                    ") ENGINE = MergeTree() ORDER BY column_name";
            statement.executeUpdate(createTableSQL);
            System.out.println("Table checked/created: campaign_product_subcategory");

            // Step 4: Prepare the insert SQL for ClickHouse
            String insertSQL = "INSERT INTO " + databaseName + ".campaign_product_subcategory " +
                    "(column_name, value_1, value_2, value_3)" + 
                    " VALUES (?, ?, ?, ?)";
            clickhouseStmt = clickhouseConn.prepareStatement(insertSQL);

            // Step 5: Loop through the transformed data and insert it into ClickHouse
            for (Map<String, List<Object>> row : transformedData) {
                for (Map.Entry<String, List<Object>> entry : row.entrySet()) {
                    String columnName = entry.getKey();
                    List<Object> values = entry.getValue();

                    // Set the values in the prepared statement
                    clickhouseStmt.setString(1, columnName);
                    clickhouseStmt.setString(2, values.size() > 0 ? values.get(0).toString() : null);
                    clickhouseStmt.setString(3, values.size() > 1 ? values.get(1).toString() : null);
                    clickhouseStmt.setString(4, values.size() > 2 ? values.get(2).toString() : null);

                    clickhouseStmt.addBatch(); // Add to batch for efficiency
                }
            }

            // Execute the batch insert
            clickhouseStmt.executeBatch();
            System.out.println("Data successfully loaded into ClickHouse!");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close connections and statements
            try {
                if (clickhouseStmt != null) clickhouseStmt.close();
                if (statement != null) statement.close();
                if (clickhouseConn != null) clickhouseConn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
