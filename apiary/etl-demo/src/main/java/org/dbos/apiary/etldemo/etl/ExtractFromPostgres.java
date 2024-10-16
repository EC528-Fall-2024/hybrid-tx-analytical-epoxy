package org.dbos.apiary.etldemo.etl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ExtractFromPostgres {

    // PostgreSQL connection details
    private static final String POSTGRES_URL = "jdbc:postgresql://localhost:5432/campaign_product_subcategory";
    private static final String POSTGRES_USER = "postgres";
    private static final String POSTGRES_PASSWORD = "dbos";

    public static ResultSet extractData() {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // Connect to PostgreSQL
            connection = DriverManager.getConnection(POSTGRES_URL, POSTGRES_USER, POSTGRES_PASSWORD);
            System.out.println("Connected to PostgreSQL!");

            // Execute SQL query to extract data
            statement = connection.createStatement();
            String query = "SELECT * FROM campaign_product_subcategory";
            resultSet = statement.executeQuery(query);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultSet; // Return the result set for further processing
    }
}
