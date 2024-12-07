package org.dbos.apiary.etldemo.etl;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExtractFromPostgres {
    private static Map<String, List<String>> tableNameCache = new HashMap<>();

    public static List<String> getTableNames(String postgresUrl, String postgresUser, String postgresPassword) {
        String cacheKey = postgresUrl + postgresUser + postgresPassword;
        if (tableNameCache.containsKey(cacheKey)) {
            return tableNameCache.get(cacheKey);
        }

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<String> tableNames = new ArrayList<>();
        try {
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            // System.out.println("Connected to PostgreSQL!");

            String tableQuery = "SELECT table_name FROM information_schema.tables WHERE table_type = 'BASE TABLE' AND table_schema NOT IN ('pg_catalog', 'information_schema') AND table_name != 'last_extracted_time' AND table_name != 'deleted_rows' ORDER BY table_name;";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(tableQuery);

            while (resultSet.next()) {
                tableNames.add(resultSet.getString("table_name"));
            }

            tableNameCache.put(cacheKey, tableNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableNames;
    }

    // Method to check if a timestamp column exists in current table
    public static String checkTimestampColumn(String postgresUrl, String postgresUser, String postgresPassword, String table) {
        Connection connection = null;
        ResultSet resultSet = null;
        String timestampColumn = null;

        try {
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            // System.out.println("Connected to PostgreSQL!");

            // Query to check for columns with a timestamp type
            String query = "SELECT column_name " +
                    "FROM information_schema.columns " +
                    "WHERE table_name = '" + table + "' " +
                    "AND data_type IN ('timestamp without time zone', 'timestamp with time zone')";
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                timestampColumn = resultSet.getString("column_name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return timestampColumn;
    }

    // Method to add a timestamp column if it doesn't exist
    public static void addTimestampColumn(String postgresUrl, String postgresUser, String postgresPassword, String table) {
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            // System.out.println("Connected to PostgreSQL!");
            
            // Add a new timestamp column and set its default value to current time
            String alterTableQuery = "ALTER TABLE " + table + " ADD COLUMN updated_at TIMESTAMP DEFAULT current_timestamp";
            Statement statement = connection.createStatement();
            statement.executeUpdate(alterTableQuery);

            System.out.println("Timestamp column 'updated_at' added to table " + table + ".");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static ResultSet extractData(String postgresUrl, String postgresUser, String postgresPassword, String table,
                                        String timestampColumn, String lastExtractedTime, String loadtype) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            // System.out.println("Connected to PostgreSQL!");

            statement = connection.createStatement();
            String query;
            if (timestampColumn == null || lastExtractedTime == null || loadtype == "full") {
                query = "SELECT * FROM " + table;
            } else {
                query = "SELECT * FROM " + table + " WHERE " + timestampColumn + " > '" + lastExtractedTime + "'";
            }
            resultSet = statement.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultSet;
    }
}