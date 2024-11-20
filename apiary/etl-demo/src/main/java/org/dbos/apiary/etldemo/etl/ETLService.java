package org.dbos.apiary.etldemo.etl;

import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ETLService {

    // Function for create timestamp table if not exists for incremental load detection
    public static void createLastExtractedTimeTable(String postgresUrl, String postgresUser, String postgresPassword) {
        Connection connection = null;
        Statement statement = null;

        try {
            // Connect to the PostgreSQL database
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            // System.out.println("Connected to PostgreSQL!");

            // Define the CREATE TABLE IF NOT EXISTS query
            String createTableQuery = "CREATE TABLE IF NOT EXISTS last_extracted_time (" +
                    "table_name TEXT PRIMARY KEY, " +
                    "last_extracted_time TIMESTAMP" +
                    ")";

            // Execute the query
            statement = connection.createStatement();
            statement.executeUpdate(createTableQuery);

            System.out.println("Table 'last_extracted_time' is ensured to exist.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Function for get last extract time for a given table
    public static String getLastExtractedTime(String postgresUrl, String postgresUser, String postgresPassword, String table) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        String lastExtractedTime = null;

        try {
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            statement = connection.createStatement();
            String query = "SELECT last_extracted_time FROM last_extracted_time WHERE table_name = '" + table + "'";
            resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                lastExtractedTime = resultSet.getString("last_extracted_time");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return lastExtractedTime;
    }

    // Function for update last extract time for a given table
    public static void updateLastExtractedTime(String postgresUrl, String postgresUser, String postgresPassword, String table) {
        Connection connection = null;
        Statement statement = null;
    
        try {
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            statement = connection.createStatement();
            // String query = "UPDATE last_extracted_time SET last_extracted_time = current_timestamp WHERE table_name = '" + table + "'";
            String query = "INSERT INTO last_extracted_time (table_name, last_extracted_time) " +
                             "VALUES ('" + table + "', current_timestamp) " +
                             "ON CONFLICT (table_name) DO UPDATE " +
                             "SET last_extracted_time = current_timestamp";
            statement.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Map<String, Object>> resultSetToList(ResultSet resultSet) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
                rows.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public void runETL(String postgresUrl, String postgresUser, String postgresPassword, 
                       String clickhouseUrl, String clickhouseUser, String clickhousePassword) {
        ResultSet resultSet = null;

        try {
            List<String> tableNames = ExtractFromPostgres.getTableNames(postgresUrl, postgresUser, postgresPassword);

            // Extract database name
            int lastSlashIndex = postgresUrl.lastIndexOf("/");
            String databaseName = postgresUrl.substring(lastSlashIndex + 1);


            // Check and create extract time table if not exists
            createLastExtractedTimeTable(postgresUrl, postgresUser, postgresPassword);

            for (String tableName : tableNames) {
                // Step 1: Extract data from PostgreSQL using provided credentials - incremental load
                // 1.1: Check for a timestamp column
                String timestampColumn = ExtractFromPostgres.checkTimestampColumn(postgresUrl, postgresUser, postgresPassword, tableName);
                String lastExtractedTime = getLastExtractedTime(postgresUrl, postgresUser, postgresPassword, tableName);
                
                resultSet = ExtractFromPostgres.extractData(postgresUrl, postgresUser, postgresPassword, tableName, timestampColumn, lastExtractedTime);

                if (timestampColumn == null) {
                    // If no timestamp column exists, extract everything and add a timestamp column
                    System.out.println("No timestamp column found. Extracting all rows...");

                    // Add a timestamp column for future tracking
                    ExtractFromPostgres.addTimestampColumn(postgresUrl, postgresUser, postgresPassword, tableName);
                } else {
                    // If a timestamp column exists, extract rows based on the timestamp
                    System.out.println("Timestamp column found: " + timestampColumn);
                }

                // Update last extract time
                updateLastExtractedTime(postgresUrl, postgresUser, postgresPassword, tableName);

                //resultSet = ExtractFromPostgres.extractData(postgresUrl, postgresUser, postgresPassword, tableName);
                
                // Only perform transform and load if we attracted data
                if (resultSet != null && resultSet.isBeforeFirst()) {
                    // Step 2: Transform data from column-based to row-based
                    List<Map<String, List<Object>>> transformedData = TransformService.transformColumnToRow(resultSet);
                    
                    // Step 3: Load the transformed data into ClickHouse in batches
                    // LoadToClickHouse.loadData(transformedData, tableName, clickhouseUrl, clickhouseUser, clickhousePassword);
                    LoadToClickHouse.loadData(transformedData, clickhouseUrl, clickhouseUser, clickhousePassword, tableName, databaseName);
                } else {
                    System.out.println("Already updated since previous ETL!");
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
