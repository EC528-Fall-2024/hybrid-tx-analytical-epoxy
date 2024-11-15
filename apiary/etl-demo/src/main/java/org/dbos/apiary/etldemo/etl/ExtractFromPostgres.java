package org.dbos.apiary.etldemo.etl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
// import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class ExtractFromPostgres {
    public static List<String> getTableNames(String postgresUrl, String postgresUser, String postgresPassword) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<String> tableNames = new ArrayList<>();
        try {
            // Connect to PostgreSQL
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            System.out.println("Connected to PostgreSQL!");

            // Obtain table names
            String tableQuery = "SELECT table_name FROM information_schema.tables WHERE table_type = 'BASE TABLE' AND table_schema NOT IN ('pg_catalog', 'information_schema') ORDER BY table_name;";
            statement = connection.createStatement();
            ResultSet tableResultSet = statement.executeQuery(tableQuery);

            // Store table names in a list
            while (tableResultSet.next()) {
                tableNames.add(tableResultSet.getString("table_name"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("This is from extract from postgres:");
        System.out.println(tableNames);
        return tableNames;
    }

    public static ResultSet extractData(String postgresUrl, String postgresUser, String postgresPassword, String table) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            // Connect to PostgreSQL
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            System.out.println("Connected to PostgreSQL!");

            // Execute SQL query to extract data
            statement = connection.createStatement();
            String query = "SELECT * FROM " + table; // Modify as needed
            resultSet = statement.executeQuery(query);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultSet; // Return the result set for further processing
    }
}