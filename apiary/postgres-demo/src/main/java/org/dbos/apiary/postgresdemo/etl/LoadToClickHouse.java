package org.dbos.apiary.postgresdemo.etl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.sql.SQLException;


public class LoadToClickHouse {

    // ClickHouse connection parameters
    private static final String CLICKHOUSE_URL = "jdbc:clickhouse://localhost:8123";
    private static final String CLICKHOUSE_USER = "default";
    private static final String CLICKHOUSE_PASSWORD = "";

    public static void loadData(ResultSet resultSet) {
        Connection clickhouseConn = null;
        PreparedStatement clickhouseStmt = null;
        Statement statement = null;

        try {
            // Step 1: Connect to ClickHouse
            // clickhouseConn = DriverManager.getConnection(CLICKHOUSE_URL, CLICKHOUSE_USER, CLICKHOUSE_PASSWORD);
            // System.out.println("Connected to ClickHouse!");
            // statement = clickhouseConn.createStatement();
            try {
                clickhouseConn = DriverManager.getConnection(CLICKHOUSE_URL, CLICKHOUSE_USER, CLICKHOUSE_PASSWORD);
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
                    "campaign_product_subcategory_id UInt32, " +
                    "campaign_id UInt32, " +
                    "subcategory_id UInt32, " +
                    "discount Float32" +
                    ") ENGINE = MergeTree() ORDER BY campaign_product_subcategory_id";
            statement.executeUpdate(createTableSQL);
            System.out.println("Table checked/created: campaign_product_subcategory");

            // Step 4: Prepare the insert SQL for ClickHouse
            String insertSQL = "INSERT INTO " + databaseName + ".campaign_product_subcategory " +
                    "(campaign_product_subcategory_id, campaign_id, subcategory_id, discount) " +
                    "VALUES (?, ?, ?, ?)";
            clickhouseStmt = clickhouseConn.prepareStatement(insertSQL);

            // Step 5: Loop through the PostgreSQL result set and insert the data into ClickHouse
            while (resultSet.next()) {
                clickhouseStmt.setInt(1, resultSet.getInt("campaign_product_subcategory_id"));
                clickhouseStmt.setInt(2, resultSet.getInt("campaign_id"));
                clickhouseStmt.setInt(3, resultSet.getInt("subcategory_id"));
                clickhouseStmt.setFloat(4, resultSet.getFloat("discount"));

                clickhouseStmt.addBatch(); // Add to batch for efficiency
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
