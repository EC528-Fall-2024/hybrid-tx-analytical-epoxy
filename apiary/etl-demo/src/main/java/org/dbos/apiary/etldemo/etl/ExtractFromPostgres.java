package org.dbos.apiary.etldemo.etl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ExtractFromPostgres {

    public static ResultSet extractData(String postgresUrl, String postgresUser, String postgresPassword) {
        
        try {
            // Connect to PostgreSQL
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            System.out.println("Connected to PostgreSQL!");

            // Execute SQL query to extract data
            statement = connection.createStatement();
            String query = "SELECT * FROM campaign_product_subcategory"; // Modify as needed
            resultSet = statement.executeQuery(query);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultSet; // Return the result set for further processing
    }
}
